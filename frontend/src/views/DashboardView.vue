<template>
  <DashboardLayout v-model:month="currentMonth">
    <div class="dashboard-content">
      <!-- Stats Cards -->
      <el-row :gutter="20" class="stats-grid">
        <el-col :xs="24" :sm="8">
          <el-card class="stat-card card-glass" shadow="hover">
            <div class="stat-content">
              <div class="icon-wrapper income">💰</div>
              <div class="info">
                <span class="label">本月收入</span>
                <span class="value income">+¥{{ monthlyStats.income.toLocaleString() }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-card class="stat-card card-glass" shadow="hover">
            <div class="stat-content">
              <div class="icon-wrapper expense">💸</div>
              <div class="info">
                <span class="label">本月支出</span>
                <span class="value expense">-¥{{ monthlyStats.expense.toLocaleString() }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8">
          <el-card class="stat-card card-glass" shadow="hover">
            <div class="stat-content">
              <div class="icon-wrapper balance">🏦</div>
              <div class="info">
                <span class="label">结余</span>
                <span class="value balance">¥{{ (monthlyStats.income - monthlyStats.expense).toLocaleString() }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Month Picker Removed (Moved to Layout) -->

      <!-- Charts Area -->
      <el-row :gutter="20" class="charts-grid">
        <el-col :xs="24" :md="16">
          <el-card class="chart-card card-glass" shadow="never">
            <template #header>
              <div class="card-header">
                <span>收支趋势</span>
              </div>
            </template>
            <div ref="trendChart" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :xs="24" :md="8">
          <el-card class="chart-card card-glass" shadow="never">
            <template #header>
              <div class="card-header">
                <span>支出分类占比</span>
              </div>
            </template>
            <div ref="pieChart" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Recent Records -->
      <el-card class="recent-records card-glass" shadow="never">
        <template #header>
          <div class="card-header">
            <span>最近记录</span>
            <el-button type="primary" size="default" @click="showAddModal = true" class="add-record-btn">
              <el-icon class="el-icon--left"><Plus /></el-icon>记一笔
            </el-button>
          </div>
        </template>
        
        <div class="records-list">
          <div v-for="record in recentRecords" :key="record.id" class="record-item">
            <div class="left">
              <div class="category-icon">{{ record.category?.icon || '📝' }}</div>
              <div class="details">
                <span class="category-name">{{ record.category?.name || '一般' }}</span>
                <span class="date">{{ record.recordDate }}</span>
              </div>
            </div>
            <div class="right">
              <span :class="['amount', record.type.toLowerCase()]">
                {{ record.type === 'INCOME' ? '+' : '-' }}¥{{ record.amount }}
              </span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <AddRecordModal
      v-model:is-open="showAddModal"
      :categories="categories"
      :user-id="userStore.user?.id || 0"
      @submit="handleAddRecord"
    />
  </DashboardLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import AddRecordModal from '@/components/AddRecordModal.vue'
import * as echarts from 'echarts'
import { useUserStore } from '@/stores/user'
import { recordApi } from '@/api/record'
import { categoryApi } from '@/api/category'
import { Plus } from '@element-plus/icons-vue'
import type { Record as TransactionRecord, Category } from '@/types'

const userStore = useUserStore()
const showAddModal = ref(false)
const trendChart = ref<HTMLElement | null>(null)
const pieChart = ref<HTMLElement | null>(null)
const categories = ref<Category[]>([])
const records = ref<TransactionRecord[]>([])
const currentMonth = ref(new Date())

// Watch removed from here, moved to onMounted
// Stats
const monthlyStats = computed(() => {
  let income = 0
  let expense = 0
  records.value.forEach(r => {
    if (r.type === 'INCOME') income += r.amount
    else expense += r.amount
  })
  return { income, expense }
})

const recentRecords = computed(() => {
  return [...records.value].sort((a, b) => new Date(b.recordDate).getTime() - new Date(a.recordDate).getTime()).slice(0, 10)
})

onMounted(async () => {
  if (userStore.user?.id) {
    // Initial fetch
    await fetchData()
    
    // Watch for month changes
    watch(currentMonth, async () => {
      await fetchData()
    })
  }
})

const fetchData = async () => {
  try {
    const userId = userStore.user!.id
    const now = currentMonth.value
    // Use local date string construction to avoid timezone issues with toISOString()
    const start = new Date(now.getFullYear(), now.getMonth(), 1)
    const end = new Date(now.getFullYear(), now.getMonth() + 1, 0)
    
    const formatDate = (d: Date) => {
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }

    const startDate = formatDate(start)
    const endDate = formatDate(end)

    const [catsRes, recsRes] = await Promise.all([
      categoryApi.getCategories(userId),
      recordApi.getRecords(userId, startDate, endDate)
    ])

  categories.value = catsRes.data.data
  records.value = recsRes.data.data

  initCharts()
  } catch (error) {
    console.error('Failed to fetch data', error)
  }
}

const handleAddRecord = async (record: Partial<TransactionRecord>) => {
  try {
    await recordApi.createRecord(record)
    await fetchData() // Refresh data
  } catch (error) {
    console.error('Failed to create record', error)
  }
}

const initCharts = () => {
  if (!trendChart.value || !pieChart.value) return
  
  // Group records by date for trend chart
  const incomeByDate: Record<string, number> = {}
  const expenseByDate: Record<string, number> = {}
  const dates = new Set<string>()

  // Sort records by date ascending
  const sortedRecords = [...records.value].sort((a, b) => 
    new Date(a.recordDate).getTime() - new Date(b.recordDate).getTime()
  )

  sortedRecords.forEach(r => {
    dates.add(r.recordDate)
    if (r.type === 'INCOME') {
      incomeByDate[r.recordDate] = (incomeByDate[r.recordDate] || 0) + r.amount
    } else {
      expenseByDate[r.recordDate] = (expenseByDate[r.recordDate] || 0) + r.amount
    }
  })

  const dateList = Array.from(dates).sort()
  const incomeData = dateList.map(date => incomeByDate[date] || 0)
  const expenseData = dateList.map(date => expenseByDate[date] || 0)

  // Trend Chart Logic
  const chart1 = echarts.init(trendChart.value)
  chart1.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'axis' },
      legend: {
        data: ['收入', '支出'],
        textStyle: { color: '#94A3B8' },
        bottom: 0
      },
      grid: { left: '3%', right: '4%', bottom: '10%', top: '10%', containLabel: true },
      xAxis: {
        type: 'category',
        data: dateList,
        axisLine: { lineStyle: { color: '#94A3B8' } },
        axisLabel: { color: '#94A3B8' }
      },
      yAxis: {
        type: 'value',
        axisLine: { lineStyle: { color: '#94A3B8' } },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
        axisLabel: { color: '#94A3B8' }
      },
      series: [
        {
          name: '收入',
          type: 'line',
          smooth: true,
          data: incomeData,
          itemStyle: { color: '#10B981' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(16, 185, 129, 0.5)' },
              { offset: 1, color: 'rgba(16, 185, 129, 0.0)' }
            ])
          }
        },
        {
          name: '支出',
          type: 'line',
          smooth: true,
          data: expenseData,
          itemStyle: { color: '#FBBF24' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(251, 191, 36, 0.5)' },
              { offset: 1, color: 'rgba(251, 191, 36, 0.0)' }
            ])
          }
        }
      ]
  })

  // Pie Chart Logic
  const expenseByCat = {} as Record<string, number>
  records.value.filter(r => r.type === 'EXPENSE').forEach(r => {
    const catName = r.category?.name || '其他'
    expenseByCat[catName] = (expenseByCat[catName] || 0) + r.amount
  })
  
  const pieData = Object.entries(expenseByCat).map(([name, value]) => ({ name, value }))

  const chart2 = echarts.init(pieChart.value)
  chart2.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'item' },
      series: [
        {
          name: '支出分类',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#1e293b',
            borderWidth: 2
          },
          label: { 
            show: false
          },
          labelLine: {
            show: false
          },
          data: pieData.length ? pieData : [{ value: 0, name: '暂无数据' }]
        }
      ]
  })
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stat-card {
  background: $card-bg !important;
  border: 1px solid $card-border !important;
  border-radius: 16px !important;
  
  .stat-content {
    display: flex;
    align-items: center;
    gap: 20px;
  }
  
  .icon-wrapper {
    width: 56px;
    height: 56px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    background: rgba(255, 255, 255, 0.05);
    
    &.income { background: rgba(16, 185, 129, 0.2); color: #10B981; }
    &.expense { background: rgba(239, 68, 68, 0.2); color: #EF4444; }
    &.balance { background: rgba(59, 130, 246, 0.2); color: #3B82F6; }
  }
  
  .info {
    display: flex;
    flex-direction: column;
    
    .label {
      color: $text-muted;
      font-size: 0.9rem;
      margin-bottom: 4px;
    }
    
    .value {
      font-size: 1.5rem;
      font-weight: 600;
      font-family: $font-family-display;
      
      &.income { color: #10B981; }
      &.expense { color: #EF4444; }
      &.balance { color: #22D3EE; text-shadow: 0 0 10px rgba(34, 211, 238, 0.3); }
    }
  }
}

.chart-card, .recent-records {
  background: $card-bg !important;
  border: 1px solid $card-border !important;
  border-radius: 16px !important;
  
  :deep(.el-card__header) {
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    padding: 16px 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 500;
    color: $text-muted;
  }
  
  .chart-container {
    height: 300px;
    width: 100%;
  }
}

.records-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    
    .record-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      background: rgba(255, 255, 255, 0.03);
      border-radius: 12px;
      transition: background 0.2s;
      
      &:hover {
        background: rgba(255, 255, 255, 0.05);
      }
      
      .left {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .category-icon {
          width: 40px;
          height: 40px;
          border-radius: 10px;
          background: rgba(255, 255, 255, 0.05);
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 20px;
        }
        
        .details {
          display: flex;
          flex-direction: column;
          gap: 4px;
          
          .category-name {
            font-weight: 500;
            color: #FCD34D;
          }
          
          .date {
            font-size: 0.8rem;
            color: $text-muted;
          }
        }
      }
      
      .right {
        .amount {
          font-family: $font-family-display;
          font-weight: 600;
          
          &.income { color: #10B981; }
          &.expense { color: #EF4444; }
        }
      }
    }
  }

.add-record-btn {
  background: linear-gradient(135deg, $cta-color, darken($cta-color, 10%)) !important;
  border: none !important;
  font-weight: 600;
  box-shadow: 0 4px 6px -1px rgba(139, 92, 246, 0.3);
  transition: all 0.2s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 10px -3px rgba(139, 92, 246, 0.4);
    background: linear-gradient(135deg, lighten($cta-color, 5%), $cta-color) !important;
  }
}

.month-picker-container {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.glass-date-picker {
  :deep(.el-input__wrapper) {
    background-color: rgba(255, 255, 255, 0.05);
    box-shadow: none;
    border: 1px solid rgba(255, 255, 255, 0.1);
    
    &:hover {
      border-color: rgba(255, 255, 255, 0.2);
    }
  }
  
  :deep(.el-input__inner) {
    color: $text-color;
  }
}
</style>
