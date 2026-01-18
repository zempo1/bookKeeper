<template>
  <div class="crypto-date-picker" ref="pickerRef">
    <!-- Trigger Input -->
    <div 
      class="picker-trigger" 
      :class="{ 'is-active': visible }"
      @click="togglePicker"
    >
      <el-icon class="icon"><Calendar /></el-icon>
      <span v-if="modelValue && modelValue.length === 2" class="value-text">
        {{ modelValue[0] }} <span class="separator">to</span> {{ modelValue[1] }}
      </span>
      <span v-else class="placeholder">选择日期范围</span>
      <el-icon class="arrow-icon" :class="{ 'is-rotated': visible }"><ArrowDown /></el-icon>
    </div>

    <!-- Dropdown Panel -->
    <transition name="el-zoom-in-top">
      <div v-show="visible" class="picker-panel card-glass">
        <!-- Sidebar (Shortcuts) - Optional, mimicking Element Plus -->
        <div class="picker-sidebar">
          <button @click="selectShortcut(7)">最近7天</button>
          <button @click="selectShortcut(30)">最近30天</button>
          <button @click="selectShortcut(0, 'month')">本月</button>
          <button @click="selectShortcut(-1, 'month')">上月</button>
        </div>

        <div class="picker-body">
          <div class="date-range-header">
            <!-- Left Header -->
            <div class="header-section">
              <button class="nav-btn" @click="prevYear()"><el-icon><DArrowLeft /></el-icon></button>
              <button class="nav-btn" @click="prevMonth()"><el-icon><ArrowLeft /></el-icon></button>
              <span class="header-label">{{ leftYear }}年 {{ leftMonth + 1 }}月</span>
            </div>
            
            <!-- Right Header -->
            <div class="header-section">
              <span class="header-label">{{ rightYear }}年 {{ rightMonth + 1 }}月</span>
              <button class="nav-btn" @click="nextMonth()"><el-icon><ArrowRight /></el-icon></button>
              <button class="nav-btn" @click="nextYear()"><el-icon><DArrowRight /></el-icon></button>
            </div>
          </div>

          <div class="calendars-wrapper">
            <!-- Left Calendar -->
            <div class="calendar-grid">
              <div class="week-row">
                <span v-for="day in weekDays" :key="day">{{ day }}</span>
              </div>
              <div class="days-grid">
                <div 
                  v-for="(cell, index) in leftDays" 
                  :key="'L'+index"
                  class="day-cell"
                  :class="getDayClasses(cell)"
                  @click="handleDayClick(cell)"
                  @mouseenter="handleDayHover(cell)"
                >
                  <span class="cell-text">{{ cell.date.getDate() }}</span>
                </div>
              </div>
            </div>

            <!-- Right Calendar -->
            <div class="calendar-grid">
              <div class="week-row">
                <span v-for="day in weekDays" :key="day">{{ day }}</span>
              </div>
              <div class="days-grid">
                <div 
                  v-for="(cell, index) in rightDays" 
                  :key="'R'+index"
                  class="day-cell"
                  :class="getDayClasses(cell)"
                  @click="handleDayClick(cell)"
                  @mouseenter="handleDayHover(cell)"
                >
                  <span class="cell-text">{{ cell.date.getDate() }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { Calendar, ArrowDown, ArrowLeft, ArrowRight, DArrowLeft, DArrowRight } from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: string[] // [YYYY-MM-DD, YYYY-MM-DD]
}>()

const emit = defineEmits(['update:modelValue', 'change'])

const visible = ref(false)
const pickerRef = ref<HTMLElement | null>(null)
const weekDays = ['日', '一', '二', '三', '四', '五', '六']

// State for navigation (Left panel determines Right panel)
const leftDate = ref(new Date())
const selecting = ref(false) // true if start date selected, waiting for end date
const tempStart = ref<Date | null>(null)
const tempEnd = ref<Date | null>(null)
const hoverDate = ref<Date | null>(null)

// Initialize based on props
watch(() => props.modelValue, (val) => {
  if (val && val.length === 2) {
    // Check if values are defined before creating Date objects
    if (val[0] && val[1]) {
      const start = new Date(val[0])
      const end = new Date(val[1])
      if (!isNaN(start.getTime()) && !isNaN(end.getTime())) {
        tempStart.value = start
        tempEnd.value = end
        leftDate.value = new Date(start.getFullYear(), start.getMonth(), 1)
      }
    }
  }
}, { immediate: true })

