<template>
  <Page>
    <ActionBar
      :title="article ? article.title : '播放器'"
      show-navigation-button="true"
      @navigationButtonTap="onBackTap"
    />

    <!-- 加载状态 -->
    <StackLayout v-if="isLoading" class="loading-container">
      <ActivityIndicator :busy="true" class="loading-indicator" />
      <Label text="加载中..." class="loading-text" />
    </StackLayout>

    <!-- 错误状态 -->
    <StackLayout v-else-if="error || playerError" class="error-container">
      <Label text="❌" class="error-icon" />
      <Label :text="error || playerError" class="error-text" text-wrap="true" />
      <Button text="重试" class="retry-button" @tap="onRetry" />
    </StackLayout>

    <!-- 播放器主界面 -->
    <GridLayout v-else-if="article" rows="auto, *, auto" class="player-layout">
      <!-- 顶部：文章信息 -->
      <StackLayout row="0" class="article-info">
        <Label :text="article.title" class="article-title" text-wrap="true" />
        <Label
          v-if="article.status !== 'completed'"
          :text="getStatusText(article.status)"
          :class="['status-badge', `status-${article.status}`]"
        />
      </StackLayout>

      <!-- 中间：播放器控制 -->
      <StackLayout row="1" vertical-alignment="center" class="player-controls">
        <!-- 播放器图标 -->
        <Label :text="playerStatusIcon" class="player-icon" />

        <!-- 进度条 -->
        <StackLayout class="progress-container">
          <Slider
            :value="currentTime"
            :max-value="duration"
            class="progress-slider"
            @valueChange="onProgressChange"
          />
          <FlexboxLayout class="time-labels">
            <Label :text="formatTime(currentTime)" class="time-current" />
            <Label :text="formatTime(duration)" class="time-duration" />
          </FlexboxLayout>
        </StackLayout>

        <!-- 控制按钮 -->
        <FlexboxLayout class="control-buttons">
          <!-- 快退 15 秒 -->
          <Button text="⏪ 15s" class="control-btn" @tap="onSeekBackward" />

          <!-- 播放/暂停 -->
          <Button
            :text="isPlaying ? '⏸' : '▶'"
            :class="['play-pause-btn', { playing: isPlaying }]"
            @tap="onPlayPause"
          />

          <!-- 快进 15 秒 -->
          <Button text="⏩ 15s" class="control-btn" @tap="onSeekForward" />
        </FlexboxLayout>

        <!-- 播放状态提示 -->
        <Label :text="playerStatusText" class="status-text" />
      </StackLayout>

      <!-- 底部：译文内容（可选） -->
      <ScrollView v-if="article.translation" row="2" class="transcript-container">
        <StackLayout class="transcript-content">
          <Label text="译文内容" class="transcript-header" />
          <Label :text="article.translation" class="transcript-text" text-wrap="true" />
        </StackLayout>
      </ScrollView>
    </GridLayout>

    <!-- 未找到文章 -->
    <StackLayout v-else class="error-container">
      <Label text="❌" class="error-icon" />
      <Label text="文章未找到" class="error-text" />
    </StackLayout>
  </Page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { Frame } from '@nativescript/core'
import { useStorage } from '../composables/useStorage'
import { usePlayer } from '../composables/usePlayer'
import type { Article } from '../types'

// Props
interface Props {
  articleId: string
}

const props = defineProps<Props>()

// 存储服务
const storage = useStorage()

// 播放器服务
const player = usePlayer()

// 响应式状态
const article = ref<Article | null>(null)
const isLoading = ref(false)
const error = ref<string | null>(null)

// 是否正在拖动进度条
const isDraggingProgress = ref(false)

/**
 * 加载文章数据
 */
const loadArticle = async () => {
  try {
    isLoading.value = true
    error.value = null

    await storage.initialize()
    article.value = await storage.getArticleById(props.articleId)

    if (!article.value) {
      error.value = '文章未找到'
      return
    }

    console.log('[PlayerView] Loaded article:', article.value.id)

    // 加载音频
    await player.loadAudio(props.articleId)
  } catch (err: any) {
    console.error('[PlayerView] Failed to load article:', err)
    error.value = err.message || String(err)
  } finally {
    isLoading.value = false
  }
}

/**
 * 重试加载
 */
const onRetry = () => {
  loadArticle()
}

/**
 * 返回按钮点击事件
 */
const onBackTap = async () => {
  // 停止播放并释放资源
  await player.dispose()
  Frame.topmost().goBack()
}

/**
 * 播放/暂停切换
 */
const onPlayPause = async () => {
  try {
    if (player.status.value === 'playing') {
      await player.pause()
    } else {
      await player.play()
    }
  } catch (err: any) {
    console.error('[PlayerView] Play/Pause error:', err)
  }
}

