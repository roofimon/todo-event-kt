<script setup lang="ts">
import { Effect, Option } from 'effect'
import { computed, onMounted, ref } from 'vue'
import { tasksApi } from './api/tasks'
import { usersApi } from './api/users'
import type { ApiError } from './api/http'
import type { Task, TaskStatus, User } from './types'
import TaskForm from './components/TaskForm.vue'
import TaskItem from './components/TaskItem.vue'

const tasks = ref<Task[]>([])
const users = ref<User[]>([])
const error = ref<Option.Option<string>>(Option.none())
const loading = ref(false)

const errorText = computed(() => Option.getOrUndefined(error.value))

const fail = (e: ApiError) => {
  error.value = Option.some(e.message)
}

/** Replaces the task with the same id with the server's updated copy. */
const replaceTask = (updated: Task) => {
  tasks.value = tasks.value.map((t) => (t.id === updated.id ? updated : t))
}

function load() {
  loading.value = true
  error.value = Option.none()
  Effect.runPromise(
    tasksApi.list().pipe(Effect.match({ onFailure: fail, onSuccess: (t) => (tasks.value = t) })),
  ).finally(() => (loading.value = false))
}

function loadUsers() {
  // Assignee options are best-effort; a failure here shouldn't block the task list.
  Effect.runPromise(
    usersApi.list().pipe(Effect.match({ onFailure: () => (users.value = []), onSuccess: (u) => (users.value = u) })),
  )
}

function createTask(payload: { title: string; description: Option.Option<string> }) {
  Effect.runPromise(
    tasksApi.create(payload).pipe(
      Effect.match({ onFailure: fail, onSuccess: (created) => (tasks.value = [...tasks.value, created]) }),
    ),
  )
}

function changeStatus(id: string, status: TaskStatus) {
  Effect.runPromise(
    tasksApi.changeStatus(id, status).pipe(Effect.match({ onFailure: fail, onSuccess: replaceTask })),
  )
}

function assign(id: string, assigneeId: string) {
  Effect.runPromise(
    tasksApi.assign(id, assigneeId).pipe(Effect.match({ onFailure: fail, onSuccess: replaceTask })),
  )
}

function unassign(id: string) {
  Effect.runPromise(tasksApi.unassign(id).pipe(Effect.match({ onFailure: fail, onSuccess: replaceTask })))
}

onMounted(() => {
  load()
  loadUsers()
})
</script>

<template>
  <main class="container">
    <header>
      <h1>Event-Driven Tasks</h1>
      <button class="refresh" @click="load" :disabled="loading">
        {{ loading ? 'Loading…' : 'Refresh' }}
      </button>
    </header>

    <TaskForm @create="createTask" />

    <p v-if="errorText" class="error">⚠️ {{ errorText }}</p>

    <p v-if="!tasks.length && !loading" class="empty">No tasks yet. Create one above.</p>

    <ul class="task-list">
      <TaskItem
        v-for="task in tasks"
        :key="task.id"
        :task="task"
        :users="users"
        @status-change="changeStatus"
        @assign="assign"
        @unassign="unassign"
      />
    </ul>
  </main>
</template>

<style scoped>
.container {
  max-width: 640px;
  margin: 2rem auto;
  padding: 0 1rem;
}
header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}
h1 {
  font-size: 1.5rem;
  margin: 0;
}
.refresh {
  padding: 0.4rem 0.9rem;
  border: 1px solid #cbd2d9;
  border-radius: 6px;
  background: white;
}
.task-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.error {
  color: #c53030;
  background: #fff5f5;
  border: 1px solid #feb2b2;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
}
.empty {
  color: #616e7c;
  text-align: center;
}
</style>
