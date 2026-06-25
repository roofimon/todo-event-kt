import { Effect, Option } from 'effect'
import type { CreateTaskRequest, Task, TaskStatus } from '../types'
import { type ApiError, jsonInit, request } from './http'

const BASE = '/api/tasks'

/** Raw JSON shape from the backend (optional fields are nullable on the wire). */
interface RawTask {
  id: string
  title: string
  description: string | null
  status: TaskStatus
  assigneeId: string | null
  createdAt: string
  updatedAt: string
}

const toTask = (r: RawTask): Task => ({
  ...r,
  description: Option.fromNullable(r.description),
  assigneeId: Option.fromNullable(r.assigneeId),
})

export const tasksApi = {
  list: (): Effect.Effect<Task[], ApiError> =>
    request<RawTask[]>(BASE).pipe(Effect.map((rs) => rs.map(toTask))),

  create: (body: CreateTaskRequest): Effect.Effect<Task, ApiError> =>
    request<RawTask>(
      BASE,
      jsonInit('POST', { title: body.title, description: Option.getOrNull(body.description) }),
    ).pipe(Effect.map(toTask)),

  changeStatus: (id: string, status: TaskStatus): Effect.Effect<Task, ApiError> =>
    request<RawTask>(`${BASE}/${id}/status`, jsonInit('PATCH', { status })).pipe(Effect.map(toTask)),

  assign: (id: string, assigneeId: string): Effect.Effect<Task, ApiError> =>
    request<RawTask>(`${BASE}/${id}/assignee`, jsonInit('PATCH', { assigneeId })).pipe(Effect.map(toTask)),

  unassign: (id: string): Effect.Effect<Task, ApiError> =>
    request<RawTask>(`${BASE}/${id}/assignee`, { method: 'DELETE' }).pipe(Effect.map(toTask)),
}
