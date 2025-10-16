/**
 * Vitest 配置文件
 */

import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'happy-dom',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: ['node_modules/', 'src/app.ts', '**/*.d.ts', '**/*.config.ts', 'dist/'],
    },
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
      '@nativescript/secure-storage': resolve(
        __dirname,
        './src/__mocks__/@nativescript/secure-storage.ts',
      ),
      'nativescript-audio': resolve(__dirname, './src/__mocks__/nativescript-audio.ts'),
    },
  },
})
