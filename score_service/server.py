import grpc
from concurrent import futures
import os
import re
import time
import random
from datetime import datetime
from urllib.parse import urljoin
import score_pb2
import score_pb2_grpc
import requests
from ocr import recognize_captcha
from dotenv import load_dotenv
from logger import setup_logging

# Load environment variables
load_dotenv()
logger = setup_logging()


class ScoreService(score_pb2_grpc.ScoreServiceServicer):

    def GetScore(self, request, context):
        exam_no = (request.examNo or "").strip()
        id_card = (request.idCard or "").strip()
        try:
            total_score = fetch_scores(exam_no, id_card)
            return score_pb2.GetScoreResponse(
                success=True,
                totalScore=total_score,
                message="查询成功"
            )
        except Exception as exc:
            return score_pb2.GetScoreResponse(
                success=False,
                totalScore={},
                message=str(exc)
            )


def serve():
    max_workers = int(os.getenv("MAX_WORKERS", "5"))
    port = int(os.getenv("PORT", "6667"))
    
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=max_workers))
    score_pb2_grpc.add_ScoreServiceServicer_to_server(
        ScoreService(), server
    )

    server.add_insecure_port(f'[::]:{port}')
    server.start()
    logger.info(f"gRPC Server started on port {port} with {max_workers} workers...")
    server.wait_for_termination()


def fetch_scores(exam_no, id_card):
    index_url = os.getenv("INDEX_HTTP", "https://gsas.fudan.edu.cn/sscjcx/index").strip()
    login_base = os.getenv("LOGIN_HTTP", "https://gsas.fudan.edu.cn").strip()
    year = os.getenv("FDU_YEAR", "2026")
    captcha_url = os.getenv("CAPTCHA_HTTP", "https://gsas.fudan.edu.cn/captcha/imageCode").strip()

    session = requests.Session()
    headers = _build_headers()
    index_res = session.get(index_url, headers=headers, timeout=10)
    index_res.raise_for_status()
    action = _extract_action(index_res.text)
    if not action:
        raise RuntimeError("未解析到登录action")
    verify_code = _retry_verify_code(session, captcha_url, headers)
    if len(verify_code) != 4:
        raise RuntimeError("验证码识别失败")

    form = {
        "nd": str(year),
        "username": exam_no,
        "password": id_card,
        "validateCode": verify_code
    }
    login_url = urljoin(login_base, action)
    login_res = session.post(login_url, data=form, headers=_build_login_headers(), timeout=10)
    login_res.raise_for_status()
    error_info = _extract_error_info(login_res.text)
    if error_info:
        if "验证码错误" in error_info:
            raise RuntimeError("验证码错误")
        if "未查询到相应的成绩" in error_info:
            raise RuntimeError("未查询到相应的成绩，请检查输入信息!")
        if "暂未到开放时间" in error_info:
            raise RuntimeError("暂未到开放时间!")
    

    total_score = _extract_scores(login_res.text)
    if not total_score:
        raise RuntimeError("未解析到成绩信息")
    return total_score

def _build_headers():
    return {
        "User-Agent": "Mozilla/5.0",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
        "Accept-Language": "zh-CN,zh;q=0.9",
        "Connection": "keep-alive"
    }


def _build_login_headers():
    return {
        "Accept": "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8",
        "Accept-Language": "zh-CN,zh;q=0.9",
        "Accept-Encoding": "gzip, deflate, br",
        "Host": "gsas.fudan.edu.cn",
        "Referer": "https://gsas.fudan.edu.cn/logon",
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Origin": "https://gsas.fudan.edu.cn",
        "Sec-Ch-Ua": '"Not_A Brand";v="8", "Chromium";v="120", "Google Chrome";v="120"',
        "Content-Type": "application/x-www-form-urlencoded",
        "Connection": "keep-alive",
        "Cache-Control": "max-age=0"
    }


def _extract_action(html):
    match = re.search(r'form action=\"(.*?)\"', html, re.IGNORECASE)
    if not match:
        return ""
    return match.group(1)


def _retry_verify_code(session, captcha_url, headers):
    best_code = ""
    for _ in range(2):
        image_res = session.get(captcha_url, headers=headers, timeout=10)
        image_res.raise_for_status()
        code = recognize_captcha(image_res.content)
        code = re.sub(r'[^0-9a-zA-Z]', '', code or "")
        logger.info(f"Recognized captcha: {code}")
        if len(code) == 4:
            return code
        best_code = code
    return best_code

def _extract_error_info(html):
    match = re.search(r'<div class="form-group" id="errorInfo"[^>]*>\s*(.*?)\s*</div>', html, re.DOTALL | re.IGNORECASE)
    if match:
        return match.group(1).strip()
    return ""


def _extract_scores(html):
    pattern = re.compile(
        r"准考证号</td>\s*<td[^>]*>(\d+)</td>.*?"
        r"姓名</td>\s*<td[^>]*>([^<]+)</td>.*?"
        r"报考院系</td>.*?<td[^>]*>([^<]+)</td>.*?"
        r"报考专业</td>.*?<td[^>]*>([^<]+)</td>.*?"
        r"101 思想政治理论</td>\s*<td[^>]*>([^<]+)</td>.*?"
        r"20.*? 英语.*?</td>\s*<td[^>]*>([^<]+)</td>.*?"
        r"<td colspan=\"2\" style=\"border:1px solid black;text-align: center;\">(.*?)</td>.*?"
        r"<td colspan=\"2\" style=\"border:1px solid black;text-align: center;\">(.*?)</td>.*?"
        r"<td colspan=\"2\" style=\"border:1px solid black;text-align: center;\">(.*?)</td>.*?"
        r"<td colspan=\"2\" style=\"border:1px solid black;text-align: center;\">(.*?)</td>.*?"
        r"总分</td>\s*<td[^>]*>([^<]+)</td>.*?",
        re.DOTALL | re.IGNORECASE
    )

    match = pattern.search(html)
    if not match:
        return {}

    try:
        # group 5: 政治
        politics = int(match.group(5).strip())
        # group 6: 英语
        english = int(match.group(6).strip())
        # group 8: 专业课1 (数学)
        math_score = int(match.group(8).strip())
        # group 10: 专业课2 (408)
        cs_score = int(match.group(10).strip())

        return {
            "math": math_score,
            "english": english,
            "politics": politics,
            "408": cs_score
        }
    except (ValueError, IndexError) as e:
        logger.error(f"解析分数数值失败: {e}")
        return {}


if __name__ == '__main__':
    serve()
