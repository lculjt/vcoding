import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: [
      {
        find: '@vcoding/ui/styles',
        replacement: fileURLToPath(new URL('../../packages/ui/src/styles/index.css', import.meta.url))
      },
      {
        find: '@vcoding/ui',
        replacement: fileURLToPath(new URL('../../packages/ui/src/index.ts', import.meta.url))
      },
      {
        find: '@vcoding/auth-client',
        replacement: fileURLToPath(new URL('../../packages/auth-client/src/index.ts', import.meta.url))
      },
      {
        find: '@vcoding/shared',
        replacement: fileURLToPath(new URL('../../packages/shared/src/index.ts', import.meta.url))
      }
    ]
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
