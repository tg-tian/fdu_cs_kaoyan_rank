import os

# Disable PaddleX model source check for offline usage
os.environ["PADDLE_PDX_DISABLE_MODEL_SOURCE_CHECK"] = "True"
# Suppress C++ logging from Paddle/OneDNN
os.environ["GLOG_minloglevel"] = "3"
os.environ["PADDLE_INFERENCE_LOG_LEVEL"] = "0" # 0: None, 1: Error, 2: Warn, 3: Info

import warnings
# Suppress all warnings
warnings.filterwarnings("ignore")

import logging
# Configure logging to suppress paddle output
logging.getLogger("ppocr").setLevel(logging.ERROR)
logging.getLogger("paddlex").setLevel(logging.ERROR)

import re
import io
import numpy as np
from PIL import Image
from paddleocr import TextRecognition

# Define the local model path
MODEL_DIR = os.path.join(os.path.dirname(__file__), "models", "PP-OCRv5_server_rec")

import cv2

# Initialize OCR model (Global initialization, runs once on import)
_OCR = TextRecognition(model_dir=MODEL_DIR, enable_mkldnn=True)

def preprocess_image(image_bytes):
    nparr = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_GRAYSCALE)
    _, binary = cv2.threshold(img, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
    height, width = binary.shape
    upscaled = cv2.resize(binary, (width * 2, height * 2), interpolation=cv2.INTER_CUBIC)
    denoised = cv2.medianBlur(upscaled, 3)
    final_img = cv2.cvtColor(denoised, cv2.COLOR_GRAY2RGB)
    return final_img

def recognize_captcha(image_bytes):
    processed_image = preprocess_image(image_bytes)
    result = _OCR.predict(processed_image)
    
    if not result:
        return ""
    all_texts = []
    for item in result:
        # Access as dict
        text = item.get('rec_text', '')
        if text:
            all_texts.append(text)
    
    raw_text = "".join(all_texts)
    raw_text = raw_text.replace(" ", "")
    
    return raw_text.strip()
