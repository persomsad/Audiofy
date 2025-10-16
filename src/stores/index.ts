/**
 * Vuex 4 Store - 音频状态管理
 * 兼容 Vue 3
 */

import { createStore } from 'vuex'
import { TranslateService } from '@/services/translate.service'
import { TTSService } from '@/services/tts.service'

export interface AudioItem {
  id: string
  originalText: string
  translatedText: string
  audioData: string // Base64 编码
  duration: number
  createdAt: string
}

export interface RootState {
  audioList: AudioItem[]
  loading: boolean
  error: string | null
}

export default createStore<RootState>({
  state: {
    audioList: [],
    loading: false,
    error: null,
  },

  mutations: {
    SET_LOADING(state, loading: boolean) {
      state.loading = loading
    },

    SET_ERROR(state, error: string | null) {
      state.error = error
    },

    ADD_AUDIO_ITEM(state, item: AudioItem) {
      state.audioList.unshift(item) // 最新的放在前面
    },

    CLEAR_ERROR(state) {
      state.error = null
    },
  },

  actions: {
    /**
     * 生成音频播客
     * @param commit
     * @param text 英文原文
     */
    async generateAudio({ commit }, text: string) {
      commit('SET_LOADING', true)
      commit('CLEAR_ERROR')

      try {
        // 步骤1: 调用翻译服务
        const translatedText = await TranslateService.translate(text)

        // 步骤2: 调用 TTS 服务
        const { audioData, duration } = await TTSService.textToSpeech(translatedText)

        // 步骤3: 保存音频项
        const audioItem: AudioItem = {
          id: Date.now().toString(),
          originalText: text,
          translatedText,
          audioData,
          duration,
          createdAt: new Date().toISOString(),
        }

        commit('ADD_AUDIO_ITEM', audioItem)
        commit('SET_LOADING', false)

        return audioItem
      } catch (error: any) {
        const errorMessage = error.message || 'Unknown error occurred'
        commit('SET_ERROR', errorMessage)
        commit('SET_LOADING', false)
        throw error
      }
    },
  },

  getters: {
    isLoading: (state) => state.loading,
    getError: (state) => state.error,
    getAudioList: (state) => state.audioList,
  },
})
