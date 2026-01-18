<template>
  <DashboardLayout>
    <div class="categories-view">
      <el-card class="card-glass" shadow="never">
        <template #header>
          <div class="card-header">
            <div class="left">
              <h3>分类管理</h3>
              <div class="web3-toggle">
                <div 
                  class="toggle-item" 
                  :class="{ active: currentType === 'EXPENSE' }"
                  @click="currentType = 'EXPENSE'"
                >
                  支出
                </div>
                <div 
                  class="toggle-item" 
                  :class="{ active: currentType === 'INCOME' }"
                  @click="currentType = 'INCOME'"
                >
                  收入
                </div>
                <div 
                  class="toggle-slider"
                  :style="{ transform: currentType === 'EXPENSE' ? 'translateX(0)' : 'translateX(100%)' }"
                ></div>
              </div>
            </div>
            <el-button type="primary" @click="showAddModal = true" class="add-btn">
              <el-icon class="el-icon--left"><Plus /></el-icon>新建分类
            </el-button>
          </div>
        </template>

        <div class="category-grid">
          <div v-for="cat in filteredCategories" :key="cat.id" class="category-card">
            <div class="name">{{ cat.name }}</div>
            <div class="actions">
              <el-button link type="danger" :icon="Delete" @click="handleDelete(cat)"></el-button>
            </div>
          </div>
          
          <div class="category-card add-card" @click="showAddModal = true">
            <el-icon><Plus /></el-icon>
            <span class="add-text">新建</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- Add Category Dialog -->
    <el-dialog
      v-model="showAddModal"
      title="新建分类"
      width="400px"
      class="record-dialog"
      align-center
    >
      <el-form :model="form" label-position="top" class="category-form">
        <el-form-item>
          <div class="custom-input-wrapper" :class="{ 'has-content': form.name }">
            <el-input 
              v-model="form.name" 
              placeholder="分类名称" 
              id="name-input"
            />
            <label for="name-input" class="floating-label">分类名称</label>
            <div class="input-highlight"></div>
          </div>
        </el-form-item>

        <el-form-item>
          <div class="type-toggle-wrapper">
            <div 
              class="toggle-option" 
              :class="{ active: form.type === 'EXPENSE' }"
              @click="form.type = 'EXPENSE'"
            >
              支出
            </div>
            <div 
              class="toggle-option" 
              :class="{ active: form.type === 'INCOME' }"
              @click="form.type = 'INCOME'"
            >
              收入
            </div>
            <div 
              class="toggle-indicator"
              :style="{ transform: form.type === 'EXPENSE' ? 'translateX(0)' : 'translateX(100%)' }"
            ></div>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddModal = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </DashboardLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'
import DashboardLayout from '@/layouts/DashboardLayout.vue'
import { useUserStore } from '@/stores/user'
import { categoryApi } from '@/api/category'
import { Plus, Delete } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import type { Category } from '@/types'

const userStore = useUserStore()
const toast = inject<any>('toast')

const categories = ref<Category[]>([])
const currentType = ref<'EXPENSE' | 'INCOME'>('EXPENSE')
const showAddModal = ref(false)

const form = ref({
  name: '',
  type: 'EXPENSE'
})

const filteredCategories = computed(() => {
  return categories.value.filter(c => c.type === currentType.value)
})

onMounted(fetchCategories)

async function fetchCategories() {
  if (!userStore.user?.id) return
  try {
    const res = await categoryApi.getCategories(userStore.user.id)
    categories.value = res.data.data
  } catch (error) {
    console.error(error)
  }
}

const handleSubmit = async () => {
  if (!form.value.name) return toast('请输入分类名称', 'warning')
  
  try {
    await categoryApi.createCategory({
      ...form.value,
      userId: userStore.user!.id
    })
    toast('创建成功', 'success')
    showAddModal.value = false
    fetchCategories()
    form.value.name = '' // reset
  } catch (error) {
    toast('创建失败', 'error')
  }
}

const handleDelete = (cat: Category) => {
  ElMessageBox.confirm(
    '确定要删除这个分类吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await categoryApi.deleteCategory(cat.id)
      toast('删除成功', 'success')
      fetchCategories()
    } catch (error) {
      toast('删除失败', 'error')
    }
  })
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.categories-view {
  height: 100%;
}

