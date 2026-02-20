<template>
  <div class="analysis-container">
    <div class="chart-wrapper">
      <div ref="chartRef" style="width: 100%; height: 300px;"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { ScoreItem } from '../entities/score'

const props = defineProps<{
  allScores: ScoreItem[]
  myScore?: ScoreItem
}>()

const chartRef = ref<HTMLElement>()
let myChart: echarts.ECharts | null = null

const getTotal = (s: ScoreItem) => s.englishScore + s.politicsScore + s.mathScore + s.score408

const initChart = () => {
  if (!chartRef.value) return
  
  myChart = echarts.init(chartRef.value)
  
  const scores = props.allScores.map(getTotal)
  const myTotal = props.myScore ? getTotal(props.myScore) : null
  
  // Create bins of size 10
  const binSize = 10
  const maxScore = 500
  const minScore = 0
  const bins: number[] = []
  const binCounts: number[] = []
  
  for (let i = minScore; i < maxScore; i += binSize) {
    bins.push(i)
    binCounts.push(0)
  }
  
  // Fill bins
  scores.forEach(score => {
    const binIndex = Math.floor((score - minScore) / binSize)
    if (binIndex >= 0 && binIndex < bins.length) {
      binCounts[binIndex] = (binCounts[binIndex] ?? 0) + 1
    }
  })

  // Filter out empty bins from the start and end to make the chart cleaner
  let startIndex = 0
  let endIndex = bins.length - 1
  
  while (startIndex < bins.length && binCounts[startIndex] === 0) startIndex++
  while (endIndex >= 0 && binCounts[endIndex] === 0) endIndex--
  
  // Add some padding
  startIndex = Math.max(0, startIndex - 2)
  endIndex = Math.min(bins.length - 1, endIndex + 2)
  
  const displayBins = bins.slice(startIndex, endIndex + 1)
  const displayCounts = binCounts.slice(startIndex, endIndex + 1)
  const xAxisData = displayBins.map(b => `${b}-${b + binSize}`)
  
  // Find which bin my score is in
  let myBinIndex = -1
  if (myTotal !== null) {
    const rawBinIndex = Math.floor((myTotal - minScore) / binSize)
    myBinIndex = rawBinIndex - startIndex
  }

  const option = {
    title: {
      text: '分数分布直方图',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '人数',
        type: 'bar',
        barWidth: '60%',
        data: displayCounts.map((count, index) => {
          if (index === myBinIndex) {
            return {
              value: count,
              itemStyle: {
                color: '#e6a23c' // Highlight color for my score bin
              }
            }
          }
          return count
        }),
        itemStyle: {
          color: '#409eff'
        }
      }
    ]
  }

  myChart.setOption(option)
}

watch(() => props.allScores, () => {
  if (myChart) {
    myChart.dispose()
  }
  nextTick(() => {
    initChart()
  })
}, { deep: true })

onMounted(() => {
  nextTick(() => {
    initChart()
    window.addEventListener('resize', handleResize)
  })
})

const handleResize = () => {
  myChart?.resize()
}

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  myChart?.dispose()
})
</script>

<style scoped>
.analysis-container {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.chart-wrapper {
  width: 100%;
}
</style>
