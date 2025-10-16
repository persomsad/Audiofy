/**
 * Vuex 4 + Vue 3 类型定义
 */

import { Store } from 'vuex'
import { RootState } from '@/stores'

// 扩展 ComponentCustomProperties 以支持 $store
declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    $store: Store<RootState>
  }
}
