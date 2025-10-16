<template>
  <Page>
    <ActionBar
      :title="article ? article.title : '播放器'"
      show-navigation-button="true"
      @navigationButtonTap="onBackTap"
    />

    <StackLayout v-if="isLoading" class="loading-container">
      <ActivityIndicator :busy="true" class="loading-indicator" />
      <Label text="加载中..." class="loading-text" />
    </StackLayout>

    <StackLayout v-else-if="article" class="player-container">
      <Label text="🎵" class="player-icon" />
      <Label :text="article.title" class="article-title" text-wrap="true" />
      <Label text="播放器功能开发中..." class="placeholder-text" />
      <Label :text="`文章 ID: ${article.id}`" class="article-id" />
    </StackLayout>

    <StackLayout v-else class="error-container">
      <Label text="❌" class="error-icon" />
      <Label text="文章未找到" class="error-text" />
    </StackLayout>
  </Page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Frame } from '@nativescript/core'
import { useStorage } from '../composables/useStorage'
import type { Article } from '../types'

// Props
interface Props {
  articleId: string
}

const props = defineProps<Props>()

// 存储服务
const storage = useStorage()

// 响应式状态
const article = ref<Article | null>(null)
const isLoading = ref(false)

/**
 * 加载文章数据
 */
const loadArticle = async () => {
  try {
    isLoading.value = true

    await storage.initialize()
    article.value = await storage.getArticleById(props.articleId)

    console.log('[PlayerView] Loaded article:', article.value?.id)
  } catch (err: any) {
    console.error('[PlayerView] Failed to load article:', err)
  } finally {
    isLoading.value = false
  }
}

/**
 * 返回按钮点击事件
 */
const onBackTap = () => {
  Frame.topmost().goBack()
}

// 页面加载时获取文章数据
onMounted(() => {
  loadArticle()
})
</script>

<style scoped>
.loading-container {
  margin-top: 100;
  horizontal-align: center;
  text-align: center;
}

.loading-indicator {
  color: #3b82f6;
  margin-bottom: 16;
}

.loading-text {
  font-size: 14;
  color: #6b7280;
}

.player-container {
  margin: 40 20;
  horizontal-align: center;
  text-align: center;
}

.player-icon {
  font-size: 64;
  margin-bottom: 24;
}

.article-title {
  font-size: 24;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 16;
  text-align: center;
}

.placeholder-text {
  font-size: 16;
  color: #6b7280;
  margin-bottom: 16;
}

.article-id {
  font-size: 12;
  color: #9ca3af;
}

.error-container {
  margin-top: 100;
  horizontal-align: center;
  text-align: center;
}

.error-icon {
  font-size: 64;
  margin-bottom: 16;
}

.error-text {
  font-size: 18;
  color: #ef4444;
}
</style>
