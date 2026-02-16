<template>
  <div class="login-container">
    <div class="login-card-wrapper">
      <el-card class="login-card" :body-style="{ padding: '0px' }">
        <div class="banner-area">
          <img src="/bg.jpg" alt="Fudan University" class="banner-img" />
        </div>
        
        <div class="form-content">
          <div class="header-area">
            <h2 class="sub-title">复旦CS考研录分网站</h2>
          </div>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="rules"
            class="login-form"
            status-icon
            label-position="top"
          >
            <el-form-item label="考生编号" prop="candidateNumber">
              <el-input 
                v-model="loginForm.candidateNumber" 
                placeholder="请输入15位考生编号"
                maxlength="15"
                clearable
                size="large"
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="证件号码" prop="idNumber">
              <el-input 
                v-model="loginForm.idNumber" 
                placeholder="请输入18位身份证号"
                maxlength="18"
                show-password
                clearable
                size="large"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item class="actions-item">
              <el-button 
                type="primary" 
                @click="handleLogin(loginFormRef)" 
                :loading="loading"
                class="login-button"
                size="large"
              >
                {{ loading ? '查询中...' : '查询成绩' }}
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="tips">
            <p>首次登录查询过程可能较慢，请耐心等待。</p>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { loginAndGetScore } from '../api/user'

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  candidateNumber: '',
  idNumber: ''
})

const validateCandidateNumber = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请输入考生编号'))
  } else if (!/^\d{15}$/.test(value)) {
    callback(new Error('考生编号通常为15位数字'))
  } else {
    callback()
  }
}

const validateIdNumber = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请输入证件号码'))
  } else if (!/(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/.test(value)) {
    callback(new Error('请输入正确的身份证号码'))
  } else {
    callback()
  }
}

const rules = reactive<FormRules>({
  candidateNumber: [
    { validator: validateCandidateNumber, trigger: 'blur' },
    { required: true, message: '请输入考生编号', trigger: 'blur' }
  ],
  idNumber: [
    { validator: validateIdNumber, trigger: 'blur' },
    { required: true, message: '请输入证件号码', trigger: 'blur' }
  ]
})

const handleLogin = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  
  await formEl.validate(async (valid, fields) => {
    if (valid) {
      loading.value = true
      try {
        const scoreData = await loginAndGetScore({
          candidateNumber: loginForm.candidateNumber,
          idNumber: loginForm.idNumber
        })
        
        ElMessage.success('查询成功')
        console.log('User Score:', scoreData)
        // 登录成功逻辑
        
      } catch (error: any) {
        ElMessage.error(error.message || '查询失败，请稍后重试')
      } finally {
        loading.value = false
      }
    } else {
      console.log('error submit!', fields)
    }
  })
}
</script>

<style scoped>
:deep(:root) {
  --fudan-blue: #1c4e8e;
  --fudan-blue-hover: #163d72;
}

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  width: 100%;
  background-color: #f5f7fa; /* 干净的浅灰背景 */
}

.login-card-wrapper {
  width: 100%;
  max-width: 440px;
  padding: 20px;
}

.login-card {
  border: none;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08); /* 柔和阴影 */
  overflow: hidden;
  background-color: #fff;
}

.banner-area {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.banner-area:hover .banner-img {
  transform: scale(1.05); /* 细微的交互 */
}

.banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(28, 78, 142, 0.3), rgba(28, 78, 142, 0.7));
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-title {
  color: #fff;
  font-family: 'Songti SC', 'SimSun', serif;
  font-size: 32px;
  letter-spacing: 4px;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.form-content {
  padding: 30px 40px 40px;
}

.header-area {
  text-align: center;
  margin-bottom: 30px;
}

.sub-title {
  margin: 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
}

.login-form {
  margin-top: 10px;
}

:deep(.el-form-item__label) {
  padding-bottom: 8px;
  font-weight: 500;
  color: #606266;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #1c4e8e inset !important;
}

.login-button {
  width: 100%;
  background-color: #1c4e8e;
  border-color: #1c4e8e;
  font-weight: 500;
  letter-spacing: 2px;
  margin-top: 10px;
  height: 44px;
  font-size: 16px;
}

.login-button:hover, .login-button:focus {
  background-color: #163d72;
  border-color: #163d72;
}

.tips {
  margin-top: 24px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

.footer-info {
  margin-top: 24px;
  text-align: center;
  color: #909399;
  font-size: 12px;
}

@media (max-width: 480px) {
  .login-container {
    background-color: #fff;
    align-items: flex-start;
  }

  .login-card-wrapper {
    padding: 0;
    max-width: 100%;
    width: 100%;
  }

  .login-card {
    box-shadow: none;
    border-radius: 0;
  }

  .form-content {
    padding: 30px 24px;
  }

  .banner-area {
    height: 180px; /* 移动端Banner稍微高一点，更有视觉冲击力 */
  }

  .main-title {
    font-size: 28px;
  }

  .sub-title {
    font-size: 16px;
  }
  
  .footer-info {
    margin-bottom: 30px;
  }
}
</style>
