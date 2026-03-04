# FDU CS Kaoyan Rank (复旦大学计算机考研排名系统)

这是一个用于查询和排名复旦大学计算机考研成绩的系统。通过后端服务与爬虫识别服务协作，自动获取成绩并进行排名与可视化分析。

## 核心功能

- 自动化查询成绩，支持验证码识别与异常提示
- 成绩排名统计与数据可视化分析
- SSE 实时推送查询进度
- 分布式限流与排队机制
- 用户敏感信息 HMAC 脱敏存储

## 技术栈

- 后端：Java 21, Spring Boot 3, MyBatis, Redis (Redisson)
- 服务通信：gRPC + Protobuf
- 识别服务：Python 3.12, PaddleOCR
- 前端：Vue 3 + TypeScript + Vite + ECharts
- 部署：Docker, Nginx

## 系统架构

- 前端通过 HTTP 调用后端 API 获取排名与图表数据
- 后端通过 gRPC 调用 Python 识别服务，自动完成成绩查询
- MySQL 存储用户与成绩数据，Redis 做限流与登录状态

## 项目结构 (Project Structure)

- `backend/`: 后端服务，基于 Java Spring Boot 构建
- `frontend/`: 前端应用，基于 Vue 3 + TypeScript + Vite 构建
- `score_service/`: Python gRPC 识别服务，负责验证码识别与成绩爬取
- `proto/`: gRPC 接口定义

## 本地开发

### 依赖环境

- JDK 21
- Maven 3.9+
- Python 3.12
- Node.js 20+
- MySQL 8+
- Redis 6+

### 数据库准备

创建数据库 `kaoyan_rank`，并准备表结构（可按实际情况调整字段类型）。

```sql
CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  exam_no_hash CHAR(64) NOT NULL,
  id_card_hash CHAR(64) NOT NULL,
  role VARCHAR(32) DEFAULT 'user',
  status TINYINT DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE exam_score (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  exam_no_hash CHAR(64) NOT NULL,
  english_score INT NOT NULL,
  politics_score INT NOT NULL,
  math_score INT NOT NULL,
  score_408 INT NOT NULL
);
```


```bash
cd frontend
npm install
npm run dev
```

前端默认通过 Vite 代理将 `/api` 请求转发到 `http://localhost:6666`。

## 配置说明

### 后端环境变量

- `SERVER_PORT`：后端端口，默认 6666
- `SPRING_DATASOURCE_URL` / `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATA_REDIS_HOST` / `SPRING_DATA_REDIS_PORT` / `SPRING_DATA_REDIS_DATABASE`
- `SPRING_DATA_REDIS_USERNAME` / `SPRING_DATA_REDIS_PASSWORD`
- `APP_HMAC_SECRET`：HMAC 密钥
- `APP_IP_WHITELIST`：IP 白名单，逗号分隔
- `GRPC_CLIENT_SCORE_SERVICE_ADDRESS`：gRPC 服务地址

### 识别服务环境变量

`score_service` 支持 `.env` 文件，也可直接设置环境变量：

- `PORT`：gRPC 服务端口，默认 6667
- `MAX_WORKERS`：线程数，默认 5
- `INDEX_HTTP` / `LOGIN_HTTP` / `CAPTCHA_HTTP`
- `FDU_YEAR`：查询年份，默认 2026

## 部署方法

### 三台 2C2G 云服务器部署方案

部署形态为 1 台对外入口 + 2 台业务节点，使用 Nginx 实现负载均衡。

- 服务器 A（入口机）：Nginx + 前端静态资源 + 后端服务实例 #1
- 服务器 B（业务机）：后端服务实例 #2 + Python 识别服务实例 #1
- 服务器 C（业务机）：后端服务实例 #3 + Python 识别服务实例 #2

### Nginx 负载均衡配置

在服务器 A 上配置 Nginx，将前端请求转发到 3 个后端实例，并为后端接口做负载均衡。

前端构建后将 `dist/` 放置到服务器 A 的 `/usr/share/nginx/html`。
