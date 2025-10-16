<template>
  <Page>
    <ActionBar title="音频库" />

    <!-- 主内容区域 -->
    <StackLayout>
      <!-- 加载状态 -->
      <ActivityIndicator v-if="isLoading" :busy="true" class="loading-indicator" />

      <!-- 空状态 -->
      <StackLayout v-else-if="articles.length === 0" class="empty-state">
        <Label text="📚" class="empty-icon" />
        <Label text="暂无音频" class="empty-title" />
        <Label text="点击下方按钮创建您的第一个音频播客" class="empty-subtitle" />
      </StackLayout>

      <!-- 文章列表 -->
      <ListView
        v-else
        :items="articles"
        separator-color="transparent"
        class="article-list"
        @itemTap="onArticleTap"
      >
        <template #default="{ item }">
          <StackLayout class="article-card">
            <!-- 标题 -->
            <Label :text="item.title" class="article-title" text-wrap="true" />

            <!-- 元信息 -->
            <FlexboxLayout class="article-meta">
              <Label :text="formatDuration(item.duration)" class="meta-item" />
              <Label text="•" class="meta-separator" />
              <Label :text="formatDate(item.createdAt)" class="meta-item" />
            </FlexboxLayout>

            <!-- 状态指示器 -->
            <Label
              v-if="item.status !== 'completed'"
              :text="getStatusText(item.status)"
              :class="['status-badge', `status-${item.status}`]"
            />
          </StackLayout>
        </template>
      </ListView>

      <!-- 添加按钮（固定在底部） -->
      <Button text="+ 添加新文章" class="add-button" @tap="onAddArticle" />
    </StackLayout>
  </Page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Frame } from '@nativescript/core'
import { useStorage } from '../composables/useStorage'
import type { Article } from '../types'
import PlayerView from './PlayerView.vue'
import InputView from './InputView.vue'

// 存储服务
const storage = useStorage()

// 响应式状态
const articles = ref<Article[]>([])
const isLoading = ref(false)
const error = ref<string | null>(null)

/**
 * 加载文章列表
 */
const loadArticles = async () => {
  try {
    isLoading.value = true
    error.value = null

    await storage.initialize()
    articles.value = await storage.getAllArticles()

    console.log('[LibraryView] Loaded articles:', articles.value.length)
  } catch (err: any) {
    error.value = err.message || String(err)
    console.error('[LibraryView] Failed to load articles:', error.value)
  } finally {
    isLoading.value = false
  }
}

/**
 * 格式化时长（秒 → mm:ss）
 */
const formatDuration = (seconds: number): string => {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = Math.floor(seconds % 60)
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

/**
 * 格式化日期
 */
const formatDate = (date: Date): string => {
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))

  if (diffDays === 0) {
    return '今天'
  } else if (diffDays === 1) {
    return '昨天'
  } else if (diffDays < 7) {
    return `${diffDays}天前`
  } else {
    const month = date.getMonth() + 1
    const day = date.getDate()
    return `${month}月${day}日`
  }
}

/**
 * 获取状态文本
 */
const getStatusText = (status: Article['status']): string => {
  const statusMap = {
    pending: '等待中',
    processing: '处理中',
    completed: '已完成',
    failed: '失败',
  }
  return statusMap[status] || status
}

/**
 * 文章卡片点击事件
 */
const onArticleTap = (args: any) => {
  const article = articles.value[args.index]
  console.log('[LibraryView] Article tapped:', article.id)

  // 导航到 PlayerView，传递 articleId
  Frame.topmost().navigate({
    moduleName: PlayerView,
    props: {
      articleId: article.id,
    },
    transition: {
      name: 'slide',
      duration: 300,
      curve: 'easeInOut',
    },
  })
}

/**
 * 添加文章按钮点击事件
 */
const onAddArticle = () => {
  console.log('[LibraryView] Add article button tapped')

  // 导航到 InputView
  Frame.topmost().navigate({
    moduleName: InputView,
    transition: {
      name: 'slide',
      duration: 300,
      curve: 'easeInOut',
    },
  })
}

// 页面加载时获取文章列表
onMounted(() => {
  loadArticles()
})
</script>

<style scoped>
/* 加载指示器 */
.loading-indicator {
  margin-top: 100;
  color: #3b82f6;
}

/* 空状态 */
.empty-state {
  margin-top: 100;
  horizontal-align: center;
  text-align: center;
}

.empty-icon {
  font-size: 64;
  margin-bottom: 16;
}

.empty-title {
  font-size: 20;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 8;
}

.empty-subtitle {
  font-size: 14;
  color: #6b7280;
  text-align: center;
  padding: 0 40;
}

/* 文章列表 */
.article-list {
  height: 100%;
}

/* 文章卡片 */
.article-card {
  background-color: #ffffff;
  margin: 12 16 0 16;
  padding: 16;
  border-radius: 12;
  border-width: 1;
  border-color: #e5e7eb;
}

.article-card:active {
  background-color: #f9fafb;
}

/* 文章标题 */
.article-title {
  font-size: 18;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 8;
}

/* 元信息容器 */
.article-meta {
  flex-direction: row;
  align-items: center;
}

.meta-item {
  font-size: 12;
  color: #6b7280;
}

.meta-separator {
  font-size: 12;
  color: #d1d5db;
  margin: 0 6;
}

/* 状态徽章 */
.status-badge {
  font-size: 12;
  padding: 4 8;
  border-radius: 6;
  margin-top: 8;
  width: 70;
  text-align: center;
}

.status-pending {
  background-color: #fef3c7;
  color: #92400e;
}

.status-processing {
  background-color: #dbeafe;
  color: #1e40af;
}

.status-failed {
  background-color: #fee2e2;
  color: #991b1b;
}

/* 添加按钮 */
.add-button {
  background-color: #3b82f6;
  color: #ffffff;
  font-size: 16;
  font-weight: bold;
  border-radius: 12;
  margin: 16;
  padding: 16;
}

.add-button:active {
  background-color: #2563eb;
}
</style>
