<template>
  <div class="home-container">
    <div class="home-content">
      <el-dialog
        v-model="showCommentNotice"
        width="520px"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
        align-center
      >
        <template #header>
          <div class="notice-title">提示</div>
        </template>
        <div class="comment-notice">
          <p>页面底部有评论区和公告。</p>
          <p>由于使用 GitHub 服务，加载可能会有一定延迟。</p>
        </div>
        <template #footer>
          <el-button type="primary" @click="handleCommentNoticeClose">知道了</el-button>
        </template>
      </el-dialog>
      <h1 class="title">复旦CS考研录分网站</h1>
      <div class="notice-card">
        <p class="notice-title">复试说明：</p>
        <p class="notice-content">初试成绩占50%、复试成绩占50%。复试成绩中，专业能力考查成绩占90%、外国语听力与口语占10%。</p>
      </div>
      
      <div v-if="myScore" class="score-card">
        <h2 class="card-title">我的成绩</h2>
        <div class="score-grid">
          <div class="score-item">
            <span class="score-label">总分</span>
            <span class="score-value highlight">{{ totalScore }}</span>
          </div>
          <div class="score-item">
            <span class="score-label">英语</span>
            <span class="score-value">{{ myScore.englishScore }}</span>
          </div>
          <div class="score-item">
            <span class="score-label">政治</span>
            <span class="score-value">{{ myScore.politicsScore }}</span>
          </div>
          <div class="score-item">
            <span class="score-label">数学</span>
            <span class="score-value">{{ myScore.mathScore }}</span>
          </div>
          <div class="score-item">
            <span class="score-label">408</span>
            <span class="score-value">{{ myScore.score408 }}</span>
          </div>
        </div>
      </div>

      <ScoreAnalysis 
        v-if="scores.length > 0 && myScore" 
        :all-scores="scores" 
        :my-score="myScore" 
      />
      <ScoreHistogram 
        v-if="scores.length > 0" 
        :all-scores="scores" 
        :my-score="myScore || undefined" 
      />

      <div class="score-list-card">
        <h2 class="card-title">
          <span>成绩列表</span>
          <span class="score-count">系统收录成绩{{ scores.length }}条</span>
        </h2>
        <el-table 
          :data="pagedScores" 
          style="width: 100%" 
          v-loading="loading"
          :default-sort="{ prop: 'totalScore', order: 'descending' }"
          @sort-change="handleSortChange"
        >
          <el-table-column label="排名" width="80" align="center">
            <template #default="{ $index }">
              <span class="rank-badge" :class="getRankClass($index)">
                {{ (currentPage - 1) * pageSize + $index + 1 }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="englishScore" label="英语" sortable="custom" align="center" :sort-orders="['descending', 'ascending']" />
          <el-table-column prop="politicsScore" label="政治" sortable="custom" align="center" :sort-orders="['descending', 'ascending']" />
          <el-table-column prop="mathScore" label="数学" sortable="custom" align="center" :sort-orders="['descending', 'ascending']" />
          <el-table-column prop="score408" label="408" sortable="custom" align="center" :sort-orders="['descending', 'ascending']" />
          <el-table-column prop="totalScore" label="总分" sortable="custom" align="center" width="100" :sort-orders="['descending', 'ascending']">
            <template #default="{ row }">
              <span class="total-score">{{ row.totalScore }}</span>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            layout="prev, pager, next"
            :total="sortedScores.length"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
      
      <HistoryScoreLines />
      <div ref="giscusRef" class="giscus-container"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getAllScores, getMyScore } from '../api/score'
import type { ScoreItem } from '../entities/score'
import { ElMessage } from 'element-plus'
import ScoreHistogram from '../components/ScoreHistogram.vue'
import ScoreAnalysis from '../components/ScoreAnalysis.vue'
import HistoryScoreLines from '../components/HistoryScoreLines.vue'

const router = useRouter()
const scores = ref<ScoreItem[]>([])
const myScore = ref<ScoreItem | null>(null)
const loading = ref(false)
const giscusRef = ref<HTMLElement | null>(null)
const showCommentNotice = ref(false)
const commentNoticeStorageKey = 'home-comment-notice-shown'

// Pagination and Sorting state
const currentPage = ref(1)
const pageSize = ref(10)
const sortProp = ref('totalScore')
const sortOrder = ref('descending')

// Compute total score for each item and add it to the list
const scoresWithTotal = computed(() => {
  return scores.value.map(score => ({
    ...score,
    totalScore: score.englishScore + score.politicsScore + score.mathScore + score.score408
  }))
})

