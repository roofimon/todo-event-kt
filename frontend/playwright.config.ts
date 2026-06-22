import { defineConfig, devices } from '@playwright/test'

/**
 * UI end-to-end tests. The Spring backend is NOT required: the frontend's
 * /api/tasks calls are intercepted and mocked per test (see e2e/tasks.spec.ts),
 * so these tests exercise the Vue UI deterministically. Playwright boots only
 * the Vite dev server.
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: 'list',
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
  },
})
