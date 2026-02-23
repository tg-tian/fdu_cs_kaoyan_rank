FROM python:3.12-slim
WORKDIR /app
RUN apt-get update && apt-get install -y libgl1 libglib2.0-0 libgomp1 tzdata && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
ENV PORT=6667
EXPOSE 6667
CMD ["python","server.py"]
