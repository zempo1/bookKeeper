<template>
  <div class="login-container">
    <div class="login-card card-glass">
      <div class="visualization-area">
        <div class="chart-decoration">
          <div class="circle c1"></div>
          <div class="circle c2"></div>
          <div class="line-chart">
            <svg viewBox="0 0 100 40" class="chart-svg">
              <path d="M0,40 L10,35 L20,38 L30,30 L40,32 L50,20 L60,25 L70,15 L80,18 L90,10 L100,5" fill="none" stroke="#F59E0B" stroke-width="2" />
              <path d="M0,40 L10,35 L20,38 L30,30 L40,32 L50,20 L60,25 L70,15 L80,18 L90,10 L100,5 V40 H0 Z" fill="url(#gradient)" opacity="0.2" />
              <defs>
                <linearGradient id="gradient" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stop-color="#F59E0B" />
                  <stop offset="100%" stop-color="transparent" />
                </linearGradient>
              </defs>
            </svg>
          </div>
        </div>
        <h3>BookKeeper</h3>
        <p>智能财务管理</p>
      </div>
      
      <div class="form-area">
        <h2 class="form-title">{{ isLoginMode ? '欢迎回来' : '创建账号' }}</h2>
        <div class="features">
          <div class="feature-item">
            <el-icon><DataAnalysis /></el-icon>
            <span>实时分析</span>
          </div>
          <div class="feature-item">
            <el-icon><Lock /></el-icon>
            <span>数据安全</span>
          </div>
        </div>
        
        <el-form 
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @submit.prevent
          class="custom-form"
        >
          <el-form-item label="用户名" prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="请输入您的用户名" 
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入您的密码" 
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          
          <el-button 
            type="primary" 
            class="cta-button" 
            :loading="loading"
            @click="handleSubmit"
          >
            {{ isLoginMode ? '登录' : '注册' }}
          </el-button>
        </el-form>
        
        <p class="toggle-mode">
          {{ isLoginMode ? '还没有账号？' : '已有账号？' }} 
          <a @click="toggleMode">{{ isLoginMode ? '立即注册' : '立即登录' }}</a>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, inject } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { User, Lock, DataAnalysis } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const toast = inject<any>('toast')

const formRef = ref<FormInstance>()
const loading = ref(false)
const isLoginMode = ref(true)

const form = reactive({
  username: '',
  password: ''
})

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '长度至少为 3 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '长度至少为 6 个字符', trigger: 'blur' }
  ]
})

const toggleMode = () => {
  isLoginMode.value = !isLoginMode.value
  form.password = ''
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      if (isLoginMode.value) {
        await handleLogin()
      } else {
        await handleRegister()
      }
      loading.value = false
    }
  })
}

const handleLogin = async () => {
  try {
    console.log('Logging in...', form.username)
    const res = await authApi.login({ 
      username: form.username, 
      password: form.password 
    })
    
    if (res.data.code === 200) {
      const userStore = useUserStore()
      userStore.setUser(res.data.data)
      toast('登录成功', 'success')
      router.push('/dashboard')
    } else {
      toast(res.data.message || '登录失败', 'error')
    }
  } catch (error: any) {
    console.error('Login error', error)
    if (error.response?.data?.message) {
      toast(error.response.data.message, 'error')
    } else {
      // Mock login for demo if backend is not running
      // REMOVE THIS IN PRODUCTION
      if (form.username) {
        const userStore = useUserStore()
        userStore.setUser({ id: 1, username: form.username, createdAt: new Date().toISOString() })
        toast('登录成功 (模拟)', 'warning')
        router.push('/dashboard')
      }
    }
  }
}

const handleRegister = async () => {
  try {
    console.log('Registering...', form.username)
    const res = await authApi.register({ 
      username: form.username, 
      password: form.password 
    })
    
    if (res.data.code === 200) {
      toast('注册成功，请登录', 'success')
      toggleMode()
    } else {
      toast(res.data.message || '注册失败', 'error')
    }
  } catch (error: any) {
    console.error('Registration error', error)
    if (error.response?.data?.message) {
      toast(error.response.data.message, 'error')
    } else {
      // Fallback for demo
      toast('注册成功 (模拟)，请登录', 'warning')
      toggleMode()
    }
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  position: relative;
  overflow: hidden;

  // Background Grid Decoration
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: 
      linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
    background-size: 40px 40px;
    z-index: -1;
  }
}