/**
 * 快退 15 秒
 */
const onSeekBackward = async () => {
  const newTime = Math.max(0, player.currentTime.value - 15)
  await player.seekTo(newTime)
}

/**
 * 快进 15 秒
 */
const onSeekForward = async () => {
  const newTime = Math.min(player.duration.value, player.currentTime.value + 15)
  await player.seekTo(newTime)
}

/**
 * 进度条值变化事件
 */
const onProgressChange = async (args: any) => {
  const newTime = Math.floor(args.value)

  // 防止播放时的自动更新触发 seekTo
  if (Math.abs(newTime - player.currentTime.value) > 1) {
    isDraggingProgress.value = true
    await player.seekTo(newTime)
    setTimeout(() => {
      isDraggingProgress.value = false
    }, 500)
  }
}

/**
 * 格式化时间（秒 → mm:ss）
 */
const formatTime = (seconds: number): string => {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = Math.floor(seconds % 60)
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
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

// 计算属性
const isPlaying = computed(() => player.status.value === 'playing')
const currentTime = computed(() => player.currentTime.value)
const duration = computed(() => player.duration.value)
const playerError = computed(() => player.error.value)

const playerStatusIcon = computed(() => {
  const status = player.status.value
  if (status === 'playing') return '🎵'
  if (status === 'paused') return '⏸️'
  if (status === 'loading') return '⏳'
  if (status === 'error') return '❌'
  return '🎧'
})

const playerStatusText = computed(() => {
  const status = player.status.value
  if (status === 'loading') return '加载音频中...'
  if (status === 'playing') return '播放中'
  if (status === 'paused') return '已暂停'
  if (status === 'stopped') return '已停止'
  if (status === 'error') return '播放出错'
  return '准备就绪'
})

// 监听播放完成
watch(
  () => player.status.value,
  (newStatus, oldStatus) => {
    if (oldStatus === 'playing' && newStatus === 'stopped') {
      console.log('[PlayerView] Playback completed')
    }
  },
)

// 页面加载时获取文章数据
onMounted(() => {
  loadArticle()
})
</script>

<style scoped>
/* 加载指示器 */
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

/* 错误状态 */
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
  font-size: 16;
  color: #ef4444;
  text-align: center;
  padding: 0 40;
  margin-bottom: 24;
}

.retry-button {
  background-color: #3b82f6;
  color: #ffffff;
  font-size: 16;
  border-radius: 8;
  padding: 12 24;
}

/* 播放器布局 */
.player-layout {
  background-color: #f9fafb;
}

/* 文章信息 */
.article-info {
  background-color: #ffffff;
  padding: 20 16;
  border-bottom-width: 1;
  border-bottom-color: #e5e7eb;
}

.article-title {
  font-size: 20;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 8;
  text-align: center;
}

.status-badge {
  font-size: 12;
  padding: 4 8;
  border-radius: 6;
  margin-top: 8;
  horizontal-align: center;
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

/* 播放器控制 */
.player-controls {
  padding: 0 24;
}

.player-icon {
  font-size: 80;
  text-align: center;
  margin-bottom: 32;
}

/* 进度条 */
.progress-container {
  margin-bottom: 32;
}

.progress-slider {
  margin-bottom: 8;
  background-color: #d1d5db;
  color: #3b82f6;
  height: 4;
}

.time-labels {
  flex-direction: row;
  justify-content: space-between;
}

.time-current,
.time-duration {
  font-size: 12;
  color: #6b7280;
}

/* 控制按钮 */
.control-buttons {
  flex-direction: row;
  justify-content: center;
  align-items: center;
  margin-bottom: 16;
}

.control-btn {
  background-color: #e5e7eb;
  color: #1f2937;
  font-size: 14;
  border-radius: 24;
  padding: 12 16;
  margin: 0 8;
  width: 80;
}

.control-btn:active {
  background-color: #d1d5db;
}

.play-pause-btn {
  background-color: #3b82f6;
  color: #ffffff;
  font-size: 32;
  border-radius: 40;
  width: 80;
  height: 80;
  margin: 0 16;
}

.play-pause-btn.playing {
  background-color: #2563eb;
}

.play-pause-btn:active {
  background-color: #1d4ed8;
}

/* 状态文本 */
.status-text {
  font-size: 14;
  color: #6b7280;
  text-align: center;
}

/* 译文容器 */
.transcript-container {
  background-color: #ffffff;
  border-top-width: 1;
  border-top-color: #e5e7eb;
  max-height: 200;
}

.transcript-content {
  padding: 16;
}

.transcript-header {
  font-size: 16;
  font-weight: bold;
  color: #1f2937;
  margin-bottom: 12;
}

.transcript-text {
  font-size: 14;
  color: #4b5563;
  line-height: 24;
}
</style>
