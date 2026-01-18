<template>
  <DashboardLayout>
    <div class="records-view">
      <el-card class="card-glass" shadow="never">
        <template #header>
          <div class="card-header">
            <div class="left">
              <h3>账单记录</h3>
              <div class="date-picker-wrapper">
                <CryptoDateRangePicker
                  v-model="dateRange"
                  @change="fetchRecords"
                />
              </div>
              <div class="balance-display">
                <span class="label">结余:</span>
                <span class="value" :class="{ 'positive': totalBalance >= 0, 'negative': totalBalance < 0 }">
                  ¥{{ totalBalance.toLocaleString() }}
                </span>
              </div>
            </div>
            <el-button type="primary" @click="showAddModal = true" class="add-btn">
              <el-icon class="el-icon--left"><Plus /></el-icon>记一笔
            </el-button>
          </div>
        </template>

        <div v-loading="loading" class="record-list">
          <template v-if="records.length > 0">
            <div 
              v-for="record in records" 
              :key="record.id" 
              class="record-row"
            >
              <div class="left-section">
                <div class="icon-wrapper">{{ record.category?.icon || '📝' }}</div>
                <div class="info">
                  <span class="category">{{ record.category?.name || '其他' }}</span>
                  <span class="date">{{ record.recordDate }}</span>
                </div>
              </div>
              
              <div class="middle-section">
                <span class="desc">{{ record.description }}</span>
              </div>
              
              <div class="right-section">
                <span :class="['amount', record.type.toLowerCase()]">
                  {{ record.type === 'INCOME' ? '+' : '-' }}¥{{ record.amount.toLocaleString() }}
                </span>
                <el-button 
                  link 
                  class="delete-btn"
                  @click="handleDelete(record)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </template>
          <el-empty v-else description="暂无记录" :image-size="100" />
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
import { ref, onMounted, inject, computed } from 'vue'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import AddRecordModal from '@/components/AddRecordModal.vue'
import CryptoDateRangePicker from '@/components/ui/CryptoDateRangePicker.vue'
import { useUserStore } from '@/stores/user'
import { recordApi } from '@/api/record'
import { categoryApi } from '@/api/category'
import { Plus, Delete } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import type { Record, Category } from '@/types'

const userStore = useUserStore()
const toast = inject<any>('toast')

const records = ref<Record[]>([])
const categories = ref<Category[]>([])
const loading = ref(false)
const showAddModal = ref(false)

const now = new Date()
const start = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0] || ''
const end = new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString().split('T')[0] || ''
const dateRange = ref<string[]>([start, end])

const totalBalance = computed(() => {
  let income = 0
  let expense = 0
  records.value.forEach(r => {
    if (r.type === 'INCOME') income += r.amount
    else expense += r.amount
  })
  return income - expense
})

onMounted(async () => {
  if (userStore.user?.id) {
    await Promise.all([
      fetchCategories(),
      fetchRecords()
    ])
  }
})

const fetchCategories = async () => {
  try {
    const res = await categoryApi.getCategories(userStore.user!.id)
    categories.value = res.data.data
  } catch (error) {
    console.error('Failed to fetch categories', error)
  }
}

const fetchRecords = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) return
  const [start, end] = dateRange.value
  if (!start || !end) return
  
  loading.value = true
  try {
    const res = await recordApi.getRecords(userStore.user!.id, start, end)
    records.value = res.data.data
  } catch (error) {
    toast('获取记录失败', 'error')
  } finally {
    loading.value = false
  }
}

const handleAddRecord = async (record: Partial<Record>) => {
  try {
    await recordApi.createRecord(record)
    toast('记账成功', 'success')
    fetchRecords()
  } catch (error) {
    toast('记账失败', 'error')
  }
}

const handleDelete = (row: Record) => {
  ElMessageBox.confirm(
    '确定要删除这条记录吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await recordApi.deleteRecord(row.id)
      toast('删除成功', 'success')
      fetchRecords()
    } catch (error) {
      toast('删除失败', 'error')
    }
  })
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.records-view {
  height: 100%;
}

.card-glass {
  background: $card-bg !important;
  border: 1px solid $card-border !important;
  border-radius: 16px !important;
  height: 100%;
  display: flex;
  flex-direction: column;
  
  :deep(.el-card__header) {
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    padding: 16px 20px;
  }
  
  :deep(.el-card__body) {
    flex: 1;
    overflow: hidden;
    padding: 0;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .left {
    display: flex;
    align-items: center;
    gap: 20px;
    
    h3 {
      margin: 0;
      color: $text-muted;
      font-weight: 500;
    }
  }
}

.date-picker-wrapper {
    width: 260px;
  }
  
  .balance-display {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-left: 20px;
    padding: 6px 16px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    
    .label {
      color: $text-muted;
      font-size: 0.9rem;
    }
    
    .value {
      font-family: $font-family-display;
      font-weight: 600;
      font-size: 1.1rem;
      
      &.positive { color: #22D3EE; text-shadow: 0 0 10px rgba(34, 211, 238, 0.3); }
      &.negative { color: #EF4444; }
    }
  }

.custom-table {
  background: transparent !important;
  --el-table-border-color: rgba(255, 255, 255, 0.05);
  --el-table-header-bg-color: rgba(255, 255, 255, 0.02);
  --el-table-row-hover-bg-color: rgba(255, 255, 255, 0.05);
  --el-table-text-color: #{$text-color};
  --el-table-header-text-color: #{$text-muted};
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  
  :deep(th.el-table__cell) {
    background: rgba(255, 255, 255, 0.02) !important;
  }
  
  :deep(td.el-table__cell) {
    border-bottom: 1px solid rgba(255, 255, 255, 0.05) !important;
  }
}

.category-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .icon {
    font-size: 1.2rem;
  }
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 10px;
}

.record-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  transition: all 0.2s;
  
  &:hover {
    transform: translateY(-2px);
    background: rgba(255, 255, 255, 0.06);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  .left-section {
    display: flex;
    align-items: center;
    gap: 16px;
    width: 200px;
    
    .icon-wrapper {
      width: 44px;
      height: 44px;
      border-radius: 12px;
      background: rgba(255, 255, 255, 0.05);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 22px;
    }
    
    .info {
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      .category {
        font-weight: 500;
        font-size: 1rem;
        color: #FCD34D;
      }
      
      .date {
        font-size: 0.8rem;
        color: $text-muted;
      }
    }
  }
  
  .middle-section {
    flex: 1;
    color: $text-muted;
    font-size: 0.95rem;
    padding: 0 20px;
  }
  
  .right-section {
    display: flex;
    align-items: center;
    gap: 20px;
    
    .amount {
      font-family: $font-family-display;
      font-weight: 600;
      font-size: 1.1rem;
      min-width: 100px;
      text-align: right;
      
      &.income { color: #10B981; }
      &.expense { color: #EF4444; }
    }
    
    .delete-btn {
      color: $text-muted;
      padding: 8px;
      border-radius: 8px;
      transition: all 0.2s;
      
      &:hover {
        background: rgba(239, 68, 68, 0.1);
        color: #EF4444;
      }
    }
  }
}

.add-btn {
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
</style>
