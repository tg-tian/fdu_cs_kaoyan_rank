import io
import numpy as np
from PIL import Image
from paddleocr import PaddleOCR


_OCR = PaddleOCR(use_angle_cls=False, lang="en")


def recognize_captcha(image_bytes):
    image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
    result = _OCR.ocr(np.array(image), cls=False)
    if not result:
        return ""
    best_text = ""
    best_score = -1.0
    for line in result:
        if not line:
            continue
        text, score = line[1][0], line[1][1]
        if score is None:
            score = 0.0
        if score > best_score:
            best_score = score
            best_text = text
    return (best_text or "").strip()
