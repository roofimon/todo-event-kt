<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { tasksApi } from './api/tasks'
import type { Task, TaskStatus } from './types'
import TaskForm from './components/TaskForm.vue'
import TaskItem from './components/TaskItem.vue'

const tasks = ref<Task[]>([])
const error = ref<string | null>(null)
const loading = ref(false)

async function load() {
  loading.value = true
  error.value = null
  try {
    tasks.value = await tasksApi.list()
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

async function createTask(payload: { title: string; description: string | null }) {
  try {
    const created = await tasksApi.create(payload)
    tasks.value = [...tasks.value, created]
  } catch (e) {
    error.value = (e as Error).message
  }
}

async function changeStatus(id: string, status: TaskStatus) {
  try {
    const updated = await tasksApi.changeStatus(id, status)
    tasks.value = tasks.value.map((t) => (t.id === id ? updated : t))
  } catch (e) {
    error.value = (e as Error).message
  }
}

onMounted(load)
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

    <p v-if="error" class="error">⚠️ {{ error }}</p>

    <p v-if="!tasks.length && !loading" class="empty">No tasks yet. Create one above.</p>

    <ul class="task-list">
      <TaskItem v-for="task in tasks" :key="task.id" :task="task" @status-change="changeStatus" />
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
