/// <reference types="vitest/config" />
import { defineConfig } from 'vite'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [
    tailwindcss(),
  ],
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    coverage: {
      provider: 'v8',
      include: ['src/**/*.{ts,tsx}'],
      exclude: [
        'src/**/*.test.{ts,tsx}',
        'src/test/**',
        // bootstrap ReactDOM — análogo à classe Application no backend
        'src/main.tsx',
      ],
      thresholds: {
        lines: 90,
      },
    },
  },
})
