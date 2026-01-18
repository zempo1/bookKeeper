<template>
  <div class="crypto-month-picker" ref="pickerRef">
    <!-- Trigger Input -->
    <div 
      class="picker-trigger" 
      :class="{ 'is-active': visible }"
      @click="togglePicker"
    >
      <el-icon class="icon"><Calendar /></el-icon>
      <span class="value-text">
        {{ formattedValue }}
      </span>
      <el-icon class="arrow-icon" :class="{ 'is-rotated': visible }"><ArrowDown /></el-icon>
    </div>

    <!-- Dropdown Panel -->
    <transition name="el-zoom-in-top">
      <div v-show="visible" class="picker-panel card-glass">
        <div class="picker-header">
          <button class="nav-btn" @click.stop="changeYear(-1)"><el-icon><DArrowLeft /></el-icon></button>
          <span class="year-label">{{ currentYear }}年</span>
          <button class="nav-btn" @click.stop="changeYear(1)"><el-icon><DArrowRight /></el-icon></button>
        </div>

        <div class="months-grid">
          <div 
            v-for="(month, index) in 12" 
            :key="index"
            class="month-cell"
            :class="{ 
              'is-current': isCurrentMonth(index),
              'is-selected': isSelectedMonth(index)
            }"
            @click.stop="handleMonthClick(index)"
          >
            <span class="cell-text">{{ index + 1 }}月</span>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { Calendar, ArrowDown, DArrowLeft, DArrowRight } from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: Date
}>()

const emit = defineEmits(['update:modelValue', 'change'])

const visible = ref(false)
const pickerRef = ref<HTMLElement | null>(null)

// Internal state
const currentYear = ref(props.modelValue.getFullYear())

watch(() => props.modelValue, (val) => {
  if (val) {
    currentYear.value = val.getFullYear()
  }
})

const formattedValue = computed(() => {
  const y = props.modelValue.getFullYear()
  const m = String(props.modelValue.getMonth() + 1).padStart(2, '0')
  return `${y}年${m}月`
})

// Actions
function togglePicker() {
  visible.value = !visible.value
  if (visible.value) {
    // Reset view to selected year when opening
    currentYear.value = props.modelValue.getFullYear()
  }
}

function changeYear(offset: number) {
  currentYear.value += offset
}

function isCurrentMonth(monthIndex: number) {
  const now = new Date()
  return now.getFullYear() === currentYear.value && now.getMonth() === monthIndex
}

function isSelectedMonth(monthIndex: number) {
  return props.modelValue.getFullYear() === currentYear.value && props.modelValue.getMonth() === monthIndex
}

function handleMonthClick(monthIndex: number) {
  const newDate = new Date(currentYear.value, monthIndex, 1)
  emit('update:modelValue', newDate)
  emit('change', newDate)
  visible.value = false
}

// Outside Click
function handleClickOutside(e: MouseEvent) {
  if (pickerRef.value && !pickerRef.value.contains(e.target as Node)) {
    visible.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.crypto-month-picker {
  position: relative;
  font-family: $font-family;
  width: 160px;
}

.picker-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  height: 40px;
  
  &:hover, &.is-active {
    border-color: $primary-color;
    box-shadow: 0 0 10px rgba($primary-color, 0.2);
    background: rgba(255, 255, 255, 0.08);
  }
  
  .icon {
    font-size: 1.1rem;
    color: $text-muted;
    margin-right: 8px;
  }
  
  .value-text {
    flex: 1;
    font-size: 0.9rem;
    color: $text-color;
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .arrow-icon {
    font-size: 0.8rem;
    color: $text-muted;
    transition: transform 0.3s;
    margin-left: 8px;
    
    &.is-rotated {
      transform: rotate(180deg);
    }
  }
}

.picker-panel {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  z-index: 9999;
  background: #1e293b;
  background: rgba(30, 41, 59, 0.95);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
  overflow: hidden;
  padding: 16px;
  width: 280px;
}

.picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 8px;
  
  .year-label {
    font-size: 1.1rem;
    font-weight: 600;
    color: $text-color;
    font-family: $font-family-display;
  }
  
  .nav-btn {
    background: transparent;
    border: none;
    color: $text-muted;
    cursor: pointer;
    padding: 4px;
    border-radius: 4px;
    display: flex;
    align-items: center;
    transition: all 0.2s;
    
    &:hover {
      color: $primary-color;
      background: rgba(255, 255, 255, 0.05);
    }
    
    .el-icon {
      font-size: 1.2rem;
    }
  }
}

.months-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.month-cell {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s;
  color: $text-muted;
  font-size: 0.9rem;
  font-weight: 500;
  position: relative;
  
  &:hover {
    color: $text-color;
    background: rgba(255, 255, 255, 0.05);
  }
  
  &.is-current {
    color: $primary-color;
    font-weight: 600;
    
    &::after {
      content: '';
      position: absolute;
      bottom: 6px;
      left: 50%;
      transform: translateX(-50%);
      width: 4px;
      height: 4px;
      border-radius: 50%;
      background: $primary-color;
    }
  }
  
  &.is-selected {
    background: $primary-color !important;
    color: #000 !important;
    font-weight: 600;
    box-shadow: 0 0 10px rgba($primary-color, 0.4);
    
    &::after {
      display: none;
    }
  }
}
</style>