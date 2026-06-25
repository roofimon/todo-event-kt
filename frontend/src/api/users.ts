import { Effect, Option } from 'effect'
import type { User } from '../types'
import { type ApiError, request } from './http'

/** Raw JSON shape from the backend (email is nullable on the wire). */
interface RawUser {
  id: string
  name: string
  email: string | null
}

const toUser = (r: RawUser): User => ({ ...r, email: Option.fromNullable(r.email) })

export const usersApi = {
  list: (): Effect.Effect<User[], ApiError> =>
    request<RawUser[]>('/api/users').pipe(Effect.map((rs) => rs.map(toUser))),
}
