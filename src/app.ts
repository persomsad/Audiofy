import Vue from 'nativescript-vue'
import InputView from './views/InputView.vue'
import store from './stores'

declare let __DEV__: boolean

// Prints Vue logs when --env.production is *NOT* set while building
Vue.config.silent = !__DEV__

new Vue({
  store,
  render: (h: any) => h('frame', [h(InputView)]),
}).$start()
