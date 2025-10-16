/**
 * Vuex Store 类型定义
 */

import { Store } from 'vuex'
import { RootState } from '@/stores'

declare module 'vue/types/vue' {
  interface Vue {
    $store: Store<RootState>
  }
}

declare module 'vue/types/options' {
  interface ComponentOptions<_V extends Vue> {
    store?: Store<RootState>
  }
}
