import os
import requests
import server
from ocr import recognize_captcha
from dotenv import load_dotenv


def main():
    load_dotenv()
    index_url = os.getenv("INDEX_HTTP").strip()
    captcha_url = os.getenv("CAPTCHA_HTTP").strip()
    headers = server._build_headers()
    session = requests.Session()
    image_res = session.get(captcha_url, headers=headers, timeout=10)
    image_res.raise_for_status()
    
    # Save the captcha image for inspection
    with open("captcha_debug.png", "wb") as f:
        f.write(image_res.content)
    print("Saved captcha image to captcha_debug.png")
    
    text = recognize_captcha(image_res.content)
    print("captcha_url:", captcha_url)
    print("ocr:", text)


if __name__ == "__main__":
    main()
