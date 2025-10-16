/**
 * NativeScript-Vue 3.x 应用入口
 */

import { createApp } from 'nativescript-vue'
import InputView from './views/InputView.vue'
import store from './stores'

declare let __DEV__: boolean

// 创建Vue 3应用实例
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const app = createApp(InputView as any)

// 注册Vuex store
app.use(store)

// 开发模式配置
if (__DEV__) {
  app.config.performance = true
}

// 启动应用
app.start()
