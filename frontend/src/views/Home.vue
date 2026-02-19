<template>
  <div class="home-container">
    <div class="home-content">
      <h1 class="title">复旦CS考研录分网站</h1>
      <p class="subtitle">欢迎使用成绩统计与排名查询平台</p>
      <div class="info-card">
        <div class="info-item">
          <span class="label">当前状态</span>
          <span class="value">已登录</span>
        </div>
        <div class="info-item">
          <span class="label">功能入口</span>
          <span class="value">成绩查询 / 排名统计</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts"></script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
  background-color: #f5f7fa;
}

.home-content {
  width: 100%;
  max-width: 640px;
  background: #fff;
  border-radius: 12px;
  padding: 32px 28px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.title {
  margin: 0 0 8px;
  color: #1c4e8e;
  font-size: 28px;
  letter-spacing: 1px;
}

.subtitle {
  margin: 0 0 24px;
  color: #606266;
  font-size: 15px;
}

.info-card {
  background: #f8fafc;
  border-radius: 10px;
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  color: #303133;
  font-size: 14px;
}

.label {
  color: #909399;
}

@media (max-width: 480px) {
  .home-content {
    padding: 24px 18px;
  }

  .title {
    font-size: 22px;
  }
}
</style>
