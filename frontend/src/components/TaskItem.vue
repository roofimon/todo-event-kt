<script setup lang="ts">
import { Option } from 'effect'
import { computed } from 'vue'
import { TASK_STATUSES, type Task, type TaskStatus, type User } from '../types'

const props = defineProps<{ task: Task; users: User[] }>()
const emit = defineEmits<{
  (e: 'status-change', id: string, status: TaskStatus): void
  (e: 'assign', id: string, assigneeId: string): void
  (e: 'unassign', id: string): void
}>()

const description = computed(() => Option.getOrUndefined(props.task.description))
// The <select> needs a string value; '' is the DOM representation of "Unassigned".
const selectedAssignee = computed(() => Option.getOrElse(props.task.assigneeId, () => ''))

function onChange(task: Task, event: Event) {
  const status = (event.target as HTMLSelectElement).value as TaskStatus
  if (status !== task.status) emit('status-change', task.id, status)
}

function onAssigneeChange(task: Task, event: Event) {
  const assigneeId = (event.target as HTMLSelectElement).value
  if (assigneeId === Option.getOrElse(task.assigneeId, () => '')) return
  if (assigneeId.length === 0) emit('unassign', task.id)
  else emit('assign', task.id, assigneeId)
}
</script>

<template>
  <li class="task" :class="`status-${task.status.toLowerCase()}`">
    <div class="main">
      <span class="title">{{ task.title }}</span>
      <span v-if="description" class="description">{{ description }}</span>
    </div>
    <div class="controls">
      <select
        :value="selectedAssignee"
        @change="onAssigneeChange(task, $event)"
        aria-label="Assignee"
      >
        <option value="">Unassigned</option>
        <option v-for="u in users" :key="u.id" :value="u.id">{{ u.name }}</option>
      </select>
      <select :value="task.status" @change="onChange(task, $event)" aria-label="Status">
        <option v-for="s in TASK_STATUSES" :key="s" :value="s">{{ s }}</option>
      </select>
    </div>
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
.controls {
  display: flex;
  gap: 0.5rem;
}
select {
  padding: 0.35rem 0.5rem;
  border: 1px solid #cbd2d9;
  border-radius: 6px;
}
</style>
