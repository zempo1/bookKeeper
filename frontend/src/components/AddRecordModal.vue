<template>
  <el-dialog
    v-model="visible"
    title="记一笔"
    width="500px"
    class="record-dialog"
    :before-close="close"
    align-center
  >
    <el-form :model="form" label-position="top" class="record-form">
      <!-- Type Selection -->
      <div class="type-selector">
        <el-radio-group v-model="form.type" size="large" fill="#F59E0B" @change="handleTypeChange">
          <el-radio-button label="EXPENSE">支出</el-radio-button>
          <el-radio-button label="INCOME">收入</el-radio-button>
        </el-radio-group>
      </div>

      <!-- Amount Input -->
      <el-form-item label="金额">
        <el-input 
          v-model="form.amount" 
          type="number" 
          placeholder="0.00" 
          class="amount-input"
        >
          <template #prefix>¥</template>
        </el-input>
      </el-form-item>

      <!-- Category Selection -->
      <el-form-item label="分类">
        <div class="category-grid">
          <div 
            v-for="cat in filteredCategories" 
            :key="cat.id"
            :class="['category-item', { active: form.categoryId === cat.id }]"
            @click="form.categoryId = cat.id"
          >
            <span class="cat-name">{{ cat.name }}</span>
          </div>
        </div>
      </el-form-item>

      <!-- Date & Description -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="日期">
            <el-date-picker
              v-model="form.recordDate"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="备注">
            <el-input v-model="form.description" placeholder="写点什么..." />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="close">取消</el-button>
        <el-button type="primary" @click="submit" :disabled="!form.amount || !form.categoryId">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { Category, Record } from '@/types'

import { categoryApi } from '@/api/category'

const props = defineProps<{
  isOpen: boolean
  categories: Category[]
  userId: number
}>()

const emit = defineEmits<{
  (e: 'update:isOpen', value: boolean): void
  (e: 'close'): void
  (e: 'submit', record: Partial<Record>): void
}>()

const visible = computed({
  get: () => props.isOpen,
  set: (val) => {
    emit('update:isOpen', val)
    if (!val) emit('close')
  }
})

const form = ref({
  type: 'EXPENSE' as 'INCOME' | 'EXPENSE',
  amount: '',
  categoryId: undefined as number | undefined,
  recordDate: new Date().toISOString().split('T')[0],
  description: ''
})

const categoryList = ref<Category[]>([])

const filteredCategories = computed(() => {
  return categoryList.value.filter(c => c.type === form.value.type)
})

watch(() => props.isOpen, (val) => {
  if (val) {
    fetchCategories()
  }
})

const fetchCategories = async () => {
  try {
    const res = await categoryApi.getCategories(props.userId)
    categoryList.value = res.data.data
  } catch (error) {
    console.error('Failed to fetch categories', error)
  }
}

const handleTypeChange = () => {
  form.value.categoryId = undefined
  // Ensure categories are re-filtered reactively
}

const close = () => {
  visible.value = false
}

const submit = () => {
  if (!form.value.amount || !form.value.categoryId) return
  
  emit('submit', {
    userId: props.userId,
    type: form.value.type,
    amount: parseFloat(form.value.amount),
    categoryId: form.value.categoryId,
    recordDate: form.value.recordDate,
    description: form.value.description
  })
  
  // Reset form
  form.value.amount = ''
  form.value.description = ''
  form.value.categoryId = undefined
  close()
}
</script>

<style lang="scss">
@import '@/styles/variables.scss';

.record-dialog {
  background: rgba(30, 41, 59, 0.95) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 16px !important;
  backdrop-filter: blur(12px);
  
  .el-dialog__header {
    margin-right: 0;
    padding: 20px 24px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    
    .el-dialog__title {
      color: $text-color;
      font-family: $font-family-display;
      font-size: 1.2rem;
    }
    
    .el-dialog__headerbtn .el-dialog__close {
      color: $text-muted;
      font-size: 1.2rem;
      
      &:hover {
        color: white;
      }
    }
  }
  
  .el-dialog__body {
    padding: 24px;
  }
  
  .el-dialog__footer {
    padding: 20px 24px;
    border-top: 1px solid rgba(255, 255, 255, 0.05);
  }
}
</style>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.type-selector {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
  
  :deep(.el-radio-button__inner) {
    background: rgba(255, 255, 255, 0.05);
    border-color: rgba(255, 255, 255, 0.1);
    color: $text-muted;
    border-radius: 0;
    padding: 10px 24px;
    font-weight: 500;
  }
  
  :deep(.el-radio-button:first-child .el-radio-button__inner) {
    border-radius: 8px 0 0 8px;
  }
  
  :deep(.el-radio-button:last-child .el-radio-button__inner) {
    border-radius: 0 8px 8px 0;
  }
  
  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    background: $primary-color;
    border-color: $primary-color;
    color: white;
    box-shadow: none;
  }
}

:deep(.el-form-item__label) {
  color: $text-muted;
  padding-bottom: 8px;
}

:deep(.el-input__wrapper), :deep(.el-textarea__inner) {
  background-color: rgba(15, 23, 42, 0.6);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
  
  &.is-focus {
    box-shadow: 0 0 0 1px $primary-color inset !important;
  }
}

:deep(.el-input__inner) {
  color: $text-color;
  height: 32px;
  
  &::placeholder {
    color: rgba(148, 163, 184, 0.4);
  }
}

.amount-input {
  :deep(.el-input__inner) {
    font-size: 1.5rem;
    font-family: $font-family-display;
    height: 40px;
  }
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(70px, 1fr));
  gap: 12px;
  width: 100%;
  
  .category-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 10px;
    background: rgba(255, 255, 255, 0.03);
    border-radius: 12px;
    cursor: pointer;
    border: 1px solid transparent;
    transition: all 0.2s;
    
    .cat-name { font-size: 0.8rem; color: $text-muted; }
    
    &:hover { background: rgba(255, 255, 255, 0.08); }
    
    &.active {
      border-color: $primary-color;
      background: rgba(245, 158, 11, 0.1);
      
      .cat-name { color: $primary-color; }
    }
  }
}

:deep(.el-button) {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
}

:deep(.el-button--default) {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: $text-color;
  
  &:hover {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.2);
    color: white;
  }
}

:deep(.el-button--primary) {
  background: $primary-color;
  border-color: $primary-color;
  
  &:hover {
    background: lighten($primary-color, 5%);
    border-color: lighten($primary-color, 5%);
  }
}
</style>
