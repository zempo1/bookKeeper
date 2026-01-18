<script setup lang="ts">
import { RouterView } from 'vue-router'
import { ElMessage } from 'element-plus'
import { provide } from 'vue'

const showToast = (message: string, type: 'success' | 'error' | 'info' | 'warning' = 'info', duration = 3000) => {
  ElMessage({
    message,
    type,
    duration
  })
}

provide('toast', showToast)
</script>

<template>
  <RouterView />
</template>

<style scoped>
</style>