// Sort scores based on current sort state
const sortedScores = computed(() => {
  const list = [...scoresWithTotal.value]
  const prop = sortProp.value
  // When order is null (unsorted), default to descending for totalScore, or just use list order
  if (!sortOrder.value) return list
  
  const order = sortOrder.value === 'descending' ? -1 : 1
  
  return list.sort((a, b) => {
    // @ts-ignore - dynamic property access
    if (a[prop] < b[prop]) return -1 * order
    // @ts-ignore
    if (a[prop] > b[prop]) return 1 * order
    return 0
  })
})

// Get current page scores
const pagedScores = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return sortedScores.value.slice(start, end)
})

const handleSortChange = ({ prop, order }: { prop: string, order: string }) => {
  sortProp.value = prop
  sortOrder.value = order
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

const getRankClass = (index: number) => {
  const rank = (currentPage.value - 1) * pageSize.value + index + 1
  if (rank === 1) return 'rank-1'
  if (rank === 2) return 'rank-2'
  if (rank === 3) return 'rank-3'
  return ''
}

const totalScore = computed(() => {
  if (!myScore.value) return 0
  return (
    myScore.value.englishScore +
    myScore.value.politicsScore +
    myScore.value.mathScore +
    myScore.value.score408
  )
})

const handleCommentNoticeClose = () => {
  showCommentNotice.value = false
  localStorage.setItem(commentNoticeStorageKey, '1')
}

onMounted(async () => {
  if (!localStorage.getItem(commentNoticeStorageKey)) {
    showCommentNotice.value = true
  }
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  loading.value = true
  try {
    const [allScores, me] = await Promise.all([
      getAllScores(),
      getMyScore()
    ])
    scores.value = allScores
    myScore.value = me
    console.log('Data loaded:', { allScores, me })
  } catch (error: any) {
    ElMessage.error(error.message || '获取数据失败')
  } finally {
    loading.value = false
  }

  await nextTick()
  if (!giscusRef.value) return
  if (giscusRef.value.querySelector('iframe')) return
  const script = document.createElement('script')
  script.src = 'https://giscus.app/client.js'
  script.async = true
  script.crossOrigin = 'anonymous'
  script.setAttribute('data-repo', 'tg-tian/fdu_cs_kaoyan_rank')
  script.setAttribute('data-repo-id', 'R_kgDORRUmzA')
  script.setAttribute('data-category', 'General')
  script.setAttribute('data-category-id', 'DIC_kwDORRUmzM4C3Zf7')
  script.setAttribute('data-mapping', 'pathname')
  script.setAttribute('data-strict', '1')
  script.setAttribute('data-reactions-enabled', '1')
  script.setAttribute('data-emit-metadata', '0')
  script.setAttribute('data-input-position', 'top')
  script.setAttribute('data-theme', 'preferred_color_scheme')
  script.setAttribute('data-lang', 'en')
  script.setAttribute('data-loading', 'lazy')
  giscusRef.value.appendChild(script)
})

</script>

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
  margin: 0 0 16px;
  color: #1c4e8e;
  font-size: 28px;
  letter-spacing: 1px;
}

.notice-card {
  background: #fdf6ec;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 24px;
  border: 1px solid #faecd8;
  text-align: left;
}

.notice-title {
  color: #e6a23c;
  font-weight: 600;
  margin: 0 0 4px;
  font-size: 14px;
}

.notice-content {
  color: #606266;
  font-size: 13px;
  margin: 0;
  line-height: 1.6;
}

.score-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
}

.card-title {
  margin: 0 0 16px;
  font-size: 18px;
  color: #303133;
  font-weight: 600;
  text-align: left;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.score-count {
  font-size: 14px;
  color: #909399;
  font-weight: normal;
}

.score-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.score-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background-color: #f8fafc;
  border-radius: 8px;
}

.score-label {
  font-size: 13px;
  color: #909399;
}

.score-value {
  font-size: 18px;
  color: #303133;
  font-weight: 600;
  font-family: 'Roboto', sans-serif;
}

.score-value.highlight {
  color: #1c4e8e;
  font-size: 20px;
  font-weight: 700;
}

.score-list-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
  text-align: left;
}

.total-score {
  font-weight: 700;
  color: #1c4e8e;
}

.rank-badge {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  border-radius: 50%;
  font-weight: bold;
  font-size: 12px;
}

.rank-1 {
  background-color: #ffd700;
  color: #fff;
}

.rank-2 {
  background-color: #c0c0c0;
  color: #fff;
}

.rank-3 {
  background-color: #cd7f32;
  color: #fff;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.giscus-container {
  margin-top: 24px;
}

.comment-notice {
  line-height: 1.7;
  color: #606266;
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
