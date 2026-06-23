export type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED'

export const TASK_STATUSES: TaskStatus[] = ['PENDING', 'IN_PROGRESS', 'DONE', 'CANCELLED']

export interface Task {
  id: string
  title: string
  description: string | null
  status: TaskStatus
  assigneeId: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateTaskRequest {
  title: string
  description?: string | null
}

export interface User {
  id: string
  name: string
  email: string | null
}
