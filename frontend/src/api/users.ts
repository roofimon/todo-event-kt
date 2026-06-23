import type { User } from '../types'

const BASE = '/api/users'

async function json<T>(res: Response): Promise<T> {
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`)
  }
  return res.json() as Promise<T>
}

export const usersApi = {
  list(): Promise<User[]> {
    return fetch(BASE).then((r) => json<User[]>(r))
  },
}
