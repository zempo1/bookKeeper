<template>
  <div class="dashboard-layout">
    <aside class="sidebar card-glass">
      <div class="logo">
        <span class="icon">💎</span>
        <h2>BookKeeper</h2>
      </div>
      
      <nav class="nav-menu">
        <router-link to="/dashboard" class="nav-item" active-class="active">
          <span class="icon">📊</span>
          仪表盘
        </router-link>
        <router-link to="/records" class="nav-item" active-class="active">
          <span class="icon">📝</span>
          记录
        </router-link>
        <router-link to="/categories" class="nav-item" active-class="active">
          <span class="icon">🏷️</span>
          分类
        </router-link>
      </nav>
      
      <div class="user-profile">
        <div class="avatar">{{ userStore.user?.username.charAt(0).toUpperCase() }}</div>
        <div class="info">
          <span class="username">{{ userStore.user?.username }}</span>
          <button @click="handleLogout" class="logout-btn">退出登录</button>
        </div>
      </div>
    </aside>
    
    <main class="main-content">
      <header class="top-bar card-glass">
        <h3>{{ currentPageTitle }}</h3>
        <div class="actions">
          <button class="action-btn">🔔</button>
          <button class="action-btn">⚙️</button>
        </div>
      </header>
      
      <div class="content-area">
        <slot></slot>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentPageTitle = computed(() => {
  const name = route.name?.toString() || 'dashboard'
  switch (name) {
    case 'dashboard': return '仪表盘'
    case 'records': return '记录'
    case 'categories': return '分类'
    default: return '仪表盘'
  }
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.dashboard-layout {
  display: flex;
  min-height: 100vh;
  padding: 20px;
  gap: 20px;
  background-image: 
    radial-gradient(circle at 15% 50%, rgba(139, 92, 246, 0.15) 0%, transparent 25%),
    radial-gradient(circle at 85% 30%, rgba(245, 158, 11, 0.15) 0%, transparent 25%);
}

.sidebar {
  width: 260px;
  display: flex;
  flex-direction: column;
  padding: 24px;
  height: calc(100vh - 40px);
  position: sticky;
  top: 20px;
  
  .logo {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 40px;
    padding: 0 12px;
    
    .icon {
      font-size: 24px;
    }
    
    h2 {
      margin: 0;
      font-family: $font-family-display;
      font-size: 1.25rem;
      background: linear-gradient(135deg, $primary-color, $secondary-color);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
  }
  
  .nav-menu {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .nav-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 16px;
      border-radius: 12px;
      color: $text-muted;
      text-decoration: none;
      transition: all 0.2s;
      
      &:hover {
        background: rgba(255, 255, 255, 0.05);
        color: $text-color;
      }
      
      &.active {
        background: linear-gradient(90deg, rgba(245, 158, 11, 0.15), transparent);
        color: $primary-color;
        border-left: 3px solid $primary-color;
      }
    }
  }
  
  .user-profile {
    display: flex;
    align-items: center;
    gap: 12px;
    padding-top: 20px;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    
    .avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: linear-gradient(135deg, $primary-color, $secondary-color);
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      color: white;
    }
    
    .info {
      display: flex;
      flex-direction: column;
      
      .username {
        font-weight: 500;
        font-size: 0.9rem;
      }
      
      .logout-btn {
        background: none;
        border: none;
        padding: 0;
        text-align: left;
        color: $text-muted;
        font-size: 0.8rem;
        cursor: pointer;
        
        &:hover {
          color: $text-color;
        }
      }
    }
  }
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  
  .top-bar {
    height: 70px;
    padding: 0 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    h3 {
      margin: 0;
      font-family: $font-family-display;
    }
    
    .actions {
      display: flex;
      gap: 16px;
      
      .action-btn {
        background: none;
        border: none;
        font-size: 1.2rem;
        cursor: pointer;
        opacity: 0.7;
        transition: opacity 0.2s;
        
        &:hover {
          opacity: 1;
        }
      }
    }
  }
  
  .content-area {
    flex: 1;
    overflow-y: auto;
  }
}

@media (max-width: 768px) {
  .dashboard-layout {
    flex-direction: column;
    padding: 10px;
  }
  
  .sidebar {
    width: 100%;
    height: auto;
    position: relative;
    top: 0;
    margin-bottom: 20px;
  }
}
</style>
