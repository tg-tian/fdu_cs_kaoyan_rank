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
      <p>本系统用于复旦大学计算机科学与技术学院考研分数统计排名查询，请务必阅读并理解以下内容：</p>
      <ul>
        <li>系统仅用于成绩查询与统计展示，最终结果以官方渠道为准。</li>
        <li>查询过程可能较慢，请勿重复提交或频繁刷新。</li>
        <li>请确保输入信息真实准确，错误信息可能导致查询失败。</li>
        <li>系统不会保存您的证件号码，查询完成后请及时退出。</li>
      </ul>
      <p>继续使用即表示您已知悉并同意上述内容。</p>
    </div>
    <template #footer>
      <div class="notice-footer">
        <span class="notice-tip">请滑动阅读至底部</span>
        <el-button type="primary" :disabled="!canClose" @click="handleClose">我已阅读</el-button>
      </div>
    </template>
  </el-dialog>
</template>

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