// Computed Dates
const leftYear = computed(() => leftDate.value.getFullYear())
const leftMonth = computed(() => leftDate.value.getMonth())

const rightDate = computed(() => {
  return new Date(leftYear.value, leftMonth.value + 1, 1)
})
const rightYear = computed(() => rightDate.value.getFullYear())
const rightMonth = computed(() => rightDate.value.getMonth())

interface DayCell {
  date: Date
  type: 'normal' | 'prev' | 'next' // simplistic approach: just show current month days or empty/disabled?
  // Element Plus usually shows prev/next month days in grey. We will implement full grid.
  inMonth: boolean
}

function getDaysForMonth(year: number, month: number): DayCell[] {
  const days: DayCell[] = []
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  
  // Days from prev month
  const startDayOfWeek = firstDay.getDay() // 0 (Sun) - 6 (Sat)
  const prevMonthLastDay = new Date(year, month, 0).getDate()
  
  for (let i = startDayOfWeek - 1; i >= 0; i--) {
    days.push({
      date: new Date(year, month - 1, prevMonthLastDay - i),
      type: 'prev',
      inMonth: false
    })
  }
  
  // Current month days
  for (let i = 1; i <= lastDay.getDate(); i++) {
    days.push({
      date: new Date(year, month, i),
      type: 'normal',
      inMonth: true
    })
  }
  
  // Days from next month to fill 42 cells (6 rows)
  const remaining = 42 - days.length
  for (let i = 1; i <= remaining; i++) {
    days.push({
      date: new Date(year, month + 1, i),
      type: 'next',
      inMonth: false
    })
  }
  
  return days
}

const leftDays = computed(() => getDaysForMonth(leftYear.value, leftMonth.value))
const rightDays = computed(() => getDaysForMonth(rightYear.value, rightMonth.value))

// Actions
function togglePicker() {
  visible.value = !visible.value
}

function prevMonth() {
  leftDate.value = new Date(leftYear.value, leftMonth.value - 1, 1)
}

function nextMonth() {
  leftDate.value = new Date(leftYear.value, leftMonth.value + 1, 1)
}

function prevYear() {
  leftDate.value = new Date(leftYear.value - 1, leftMonth.value, 1)
}

function nextYear() {
  leftDate.value = new Date(leftYear.value + 1, leftMonth.value, 1)
}

function handleDayClick(cell: DayCell) {
  const clickedDate = cell.date
  
  if (!selecting.value) {
    // Start new selection
    tempStart.value = clickedDate
    tempEnd.value = null
    selecting.value = true
  } else {
    // Complete selection
    if (clickedDate < tempStart.value!) {
      tempStart.value = clickedDate
      // Keep selecting or just reset end? 
      // Usually if you click earlier date, it becomes new start and we still wait for end?
      // Or we can just swap and finish? Element Plus swaps and finishes if logical.
      // Let's reset end and keep selecting for better UX or just swap.
      // Standard behavior: 
      // 1. Click Start
      // 2. Click End (if < Start, swap) -> Finish
      tempEnd.value = tempStart.value // logic swap handled by logic below
      tempStart.value = clickedDate
      finishSelection()
    } else {
      tempEnd.value = clickedDate
      finishSelection()
    }
  }
}

function finishSelection() {
  selecting.value = false
  visible.value = false
  if (tempStart.value && tempEnd.value) {
    const fmt = (d: Date) => {
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    }
    const val = [fmt(tempStart.value), fmt(tempEnd.value)]
    emit('update:modelValue', val)
    emit('change', val)
  }
}

function handleDayHover(cell: DayCell) {
  if (selecting.value) {
    hoverDate.value = cell.date
  }
}

// Styling Logic
function getDayClasses(cell: DayCell) {
  const d = cell.date.getTime()
  const s = tempStart.value?.getTime()
  const e = tempEnd.value?.getTime()
  const h = hoverDate.value?.getTime()
  
  const isStart = s === d
  const isEnd = e === d
  
  // Range logic
  let inRange = false
  if (selecting.value && s && h) {
    // Preview range
    inRange = (d > s && d <= h) || (d < s && d >= h)
  } else if (!selecting.value && s && e) {
    // Selected range
    inRange = d > s && d < e
  }
  
  return {
    'is-in-month': cell.inMonth,
    'is-start': isStart,
    'is-end': isEnd,
    'in-range': inRange,
    'is-today': isSameDay(cell.date, new Date())
  }
}

