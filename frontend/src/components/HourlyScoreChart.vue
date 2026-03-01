<template>
  <div class="chart-card">
    <h2 class="card-title">每小时录分人数</h2>
    <div class="chart-wrapper">
      <div ref="chartRef" style="width: 100%; height: 320px;"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import type { UserCreateTime } from '../api/user'

const props = defineProps<{
  createTimes: UserCreateTime[]
}>()

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const toUtc8Date = (value: string) => {
  const normalized = /[zZ]|[+-]\d{2}:?\d{2}$/.test(value) ? value : `${value}Z`
  const utcDate = new Date(normalized)
  if (Number.isNaN(utcDate.getTime())) return null
  return new Date(utcDate.getTime() + 8 * 60 * 60 * 1000)
}

const buildHourlySeries = (items: UserCreateTime[]) => {
  const offset = 8 * 60 * 60 * 1000
  const nowUtc8 = new Date(Date.now() + offset)
  const year = nowUtc8.getUTCFullYear()
  const startAt = new Date(Date.UTC(year, 1, 28, 9, 0, 0, 0))
  const endAt = nowUtc8
  const effectiveStart = endAt < startAt ? new Date(endAt) : startAt
  effectiveStart.setUTCMinutes(0, 0, 0)
  const buckets = new Map<number, number>()

  items.forEach(item => {
    const date = toUtc8Date(item.createdAt)
    if (!date) return
    if (date < effectiveStart || date > endAt) return
    date.setUTCMinutes(0, 0, 0)
    const key = date.getTime()
    buckets.set(key, (buckets.get(key) ?? 0) + 1)
  })

  const entries: { time: Date; count: number }[] = []
  const cursor = new Date(effectiveStart)
  while (cursor <= endAt) {
    const key = cursor.getTime()
    const count = buckets.get(key) ?? 0
    if (count > 0) {
      entries.push({ time: new Date(cursor), count })
    }
    cursor.setUTCHours(cursor.getUTCHours() + 1, 0, 0, 0)
  }

  const labels: string[] = []
  const points: { value: number; rangeLabel: string }[] = []
  const formatDate = (date: Date) => {
    const month = `${date.getUTCMonth() + 1}`.padStart(2, '0')
    const day = `${date.getUTCDate()}`.padStart(2, '0')
    const hour = `${date.getUTCHours()}`.padStart(2, '0')
    return { month, day, hour }
  }
  const buildRangeLabel = (start: Date, end: Date) => {
    const startParts = formatDate(start)
    const endParts = formatDate(end)
    return `${startParts.month}-${startParts.day} ${startParts.hour}:00 - ${endParts.month}-${endParts.day} ${endParts.hour}:00`
  }
  const buildAxisLabel = (start: Date, end: Date) => {
    const startParts = formatDate(start)
    const endParts = formatDate(end)
    if (startParts.month === endParts.month && startParts.day === endParts.day) {
      if (startParts.hour === endParts.hour) {
        return `${startParts.month}-${startParts.day}\n${startParts.hour}`
      }
      return `${startParts.month}-${startParts.day}\n${startParts.hour}-${endParts.hour}`
    }
    return `${startParts.month}-${startParts.day}\n${startParts.hour}`
  }

  for (let i = 0; i < entries.length; ) {
    const current = entries[i]
    if (!current) break
    if (current.count >= 50) {
      const end = new Date(current.time.getTime() + 60 * 60 * 1000)
      labels.push(buildAxisLabel(current.time, end))
      points.push({ value: current.count, rangeLabel: buildRangeLabel(current.time, end) })
      i += 1
      continue
    }
    let countSum = 0
    const start = current.time
    let end = new Date(current.time.getTime() + 60 * 60 * 1000)
    let merged = 0
    while (i < entries.length && merged < 3) {
      const entry = entries[i]
      if (!entry || entry.count >= 50) break
      countSum += entry.count
      end = new Date(entry.time.getTime() + 60 * 60 * 1000)
      i += 1
      merged += 1
    }
    labels.push(buildAxisLabel(start, end))
    points.push({ value: countSum, rangeLabel: buildRangeLabel(start, end) })
  }

  return { labels, points }
}

const initChart = () => {
  if (!chartRef.value) return
  if (chart) chart.dispose()
  chart = echarts.init(chartRef.value)
  const { labels, points } = buildHourlySeries(props.createTimes)
  const labelInterval = labels.length > 12 ? Math.ceil(labels.length / 12) - 1 : 0
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any[]) => {
        const first = params?.[0]
        if (!first) return ''
        const rangeLabel = first.data?.rangeLabel || first.axisValue
        return `<div style="padding:4px 6px;"><div style="font-weight:600;margin-bottom:4px;">${rangeLabel}</div><div>录分人数：${first.value}</div></div>`
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
      data: labels,
      axisLabel: {
        interval: labelInterval,
        formatter: (value: string) => value,
        hideOverlap: true
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '录分人数',
        type: 'line',
        smooth: true,
        data: points,
        itemStyle: { color: '#409EFF' }
      }
    ]
  }
  chart.setOption(option)
}

const handleResize = () => {
  chart?.resize()
}

watch(
  () => props.createTimes,
  () => {
    nextTick(() => {
      initChart()
    })
  },
  { deep: true }
)

onMounted(() => {
  nextTick(() => {
    initChart()
    window.addEventListener('resize', handleResize)
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style scoped>
.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
}

.chart-wrapper {
  width: 100%;
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
</style>
