import { test, expect, type Page } from '@playwright/test'

type Task = {
  id: string
  title: string
  description: string | null
  status: string
  assigneeId: string | null
  createdAt: string
  updatedAt: string
}

type User = { id: string; name: string; email: string | null }

const DEFAULT_USERS: User[] = [
  { id: 'user-alice', name: 'Alice', email: 'alice@example.com' },
  { id: 'user-bob', name: 'Bob', email: 'bob@example.com' },
]

/**
 * Installs mock handlers for the Task + User API, backed by an in-memory list
 * that mutates like the real backend (create appends, status/assignee updates).
 * No Spring Boot is required — Playwright intercepts the requests before they
 * hit the network/dev-proxy.
 */
async function mockTaskApi(page: Page, initial: Task[] = [], users: User[] = DEFAULT_USERS): Promise<void> {
  const tasks: Task[] = initial.map((t) => ({ ...t }))
  let seq = tasks.length

  // GET /api/users (seeded users that can be assigned)
  await page.route('**/api/users', async (route) => {
    await route.fulfill({ status: 200, json: users })
  })

  // PATCH /api/tasks/{id}/status  (register the more specific route first)
  await page.route('**/api/tasks/*/status', async (route) => {
    const id = route.request().url().match(/\/api\/tasks\/([^/]+)\/status/)![1]
    const { status } = route.request().postDataJSON() as { status: string }
    const task = tasks.find((t) => t.id === id)
    if (!task) {
      await route.fulfill({ status: 404, body: '' })
      return
    }
    task.status = status
    task.updatedAt = new Date().toISOString()
    await route.fulfill({ status: 200, json: task })
  })

  // PATCH (assign) / DELETE (unassign) /api/tasks/{id}/assignee
  await page.route('**/api/tasks/*/assignee', async (route) => {
    const id = route.request().url().match(/\/api\/tasks\/([^/]+)\/assignee/)![1]
    const task = tasks.find((t) => t.id === id)
    if (!task) {
      await route.fulfill({ status: 404, body: '' })
      return
    }
    if (route.request().method() === 'DELETE') {
      task.assigneeId = null
    } else {
      const { assigneeId } = route.request().postDataJSON() as { assigneeId: string }
      if (!users.some((u) => u.id === assigneeId)) {
        await route.fulfill({ status: 422, body: '' })
        return
      }
      task.assigneeId = assigneeId
    }
    task.updatedAt = new Date().toISOString()
    await route.fulfill({ status: 200, json: task })
  })

  // GET (list) and POST (create) /api/tasks
  await page.route('**/api/tasks', async (route) => {
    const req = route.request()
    if (req.method() === 'GET') {
      await route.fulfill({ status: 200, json: tasks })
      return
    }
    if (req.method() === 'POST') {
      const body = req.postDataJSON() as { title: string; description: string | null }
      const now = new Date().toISOString()
      const created: Task = {
        id: `task-${++seq}`,
        title: body.title,
        description: body.description ?? null,
        status: 'PENDING',
        assigneeId: null,
        createdAt: now,
        updatedAt: now,
      }
      tasks.push(created)
      await route.fulfill({ status: 201, json: created })
      return
    }
    await route.fallback()
  })
}

function task(overrides: Partial<Task> = {}): Task {
  const now = new Date().toISOString()
  return {
    id: 'task-1',
    title: 'Existing task',
    description: null,
    status: 'PENDING',
    assigneeId: null,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  }
}

test('shows the empty state when there are no tasks', async ({ page }) => {
  await mockTaskApi(page, [])
  await page.goto('/')

  await expect(page.getByRole('heading', { name: 'Event-Driven Tasks' })).toBeVisible()
  await expect(page.getByText('No tasks yet. Create one above.')).toBeVisible()
  await expect(page.locator('li.task')).toHaveCount(0)
})

test('lists tasks returned by the API', async ({ page }) => {
  await mockTaskApi(page, [
    task({ id: 'task-1', title: 'Write docs', description: 'the README', status: 'IN_PROGRESS' }),
    task({ id: 'task-2', title: 'Ship it', status: 'PENDING' }),
  ])
  await page.goto('/')

  await expect(page.locator('li.task')).toHaveCount(2)
  await expect(page.getByText('Write docs')).toBeVisible()
  await expect(page.getByText('the README')).toBeVisible()

  const item = page.locator('li.task', { hasText: 'Write docs' })
  await expect(item.getByLabel('Status')).toHaveValue('IN_PROGRESS')
  await expect(item).toHaveClass(/status-in_progress/)
})

test('creates a task through the form', async ({ page }) => {
  await mockTaskApi(page, [])
  await page.goto('/')

  await page.getByLabel('Task title').fill('Buy milk')
  await page.getByLabel('Description').fill('2 litres')
  await page.getByRole('button', { name: 'Add task' }).click()

  const item = page.locator('li.task', { hasText: 'Buy milk' })
  await expect(item).toBeVisible()
  await expect(item.getByText('2 litres')).toBeVisible()
  await expect(item.getByLabel('Status')).toHaveValue('PENDING')
  await expect(page.getByText('No tasks yet. Create one above.')).toHaveCount(0)
})

test('changes a task status', async ({ page }) => {
  await mockTaskApi(page, [task({ id: 'task-1', title: 'Refactor', status: 'PENDING' })])
  await page.goto('/')

  const item = page.locator('li.task', { hasText: 'Refactor' })
  await item.getByLabel('Status').selectOption('DONE')

  await expect(item.getByLabel('Status')).toHaveValue('DONE')
  await expect(item).toHaveClass(/status-done/)
})

test('assigns and unassigns a task to a user', async ({ page }) => {
  await mockTaskApi(page, [task({ id: 'task-1', title: 'Assign me' })])
  await page.goto('/')

  const item = page.locator('li.task', { hasText: 'Assign me' })
  const assignee = item.getByLabel('Assignee')
  await expect(assignee).toHaveValue('')

  // assign to Alice
  await assignee.selectOption({ label: 'Alice' })
  await expect(assignee).toHaveValue('user-alice')

  // unassign
  await assignee.selectOption({ label: 'Unassigned' })
  await expect(assignee).toHaveValue('')
})

test('lists an existing assignee', async ({ page }) => {
  await mockTaskApi(page, [task({ id: 'task-1', title: 'Owned', assigneeId: 'user-bob' })])
  await page.goto('/')

  const item = page.locator('li.task', { hasText: 'Owned' })
  await expect(item.getByLabel('Assignee')).toHaveValue('user-bob')
})

test('shows an error when loading fails', async ({ page }) => {
  await page.route('**/api/tasks', (route) => route.fulfill({ status: 500, body: '' }))
  await page.goto('/')

  await expect(page.locator('.error')).toBeVisible()
  await expect(page.locator('.error')).toContainText('500')
})