function isSameDay(d1: Date, d2: Date) {
  return d1.getFullYear() === d2.getFullYear() && 
         d1.getMonth() === d2.getMonth() && 
         d1.getDate() === d2.getDate()
}

// Shortcuts
function selectShortcut(days: number, unit: 'day' | 'month' = 'day') {
  const end = new Date()
  const start = new Date()
  
  if (unit === 'day') {
    start.setDate(end.getDate() - days)
  } else {
    // Month logic
    if (days === 0) { // Current Month
      start.setDate(1)
    } else { // Previous Month
      start.setMonth(start.getMonth() - 1)
      start.setDate(1)
      end.setDate(0) // Last day of prev month
    }
  }
  
  tempStart.value = start
  tempEnd.value = end
  finishSelection()
}

// Outside Click
function handleClickOutside(e: MouseEvent) {
  if (pickerRef.value && !pickerRef.value.contains(e.target as Node)) {
    visible.value = false
    selecting.value = false // Reset selection state if closed
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

.crypto-date-picker {
  position: relative;
  width: 100%;
  font-family: $font-family;
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
    display: flex;
    align-items: center;
    gap: 8px;
    
    .separator {
      color: $text-muted;
      font-size: 0.8rem;
    }
  }
  
  .placeholder {
    flex: 1;
    color: $text-muted;
    font-size: 0.9rem;
  }
  
  .arrow-icon {
    font-size: 0.8rem;
    color: $text-muted;
    transition: transform 0.3s;
    
    &.is-rotated {
      transform: rotate(180deg);
    }
  }
}

.picker-panel {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  z-index: 2000;
  display: flex;
  background: #1e293b; // Fallback
  background: rgba(30, 41, 59, 0.95);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
  overflow: hidden;
  padding: 0;
  width: max-content;
  
  // Glassmorphism helper class is usually external but we define specific here
}

.picker-sidebar {
  width: 120px;
  border-right: 1px solid rgba(255, 255, 255, 0.05);
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  
  button {
    background: transparent;
    border: none;
    color: $text-muted;
    padding: 8px 12px;
    text-align: left;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s;
    font-size: 0.9rem;
    
    &:hover {
      background: rgba(255, 255, 255, 0.05);
      color: $primary-color;
    }
  }
}

.picker-body {
  padding: 20px;
}

.date-range-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  padding: 0 10px;
  
  .header-section {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .header-label {
      font-weight: 600;
      color: $text-color;
      font-family: $font-family-display;
      min-width: 100px;
      text-align: center;
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
      
      &:hover {
        color: $primary-color;
        background: rgba(255, 255, 255, 0.05);
      }
    }
  }
}

.calendars-wrapper {
  display: flex;
  gap: 24px;
}

.calendar-grid {
  width: 280px;
}

.week-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 12px;
  
  span {
    text-align: center;
    color: $text-muted;
    font-size: 0.8rem;
    font-weight: 500;
  }
}

.days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  row-gap: 4px;
}

.day-cell {
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;
  font-size: 0.9rem;
  color: $text-color;
  border-radius: 4px;
  transition: all 0.2s;
  
  .cell-text {
    z-index: 2;
    position: relative;
  }
  
  // Not in month
  &:not(.is-in-month) {
    color: rgba(255, 255, 255, 0.2);
  }
  
  // Hover effect
  &:hover {
    color: $primary-color;
    background: rgba(255, 255, 255, 0.05);
  }
  
  // Range Styling
  &.in-range {
    background: rgba($primary-color, 0.15);
    color: $primary-color;
    border-radius: 0;
    
    // Connect range visually
    &:first-child { border-top-left-radius: 4px; border-bottom-left-radius: 4px; }
    &:last-child { border-top-right-radius: 4px; border-bottom-right-radius: 4px; }
  }
  
  &.is-start {
    background: $primary-color !important;
    color: #000 !important;
    border-top-left-radius: 4px;
    border-bottom-left-radius: 4px;
    box-shadow: 0 0 10px rgba($primary-color, 0.4);
  }
  
  &.is-end {
    background: $primary-color !important;
    color: #000 !important;
    border-top-right-radius: 4px;
    border-bottom-right-radius: 4px;
    box-shadow: 0 0 10px rgba($primary-color, 0.4);
  }
  
  &.is-today {
    &::after {
      content: '';
      position: absolute;
      bottom: 4px;
      left: 50%;
      transform: translateX(-50%);
      width: 4px;
      height: 4px;
      border-radius: 50%;
      background: $secondary-color;
    }
  }
}
</style>