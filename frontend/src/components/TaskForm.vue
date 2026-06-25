<script setup lang="ts">
import { Option } from 'effect'
import { ref } from 'vue'

const emit = defineEmits<{
  (e: 'create', payload: { title: string; description: Option.Option<string> }): void
}>()

const title = ref('')
const description = ref('')

function submit() {
  const t = title.value.trim()
  if (!t) return
  emit('create', {
    title: t,
    description: Option.liftPredicate(description.value.trim(), (s) => s.length > 0),
  })
  title.value = ''
  description.value = ''
}
</script>

<template>
  <form class="task-form" @submit.prevent="submit">
    <input v-model="title" placeholder="Task title" aria-label="Task title" required />
    <input v-model="description" placeholder="Description (optional)" aria-label="Description" />
    <button type="submit">Add task</button>
  </form>
</template>

<style scoped>
.task-form {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}
.task-form input {
  padding: 0.5rem 0.75rem;
  border: 1px solid #cbd2d9;
  border-radius: 6px;
}
.task-form input:first-child {
  flex: 1;
}
.task-form input:nth-child(2) {
  flex: 2;
}
.task-form button {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  background: #3182ce;
  color: white;
}
.task-form button:hover {
  background: #2b6cb0;
}
</style>
