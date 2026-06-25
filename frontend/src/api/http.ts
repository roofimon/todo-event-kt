import { Data, Effect } from 'effect'

/** Typed failure for any API call (network error or non-2xx response). */
export class ApiError extends Data.TaggedError('ApiError')<{ message: string }> {}

const toApiError = (e: unknown): ApiError =>
  new ApiError({ message: e instanceof Error ? e.message : String(e) })

/**
 * Performs an HTTP request as an Effect: network failures and non-2xx responses
 * become a typed `ApiError`; a 2xx response is parsed as JSON into `A`.
 */
export function request<A>(url: string, init?: RequestInit): Effect.Effect<A, ApiError> {
  return Effect.tryPromise({ try: () => fetch(url, init), catch: toApiError }).pipe(
    Effect.flatMap((res) =>
      res.ok
        ? Effect.tryPromise({ try: () => res.json() as Promise<A>, catch: toApiError })
        : Effect.fail(new ApiError({ message: `${res.status} ${res.statusText}` })),
    ),
  )
}

/** Builds a JSON request init for a body-carrying method. */
export const jsonInit = (method: string, body: unknown): RequestInit => ({
  method,
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(body),
})