.card-glass {
  background: $card-bg !important;
  border: 1px solid $card-border !important;
  border-radius: 16px !important;
  min-height: 100%;
  
  :deep(.el-card__header) {
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    padding: 16px 20px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .left {
    display: flex;
    align-items: center;
    gap: 30px;
    
    h3 {
      margin: 0;
      color: $text-muted;
      font-weight: 500;
    }

    .web3-toggle {
      display: flex;
      background: rgba(0, 0, 0, 0.3);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 12px;
      position: relative;
      padding: 4px;
      width: 180px;
      height: 44px;
      cursor: pointer;
      box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.2);

      .toggle-item {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 2;
        font-weight: 600;
        font-size: 0.95rem;
        color: $text-muted;
        transition: color 0.3s;
        
        &.active {
          color: white;
          text-shadow: 0 0 8px rgba(255, 255, 255, 0.4);
        }
      }

      .toggle-slider {
        position: absolute;
        top: 4px;
        left: 4px;
        width: calc(50% - 4px);
        height: calc(100% - 8px);
        background: linear-gradient(135deg, $primary-color, darken($primary-color, 5%));
        border-radius: 8px;
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        z-index: 1;
        box-shadow: 0 4px 12px rgba($primary-color, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.2);
        
        // Add a subtle glow effect
        &::after {
          content: '';
          position: absolute;
          inset: 0;
          border-radius: 8px;
          background: inherit;
          filter: blur(8px);
          opacity: 0.5;
          z-index: -1;
        }
      }
    }
  }
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 24px;
  padding: 24px;
  
  .category-card {
    background: rgba(255, 255, 255, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.05);
    border-radius: 20px;
    padding: 24px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 16px;
    position: relative;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    &:hover {
      transform: translateY(-4px);
      background: rgba(255, 255, 255, 0.06);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
      border-color: rgba(255, 255, 255, 0.1);
      
      .actions { opacity: 1; transform: translateY(0); }
    }
    
    .icon {
      font-size: 3rem;
      filter: drop-shadow(0 4px 8px rgba(0,0,0,0.2));
    }
    
    .name {
      font-weight: 500;
      color: #FCD34D;
      font-size: 1.1rem;
    }
    
    .actions {
      position: absolute;
      top: 12px;
      right: 12px;
      opacity: 0;
      transform: translateY(-5px);
      transition: all 0.2s;
    }
    
    &.add-card {
      border: 2px dashed rgba(255, 255, 255, 0.1);
      cursor: pointer;
      justify-content: center;
      color: $text-muted;
      background: transparent;
      
      .el-icon {
        font-size: 2.5rem;
        margin-bottom: 8px;
        transition: transform 0.3s;
      }
      
      .add-text {
        font-size: 0.9rem;
        font-weight: 500;
      }
      
      &:hover {
        border-color: $primary-color;
        color: $primary-color;
        background: rgba(245, 158, 11, 0.05);
        transform: translateY(-4px);
        
        .el-icon { transform: scale(1.1); }
      }
    }
  }
}

// Custom Form Styles
.category-form {
  padding: 10px 0;
}

.custom-input-wrapper {
  position: relative;
  width: 100%;
  margin-bottom: 10px;
  
  :deep(.el-input__wrapper) {
    background: transparent !important;
    box-shadow: none !important;
    border-bottom: 2px solid rgba(255, 255, 255, 0.1);
    border-radius: 0;
    padding: 10px 0;
    transition: all 0.3s;
    
    &.is-focus {
      border-color: $primary-color;
    }
  }
  
  :deep(.el-input__inner) {
    font-size: 1.2rem;
    color: $text-color;
    height: 40px;
    padding: 0;
    
    &::placeholder {
      opacity: 0;
    }
  }
  
  .floating-label {
    position: absolute;
    left: 0;
    top: 10px;
    color: $text-muted;
    font-size: 1rem;
    pointer-events: none;
    transition: all 0.3s ease;
    z-index: 10;
  }
  
  // Floating label logic
  &:focus-within .floating-label,
  &.has-content .floating-label {
    top: -24px;
    font-size: 0.8rem;
    color: $primary-color;
  }
  
  // Hide placeholder when not focused (optional, if you want placeholder to only appear when focused but label is floating)
  // Actually standard floating label doesn't use placeholder usually, or only shows it when focused.
  // With our custom label, we should just hide native placeholder always to avoid conflict.
  :deep(.el-input__inner::placeholder) {
    color: transparent;
  }
  
  .input-highlight {
    position: absolute;
    bottom: 0;
    left: 0;
    height: 2px;
    width: 0;
    background: $primary-color;
    transition: width 0.3s;
  }
  
  :deep(.el-input__wrapper.is-focus ~ .input-highlight) {
    width: 100%;
  }
}

.type-toggle-wrapper {
  display: flex;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  position: relative;
  padding: 4px;
  margin-top: 20px;
  cursor: pointer;
  
  .toggle-option {
    flex: 1;
    text-align: center;
    padding: 12px;
    z-index: 2;
    font-weight: 500;
    color: $text-muted;
    transition: color 0.3s;
    
    &.active {
      color: white;
    }
  }
  
  .toggle-indicator {
    position: absolute;
    top: 4px;
    left: 4px;
    width: calc(50% - 4px);
    height: calc(100% - 8px);
    background: $primary-color;
    border-radius: 8px;
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    z-index: 1;
    box-shadow: 0 2px 8px rgba(0,0,0,0.2);
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
