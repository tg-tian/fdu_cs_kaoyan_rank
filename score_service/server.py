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

# Load environment variables
load_dotenv()


class ScoreService(score_pb2_grpc.ScoreServiceServicer):

    def GetScore(self, request, context):
        exam_no = (request.examNo or "").strip()
        id_card = (request.idCard or "").strip()
        # try:
        #     total_score = fetch_scores(exam_no, id_card)
        #     return score_pb2.GetScoreResponse(
        #         success=True,
        #         totalScore=total_score,
        #         message="查询成功"
        #     )
        # except Exception as exc:
        #     return score_pb2.GetScoreResponse(
        #         success=False,
        #         totalScore={},
        #         message=str(exc)
        #     )
        time.sleep(3)
        total_score = {
            "math": random.randint(60, 150),
            "english": random.randint(0, 150),
            "politics": random.randint(40, 100),
            "408": random.randint(40, 150)
        }
        return score_pb2.GetScoreResponse(
            success=True,
            totalScore=total_score,
            message="查询成功"
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
    print(f"gRPC Server started on port {port} with {max_workers} workers...")
    server.wait_for_termination()


def fetch_scores(exam_no, id_card):
    index_url = os.getenv("INDEX_HTTP", "").strip()
    login_base = os.getenv("LOGIN_HTTP", "").strip()
    year = os.getenv("FDU_YEAR", str(datetime.now().year))
    captcha_url = os.getenv("CAPTCHA_HTTP", "").strip()

    session = requests.Session()
    headers = _build_headers()
    index_res = session.get(index_url, headers=headers, timeout=10)
    index_res.raise_for_status()
    action = _extract_action(index_res.text)
    if not action:
        raise RuntimeError("未解析到登录action")
    captcha_url = _extract_captcha_url(index_res.text, index_url)
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
    login_res = session.post(login_url, data=form, headers=_build_login_headers(headers, login_base), timeout=10)
    login_res.raise_for_status()
    error_info = _extract_error_info(login_res.text)
    if error_info:
        raise RuntimeError(error_info)

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


def _build_login_headers(headers, referer):
    merged = dict(headers)
    merged["Referer"] = referer
    return merged


def _extract_action(html):
    match = re.search(r'form action=\"(.*?)\"', html, re.IGNORECASE)
    if not match:
        return ""
    return match.group(1)


def _extract_captcha_url(html, base_url):
    match = re.search(r'<img[^>]+src=["\']([^"\']+)["\'][^>]*(captcha|verify|validate)[^>]*>', html, re.IGNORECASE)
    if not match:
        match = re.search(r'<img[^>]+src=["\']([^"\']+)["\']', html, re.IGNORECASE)
    if not match:
        return ""
    return urljoin(base_url, match.group(1))


def _retry_verify_code(session, captcha_url, headers):
    best_code = ""
    for _ in range(2):
        image_res = session.get(captcha_url, headers=headers, timeout=10)
        image_res.raise_for_status()
        code = recognize_captcha(image_res.content)
        code = re.sub(r'[^0-9a-zA-Z]', '', code or "")
        if len(code) == 4:
            return code
        best_code = code
    return best_code

def _extract_error_info(html):
    if re.search(r'验证码.*(错误|无效)', html):
        return "验证码错误"
    if re.search(r'(密码|口令).*(错误|不正确)', html):
        return "密码错误"
    if re.search(r'(未开放|未到|开放时间)', html):
        return "未到开放时间"
    return ""


def _extract_scores(html):
    scores = {}
    pairs = re.findall(r'<t[dh][^>]*>\s*([^<]+?)\s*</t[dh]>\s*<t[dh][^>]*>\s*([0-9]{1,3})\s*</t[dh]>', html, re.IGNORECASE)
    for name, score in pairs:
        name = re.sub(r'\s+', '', name)
        score = int(score)
        if name and score >= 0:
            scores[name] = score
    return scores


if __name__ == '__main__':
    serve()
