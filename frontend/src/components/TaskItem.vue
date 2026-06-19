<script setup lang="ts">
import { TASK_STATUSES, type Task, type TaskStatus } from '../types'

defineProps<{ task: Task }>()
const emit = defineEmits<{ (e: 'status-change', id: string, status: TaskStatus): void }>()

function onChange(task: Task, event: Event) {
  const status = (event.target as HTMLSelectElement).value as TaskStatus
  if (status !== task.status) emit('status-change', task.id, status)
}
</script>

<template>
  <li class="task" :class="`status-${task.status.toLowerCase()}`">
    <div class="main">
      <span class="title">{{ task.title }}</span>
      <span v-if="task.description" class="description">{{ task.description }}</span>
    </div>
    <select :value="task.status" @change="onChange(task, $event)" aria-label="Status">
      <option v-for="s in TASK_STATUSES" :key="s" :value="s">{{ s }}</option>
    </select>
  </li>
</template>

<style scoped>
.task {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.75rem 1rem;
  background: white;
  border: 1px solid #e4e7eb;
  border-left: 4px solid #cbd2d9;
  border-radius: 6px;
  margin-bottom: 0.5rem;
}
.status-in_progress {
  border-left-color: #3182ce;
}
.status-done {
  border-left-color: #38a169;
}
.status-cancelled {
  border-left-color: #e53e3e;
  opacity: 0.6;
}
.main {
  display: flex;
  flex-direction: column;
}
.title {
  font-weight: 600;
}
.description {
  font-size: 0.85rem;
  color: #616e7c;
}
select {
  padding: 0.35rem 0.5rem;
  border: 1px solid #cbd2d9;
  border-radius: 6px;
}
</style>