.login-card {
  display: flex;
  width: 100%;
  max-width: 900px;
  min-height: 500px;
  overflow: hidden;
  
  @media (max-width: 768px) {
    flex-direction: column;
  }
}

.visualization-area {
  flex: 1;
  background: rgba(15, 23, 42, 0.6);
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  position: relative;
  border-right: 1px solid $card-border;
  
  h3 {
    font-family: $font-family-display;
    font-size: 2.5rem;
    margin: 20px 0 10px;
    background: linear-gradient(135deg, $primary-color, $secondary-color);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    letter-spacing: 2px;
  }

  p {
    color: $text-muted;
    font-size: 1.1rem;
    letter-spacing: 1px;
  }

  .chart-decoration {
    position: relative;
    width: 200px;
    height: 120px;
    margin-bottom: 20px;

    .circle {
      position: absolute;
      border-radius: 50%;
      filter: blur(20px);
    }
    .c1 {
      width: 80px;
      height: 80px;
      background: $primary-color;
      opacity: 0.3;
      top: 10%;
      left: 10%;
      animation: float 6s infinite ease-in-out;
    }
    .c2 {
      width: 60px;
      height: 60px;
      background: $cta-color;
      opacity: 0.3;
      bottom: 10%;
      right: 10%;
      animation: float 8s infinite ease-in-out reverse;
    }
    
    .line-chart {
      position: absolute;
      bottom: 0;
      left: 0;
      width: 100%;
      height: 100%;
      z-index: 10;
      
      .chart-svg {
        width: 100%;
        height: 100%;
        overflow: visible;
        
        path {
          vector-effect: non-scaling-stroke;
        }
      }
    }
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.form-area {
  flex: 1;
  padding: 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: rgba(30, 41, 59, 0.4);
  
  h2 {
    color: $text-color;
    margin-bottom: 10px;
    font-family: $font-family-display;
    font-weight: 600;
  }
  
  .features {
    display: flex;
    gap: 20px;
    margin-bottom: 30px;
    
    .feature-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 0.9rem;
      color: $text-muted;
      background: rgba(255, 255, 255, 0.05);
      padding: 6px 12px;
      border-radius: 20px;
      border: 1px solid rgba(255, 255, 255, 0.05);
      
      .el-icon {
        font-size: 1rem;
      }
    }
  }
}

.custom-form {
  :deep(.el-form-item__label) {
    color: $text-muted;
    padding-bottom: 4px;
  }

  :deep(.el-input__wrapper) {
    background-color: rgba(15, 23, 42, 0.6);
    box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
    padding: 8px 12px;
    border-radius: 8px;
    
    &.is-focus {
      box-shadow: 0 0 0 1px $primary-color inset !important;
    }
  }

  :deep(.el-input__inner) {
    color: $text-color;
    height: 36px;
    
    &::placeholder {
      color: rgba(148, 163, 184, 0.4);
    }
  }
}

.cta-button {
  width: 100%;
  height: 48px;
  margin-top: 20px;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  background: linear-gradient(135deg, $cta-color, darken($cta-color, 10%));
  border: none;
  
  &:hover, &:focus {
    background: linear-gradient(135deg, lighten($cta-color, 5%), $cta-color);
    transform: translateY(-2px);
    box-shadow: 0 10px 15px -3px rgba(139, 92, 246, 0.4);
  }
  
  &:active {
    transform: translateY(0);
  }
}

.toggle-mode {
  text-align: center;
  margin-top: 30px;
  font-size: 0.9rem;
  color: $text-muted;
  
  a {
    color: $primary-color;
    cursor: pointer;
    font-weight: 600;
    margin-left: 5px;
    transition: color 0.2s;
    
    &:hover {
      color: lighten($primary-color, 10%);
      text-decoration: underline;
    }
  }
}
</style>
