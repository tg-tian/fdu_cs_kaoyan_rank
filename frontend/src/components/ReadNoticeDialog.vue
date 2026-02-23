<template>
  <el-dialog
    v-model="visible"
    class="notice-dialog"
    width="520px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
    align-center
  >
    <template #header>
      <div class="notice-title">必读提示</div>
    </template>
    <div ref="contentRef" class="notice-content" @scroll="handleScroll">
      <p>本系统用于复旦大学计算与智能创新学院考研分数统计排名查询，请务必阅读并理解以下内容：</p>
      <ul>
        <li>系统将使用用户提供的信息前往官方网站查询成绩。所有用户信息在入库前均经过 <strong>SHA-256 单向加密处理</strong>，<strong>数据库中不存储任何明文数据</strong>，<strong>管理员亦无法查看用户原始信息</strong>。</li>
        <li>由于查询需实时访问官方网站，过程可能存在一定延迟。<strong>请勿重复提交或频繁刷新页面</strong>，以免影响查询结果。</li>
        <li>
          开发不易，如果本项目对您有帮助，欢迎在
          <a href="https://github.com/tg-tian/fdu_cs_kaoyan_rank" target="_blank" class="notice-link">
            <svg height="16" width="16" viewBox="0 0 16 16" version="1.1" class="github-icon" aria-hidden="true">
              <path fill-rule="evenodd" d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"></path>
            </svg>
            GitHub
          </a> 上点个 Star 支持一下😄
        </li>
        <li>
          也欢迎对技术开发感兴趣的同学了解我们团队。
          老师人不错，毕业去向也很好，
          <a href="https://faculty.fudan.edu.cn/wuyijian/zh_CN/index.htm" target="_blank" class="notice-link">
            <svg viewBox="0 0 1024 1024" version="1.1" width="16" height="16" class="email-icon">
              <path d="M928 160H96c-17.7 0-32 14.3-32 32v640c0 17.7 14.3 32 32 32h832c17.7 0 32-14.3 32-32V192c0-17.7-14.3-32-32-32zM512 496.8L136.6 232h750.8L512 496.8zM128 299.2l230.2 162.2L128 653.6V299.2z m30.4 496.8l248.2-206.4L512 664.8l105.4-75.2 248.2 206.4H158.4z m737.6-142.4L665.8 461.4 896 299.2v354.4z" fill="currentColor"></path>
            </svg>欢迎联系
          </a>。😄
        </li>
      </ul>
      <p>继续使用即表示您已知悉上述内容。</p>
    </div>
    <template #footer>
      <div class="notice-footer">
        <span class="notice-tip">请滑动阅读至底部</span>
        <el-button type="primary" :disabled="!canClose" @click="handleClose">我已阅读</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.notice-link {
  color: #409eff;
  text-decoration: none;
  font-weight: bold;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  vertical-align: middle;
}
.notice-link:hover {
  text-decoration: underline;
}
.github-icon, .email-icon {
  fill: currentColor;
  margin-right: 2px;
}
</style>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{ (e: 'update:modelValue', value: boolean): void }>()

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const contentRef = ref<HTMLElement | null>(null)
const canClose = ref(false)

const resetState = async () => {
  canClose.value = false
  await nextTick()
  const el = contentRef.value
  if (!el) return
  el.scrollTop = 0
  if (el.scrollHeight <= el.clientHeight + 2) {
    canClose.value = true
  }
}

const updateCanClose = () => {
  const el = contentRef.value
  if (!el) return
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - 2) {
    canClose.value = true
  }
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) resetState()
  },
  { immediate: true }
)

const handleScroll = () => {
  updateCanClose()
}

const handleClose = () => {
  if (!canClose.value) return
  visible.value = false
}
</script>

<style scoped>
.notice-title {
  font-size: 18px;
  font-weight: 600;
  color: #1c4e8e;
  letter-spacing: 1px;
}

.notice-content {
  max-height: 320px;
  overflow: auto;
  padding-right: 6px;
  line-height: 1.7;
  color: #2c3e50;
}

.notice-content ul {
  padding-left: 18px;
  margin: 12px 0;
}

.notice-content li {
  margin: 6px 0;
}

.notice-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.notice-tip {
  font-size: 12px;
  color: #909399;
}

.notice-dialog {
  max-width: 92vw;
}
</style>
