import type { CreateTaskRequest, Task, TaskStatus } from '../types'

const BASE = '/api/tasks'

async function json<T>(res: Response): Promise<T> {
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return res.json() as Promise<T>
}

export const tasksApi = {
  list(): Promise<Task[]> {
    return fetch(BASE).then((r) => json<Task[]>(r))
  },

  create(body: CreateTaskRequest): Promise<Task> {
    return fetch(BASE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    }).then((r) => json<Task>(r))
  },

  changeStatus(id: string, status: TaskStatus): Promise<Task> {
    return fetch(`${BASE}/${id}/status`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status }),
    }).then((r) => json<Task>(r))
  },

  assign(id: string, assigneeId: string): Promise<Task> {
    return fetch(`${BASE}/${id}/assignee`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ assigneeId }),
    }).then((r) => json<Task>(r))
  },

  unassign(id: string): Promise<Task> {
    return fetch(`${BASE}/${id}/assignee`, { method: 'DELETE' }).then((r) => json<Task>(r))
  },
}
