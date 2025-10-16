<template>
  <Page>
    <ActionBar title="Audiofy" />

    <StackLayout class="input-page">
      <!-- 标题 -->
      <Label text="输入英文文章" class="page-title" />

      <!-- 提示信息 -->
      <Label :text="hintText" class="hint-text" text-wrap="true" />

      <!-- 多行文本输入框 -->
      <TextView
        v-model="inputText"
        hint="Paste your English article here..."
        class="text-input"
        return-key-type="done"
        :editable="!isLoading"
      />

      <!-- 字符计数 -->
      <Label :text="charCountText" class="char-count" />

      <!-- 生成音频按钮 -->
      <Button
        text="生成音频"
        class="btn btn-primary"
        :is-enabled="!isLoading && isValidInput"
        @tap="handleGenerateAudio"
      />

      <!-- 加载指示器 -->
      <ActivityIndicator v-if="isLoading" :busy="true" class="loading-indicator" />

      <!-- 错误提示 -->
      <Label v-if="errorMessage" :text="errorMessage" class="error-text" text-wrap="true" />
    </StackLayout>
  </Page>
</template>

<script lang="ts" setup>
import { ref, computed, watch } from 'vue'
import { alert } from '@nativescript/core'
import { useStore } from 'vuex'
import type { RootState } from '@/stores'

// Vuex store
const store = useStore<RootState>()

// 响应式状态
const inputText = ref('')
const errorMessage = ref('')

// 计算属性
const isLoading = computed(() => store.getters.isLoading)
const storeError = computed(() => store.getters.getError)

const hintText = computed(() => {
  return '请粘贴 100-5000 字符的英文文章，我们将为您生成中文语音播客。'
})

const charCountText = computed(() => {
  const count = inputText.value.length
  return `${count} / 5000 字符`
})

const isValidInput = computed(() => {
  const len = inputText.value.trim().length
  return len >= 100 && len <= 5000
})

// 监听store错误
watch(storeError, (newError) => {
  if (newError) {
    errorMessage.value = newError
  }
})

// 方法
async function handleGenerateAudio() {
  // 清空之前的错误
  errorMessage.value = ''

  // 验证输入
  const text = inputText.value.trim()
  const len = text.length

  if (len === 0) {
    errorMessage.value = '请输入文本'
    return
  }

  if (len < 100) {
    errorMessage.value = `文本太短，最少需要 100 个字符（当前 ${len} 个）`
    return
  }

  if (len > 5000) {
    errorMessage.value = `文本太长，最多支持 5000 个字符（当前 ${len} 个）`
    return
  }

  try {
    // 调用 Vuex action 生成音频
    await store.dispatch('generateAudio', text)

    // 成功提示
    await alert({
      title: '成功',
      message: '音频已生成！',
      okButtonText: '好的',
    })

    // 清空输入
    inputText.value = ''

    // TODO: 跳转到音频库页面
    // this.$navigateTo(AudioLibraryView)
  } catch (error: any) {
    // 错误已经在 store 中设置，这里只显示 alert
    await alert({
      title: '生成失败',
      message: error.message || '未知错误，请重试',
      okButtonText: '好的',
    })
  }
}
</script>

<style scoped>
.input-page {
  padding: 20;
  background-color: #f5f5f5;
}

.page-title {
  font-size: 24;
  font-weight: bold;
  color: #333;
  margin-bottom: 10;
  text-align: center;
}

.hint-text {
  font-size: 14;
  color: #666;
  margin-bottom: 15;
  text-align: center;
}

.text-input {
  height: 300;
  border-width: 1;
  border-color: #ddd;
  border-radius: 8;
  padding: 10;
  font-size: 16;
  color: #333;
  background-color: #fff;
  margin-bottom: 10;
}

.char-count {
  font-size: 12;
  color: #999;
  text-align: right;
  margin-bottom: 20;
}

.btn {
  font-size: 18;
  color: #fff;
  border-radius: 8;
  padding: 15;
  margin-bottom: 15;
}

.btn-primary {
  background-color: #007aff;
}

.btn[isEnabled='false'] {
  background-color: #ccc;
}

.loading-indicator {
  margin-top: 20;
  margin-bottom: 20;
}

.error-text {
  font-size: 14;
  color: #ff3b30;
  text-align: center;
  margin-top: 10;
}
</style>
