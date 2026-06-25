import type { Option } from 'effect'

export type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED'

export const TASK_STATUSES: TaskStatus[] = ['PENDING', 'IN_PROGRESS', 'DONE', 'CANCELLED']

export interface Task {
  id: string
  title: string
  description: Option.Option<string>
  status: TaskStatus
  assigneeId: Option.Option<string>
  createdAt: string
  updatedAt: string
}

export interface CreateTaskRequest {
  title: string
  description: Option.Option<string>
}

export interface User {
  id: string
  name: string
  email: Option.Option<string>
}
