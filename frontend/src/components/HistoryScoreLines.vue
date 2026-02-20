<template>
  <div class="history-card">
    <h2 class="card-title">历年复试分数线</h2>
    <div class="chart-wrapper">
      <div ref="chartRef" style="width: 100%; height: 350px;"></div>
    </div>
    <div class="table-wrapper">
      <el-table :data="historyData" style="width: 100%" size="small">
        <el-table-column prop="year" label="年份" align="center" />
        <el-table-column prop="cs" label="计算机学院" align="center" />
        <el-table-column prop="se" label="软件学院" align="center" />
      </el-table>
    </div>
    <div class="history-note">
      <p>计算与智能创新学院2026年前分为计算机和软件学院分别招生, 历年单科线都是50、50、75、75。</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

// Data based on user input:
// CS: 2025: 343, 2024: 345, 2023: 359, 2022: 352, 2021: 355
// SE: 2025: 343, 2024: 334, 2023: 357, 2022: 343, 2021: 350
const historyData = [
  { year: '2025', cs: 343, se: 343 },
  { year: '2024', cs: 345, se: 334 },
  { year: '2023', cs: 359, se: 357 },
  { year: '2022', cs: 352, se: 343 },
  { year: '2021', cs: 355, se: 350 }
]

const chartRef = ref<HTMLElement>()
let myChart: echarts.ECharts | null = null

const initChart = () => {
  if (!chartRef.value) return
  
  myChart = echarts.init(chartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['计算机学院', '软件学院'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: historyData.map(item => item.year).reverse() // Show chronological order 2021 -> 2025
    },
    yAxis: {
      type: 'value',
      name: '分数',
      min: 320,
      max: 380,
      interval: 10
    },
    series: [
      {
        name: '计算机学院',
        type: 'line',
        data: historyData.map(item => item.cs).reverse(),
        itemStyle: { color: '#409EFF' },
        label: { show: true, position: 'top' },
        smooth: true
      },
      {
        name: '软件学院',
        type: 'line',
        data: historyData.map(item => item.se).reverse(),
        itemStyle: { color: '#67C23A' },
        label: { show: true, position: 'top' },
        smooth: true
      }
    ]
  }

  myChart.setOption(option)
}

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
.history-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-top: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
}

.card-title {
  margin: 0 0 20px;
  font-size: 18px;
  color: #303133;
  font-weight: 600;
  text-align: left;
}

.chart-wrapper {
  margin-bottom: 24px;
}

.table-wrapper {
  margin-top: 16px;
}

.history-note {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #ebeef5;
  color: #909399;
  font-size: 13px;
  text-align: left;
  line-height: 1.6;
}

.history-note p {
  margin: 0;
}
</style>
