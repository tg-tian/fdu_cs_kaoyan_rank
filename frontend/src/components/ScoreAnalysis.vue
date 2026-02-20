<template>
  <div class="analysis-card">
    <div class="stat-group">
      <h3>我的成绩分析</h3>
      <div class="stat-summary">
        <div class="stat-row">
          <span class="label">总分排名:</span>
          <span class="value">{{ myRank }} / {{ totalCount }}</span>
        </div>
        <div class="stat-row">
          <span class="label">超过考生:</span>
          <span class="value highlight">{{ percentile }}%</span>
        </div>
      </div>
    </div>
    
    <div class="stat-table">
      <div class="table-header">
        <span>科目</span>
        <span>我的分数</span>
        <span>平均分</span>
        <span>中位分</span>
      </div>
      <div class="table-row" v-for="item in statsData" :key="item.subject">
        <span class="subject">{{ item.subject }}</span>
        <span class="score" :class="{ 'above-avg': item.myScore >= item.average }">{{ item.myScore }}</span>
        <span class="avg">{{ item.average }}</span>
        <span class="median">{{ item.median }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ScoreItem } from '../entities/score'

// Helper interface for internal stats
interface SubjectStat {
  subject: string
  myScore: number
  average: number
  median: number
}

type SubjectKey = 'englishScore' | 'politicsScore' | 'mathScore' | 'score408' | 'total'

const props = defineProps<{
  allScores: ScoreItem[]
  myScore?: ScoreItem
}>()

// Helper to calculate median
const calculateMedian = (values: number[]) => {
  if (values.length === 0) return 0
  const sorted = [...values].sort((a, b) => a - b)
  const mid = Math.floor(sorted.length / 2)
  return sorted.length % 2 !== 0 ? (sorted[mid] ?? 0) : ((sorted[mid - 1] ?? 0) + (sorted[mid] ?? 0)) / 2
}

// Helper to calculate average
const calculateAverage = (values: number[]) => {
  if (values.length === 0) return 0
  const sum = values.reduce((a, b) => a + b, 0)
  return Math.round((sum / values.length) * 10) / 10 // 1 decimal place
}

const totalCount = computed(() => props.allScores.length)

const getTotal = (s: ScoreItem) => s.englishScore + s.politicsScore + s.mathScore + s.score408

const myRank = computed(() => {
  if (!props.myScore) return '-'
  const myTotal = getTotal(props.myScore)
  // Calculate rank: count how many people have higher score + 1
  const higherScores = props.allScores.filter(s => getTotal(s) > myTotal).length
  return higherScores + 1
})

const percentile = computed(() => {
  if (!props.myScore || totalCount.value === 0) return 0
  const myTotal = getTotal(props.myScore)
  const lowerScores = props.allScores.filter(s => getTotal(s) < myTotal).length
  return Math.round((lowerScores / totalCount.value) * 100)
})

const statsData = computed<SubjectStat[]>(() => {
  if (!props.myScore || props.allScores.length === 0) return []
  
  const subjects: { key: SubjectKey; name: string }[] = [
    { key: 'englishScore', name: '英语' },
    { key: 'politicsScore', name: '政治' },
    { key: 'mathScore', name: '数学' },
    { key: 'score408', name: '408' },
    { key: 'total', name: '总分' }
  ]
  
  return subjects.map(sub => {
    let values: number[]
    let myVal: number
    
    if (sub.key === 'total') {
      values = props.allScores.map(getTotal)
      myVal = getTotal(props.myScore!)
    } else {
      const key = sub.key as Exclude<SubjectKey, 'total'>
      values = props.allScores.map(s => s[key])
      myVal = props.myScore![key]
    }
    
    return {
      subject: sub.name,
      myScore: myVal,
      average: calculateAverage(values),
      median: calculateMedian(values)
    }
  })
})
</script>

<style scoped>
.analysis-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
}

.stat-group {
  margin-bottom: 24px;
}

.stat-group h3 {
  margin: 0 0 16px;
  font-size: 16px;
  color: #303133;
}

.stat-summary {
  display: flex;
  gap: 32px;
  justify-content: flex-start;
  flex-wrap: wrap;
}

.stat-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  margin-bottom: 0;
}

.stat-row .label {
  color: #606266;
}

.stat-row .value {
  font-weight: 600;
  color: #303133;
}

.stat-row .value.highlight {
  color: #e6a23c;
  font-size: 16px;
}

.stat-table {
  width: 100%;
  font-size: 13px;
}

.table-header {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1fr;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
  color: #909399;
  font-weight: 500;
  text-align: center;
}

.table-header span:first-child {
  text-align: left;
}

.table-row {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1fr;
  padding: 10px 0;
  border-bottom: 1px solid #f2f6fc;
  color: #606266;
  text-align: center;
}

.table-row:last-child {
  border-bottom: none;
}

.table-row .subject {
  text-align: left;
  font-weight: 500;
}

.table-row .score {
  font-weight: 600;
}

.table-row .score.above-avg {
  color: #67c23a;
}
</style>
