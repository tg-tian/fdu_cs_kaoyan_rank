<template>
  <div class="login-container">
    <div class="login-card-wrapper">
      <el-card class="login-card" :body-style="{ padding: '0px' }">
        <div class="banner-area">
          <img src="/bg.jpg" alt="Fudan University" class="banner-img" />
        </div>
        
        <div class="form-content">
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
            <div v-if="loading && statusMessage" class="status-message">
              {{ statusMessage }}
            </div>
          </el-form>
        </div>
      </el-card>
    </div>
    <ReadNoticeDialog v-model="noticeVisible" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { loginAndGetToken } from '../api/user'
import type { LoginUpdate } from '../api/user'
import ReadNoticeDialog from '../components/ReadNoticeDialog.vue'
import { useRouter } from 'vue-router'

const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const noticeVisible = ref(true)
const router = useRouter()
const statusMessage = ref('')

const loginForm = reactive({
  candidateNumber: '',
  idNumber: ''
})

const normalizeValue = (value: unknown) => String(value ?? '').trim()

const validateCandidateNumber = (rule: any, value: any, callback: any) => {
  void rule
  const normalized = normalizeValue(value)
  if (!normalized) {
    callback(new Error('请输入考生编号'))
    return
  }
  if (!/^\d{15}$/.test(normalized)) {
    callback(new Error('考生编号通常为15位数字'))
    return
  }
  callback()
}

const validateIdNumber = (rule: any, value: any, callback: any) => {
  void rule
  const normalized = normalizeValue(value).toUpperCase()
  if (!normalized) {
    callback(new Error('请输入证件号码'))
    return
  }
  if (!/^\d{17}(\d|X)$/.test(normalized)) {
    callback(new Error('请输入正确的身份证号码'))
    return
  }
  callback()
}

const rules = reactive<FormRules>({
  candidateNumber: [
    { validator: validateCandidateNumber, trigger: 'blur' }
  ],
  idNumber: [
    { validator: validateIdNumber, trigger: 'blur' }
  ]
})

const handleLogin = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  
  await formEl.validate(async (valid, fields) => {
    if (valid) {
      loading.value = true
      statusMessage.value = '正在建立连接...'
      try {
        const token = await loginAndGetToken({
          examNo: loginForm.candidateNumber,
          idCard: loginForm.idNumber
        }, (update: LoginUpdate) => {
          console.log(update)
          if (update.type === 'queue') {
            statusMessage.value = `当前排队人数: ${update.data}`
          } else if (update.type === 'success') {
            statusMessage.value = '查询成绩成功，正在跳转...'
          } else if (update.type === 'error') {
            statusMessage.value = update.data || '发生错误'
          }
        }) 
        localStorage.setItem('token', token)
        ElMessage.success('登录成功')
        router.push('/home')
      } catch (error: any) {
        ElMessage.error(error.message || '查询失败，请稍后重试')
        statusMessage.value = ''
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
  align-items: flex-start;
  min-height: 100vh;
  width: 100%;
  background-color: #fff;
}

.login-card-wrapper {
  width: 100%;
  max-width: 100%;
  padding: 0;
}

.login-card {
  border: none;
  border-radius: 0;
  box-shadow: none;
  overflow: hidden;
  background-color: #fff;
}

.banner-area {
  position: relative;
  height: 180px;
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
  padding: 30px 24px;
}

.header-area {
  text-align: center;
  margin-bottom: 30px;
}

.sub-title {
  margin: 0;
  color: #333;
  font-size: 16px;
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

.status-message {
  margin-top: 12px;
  text-align: center;
  color: #409EFF;
  font-size: 14px;
  font-weight: 500;
}

.tips {
  margin-top: 24px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

.footer-info {
  margin-top: 24px;
  margin-bottom: 30px;
  text-align: center;
  color: #909399;
  font-size: 12px;
}

@media (min-width: 481px) {
  .login-container {
    align-items: center;
    background-color: #f5f7fa;
  }

  .login-card-wrapper {
    max-width: 440px;
    padding: 20px;
  }

  .login-card {
    border-radius: 8px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }

  .banner-area {
    height: 160px;
  }

  .form-content {
    padding: 30px 40px 40px;
  }

  .sub-title {
    font-size: 18px;
  }

  .footer-info {
    margin-bottom: 0;
  }
}
</style>
