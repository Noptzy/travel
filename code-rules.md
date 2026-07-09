# Code Rules — fypmedia

Mandatory rules for all generated and edited code in this monorepo. Apply to every `.ts`/`.tsx` file in `apps/api` and `apps/web`.

## 1. Zero Comments Policy

- **STRICTLY FORBIDDEN:** no comments of any kind inside code blocks.
- Remove all single-line (`//`), multi-line (`/* ... */`), and JSDoc (`/** ... */`) comments.
- No placeholder comments (`// add your logic here`, `// imports`). Write the code directly or leave the line blank.
- **Only exception:** functional tool directives required by Biome/ESLint/TypeScript — `// biome-ignore`, `// @ts-expect-error`, `// @ts-ignore`, `eslint-disable*`, shebangs, JSON `$schema`. Keep these only when the tool actually requires them.

## 2. Function & Symbol Naming

- Functions and variables use `camelCase`. Types/interfaces/components use `PascalCase`. Module-level constants use `UPPER_SNAKE_CASE`.
- Names are descriptive and explicit. Prefer `getGamblingBacklinks()` over `getData()`, `pendingScrapeJobs` over `rows`.
- Boolean names read as predicates (`isEnabled`, `hasPendingProposal`, `shouldPause`).
- Match existing naming in the surrounding module; do not rename unrelated symbols.

## 3. File Size Cap

- No file exceeds **350–450 LOC**. Target ≤350; 450 is a hard ceiling.
- When a file grows past the cap, extract cohesive sections into sibling modules in the same feature folder and re-export so public import paths stay stable.
- Excluded by nature: generated files (`routeTree.gen.ts`), data/seed files (`schema.ts`, `seed.ts`, `mock-data.ts`) — flag instead of force-splitting.

## 4. Architecture & Best Practices (kana-monorepo-fullstack-typescript)

- Respect the layer boundaries: `domain/` (pure types + ports, no framework imports) → `application/` (use-cases as pure functions of deps) → `infrastructure/` (concrete adapters: drizzle, redis, auth) → `presentation/` (oRPC routers, routes).
- Use-cases are pure functions of their dependencies; wire them through `buildUseCases(deps)`.
- Validate all external input with Zod at system boundaries.
- Immutable data only: return new objects, never mutate inputs.
- Biome style: tabs, double quotes, no semicolons. Run `pnpm biome check` before claiming green.
- Verify against the **build** config, not just dev: `pnpm build` (moon) must pass; `noUnusedLocals`/`noUnusedParameters` are enforced.

## 5. Clean Code & Self-Documenting

Because comments are forbidden, code must be 100% self-documenting.

- **Descriptive names** carry the intent that a comment otherwise would.
- **Early return** to avoid nested `if`s, callback hell, and the pyramid of doom. Guard clauses first, happy path last.
- **No magic numbers or magic strings.** Extract them into named module-level constants (e.g. `const STUCK_JOB_TIMEOUT_MS = 10 * 60_000`).
- Small focused functions; one concern each. Keep cyclomatic complexity low.

## 6. SOLID — Single Responsibility

- Each file has **1–3 responsibilities**. If a file serves more than 3 distinct concerns, split it into focused sibling modules.
- One file = one reason to change. A file handling auth validation + DB queries + email sending has 3 responsibilities — split.
- When unsure, count the distinct data flows or external integrations in the file. That count is the responsibility count.
- Extracted modules re-export from an index file so public import paths stay stable (same as File Size Cap rule).

## 7. Use Absolute Import
- Use absolute import instead of relative import
- Example:
  - `@api` at `@apps/api`
  - `@web` at `@apps/web`


## 8. Verification (per change)

- `pnpm build` (moon: api + web) — green.
- `pnpm --filter @fypmedia/api test` for API changes — green.
- `pnpm biome check <touched files>` — no issues.
- `npx fallow` — no unused exports, no circular deps.

## 9. React Hooks Policy

- **BANNED:** `useState`, `useReducer`, `useContext`, `useRef`, `useEffect`, `useMemo`, `useCallback` from React.
- These are low-level primitives. Use TanStack Query, TanStack Router, or Zustand instead.
- **Exception:** `useState` is allowed only for pure UI state (open/closed modals, form input keystrokes) with ≤3 values. If the state drives server calls or persists across navigation, move it to TanStack Query or Zustand.
- **Exception:** `useRef` is allowed only for DOM refs (`ref={}`) and animation targets (`useGSAP` refs). No storing mutable values.
- **Exception:** `useEffect` is allowed only for `window.addEventListener` cleanup or GSAP `ScrollTrigger.register()`. No data fetching, no localStorage sync, no subscription logic.

## 10. TanStack Query (Server State)

- All server-side data fetching, caching, and mutation MUST use TanStack Query.
- Wrap API calls in custom hooks (`useXxxQuery` / `useXxxMutation`), never raw `useQuery`/`useMutation` in components.
- `queryFn` must call a dedicated API function from the service layer, never inline `fetch`.
- Use `queryKey` arrays with typed keys: `['posts', { postId, status }]`. No magic strings.
- Invalidate related queries after mutations: `queryClient.invalidateQueries({ queryKey: ['posts'] })`.
- Use `select` option to transform data, not in-component `.map()` on raw server data.
- Handle `isPending`, `isError`, `isSuccess` states explicitly — never access `data` without status check.
- Use `staleTime` and `gcTime` intentionally. Default `staleTime: 0` is fine for real-time data; use longer for static data.

## 11. TanStack Router (Navigation)

- Define routes using `createFileRoute()` with the file's path literal. Do not use code-based routing.
- Use file-based routing convention: `apps/web/src/routes/` mirrors the URL hierarchy.
- Route components access params/search via `Route.useParams()` / `Route.useSearch()` — never `useParams()` or `useSearchParams()` from React Router.
- Use `loader` for data prefetching before component render. Loader data is available via `Route.useLoaderData()`.
- Nested layouts use the `__root.tsx` convention or layout route files — not manual `<Outlet>` wrapping.
- Navigation: `const navigate = useNavigate({ from: Route.fullPath })` for type-safe navigation.

## 12. Zod Schemas for API Boundaries

- Define a Zod schema for every API endpoint's request and response payload.
- Co-locate schemas with the route file or in a shared `schemas/` directory under the domain.
- Use `z.infer<typeof schema>` to derive TypeScript types — never hand-write duplicate interfaces.
- Validate incoming requests in the router handler: `schema.parse(body)` (or `.safeParse` with manual error handling).
- Export schemas for client-side reuse in query hooks, never re-defining types.
- Use Zod discriminated unions (`z.discriminatedUnion`) for variant payloads.

---

## 13. Workspace Layout

Monorepo structure:
```
apps/
  api/          # Hono backend
    src/
      domain/           # pure types + ports (NO framework imports)
      application/      # use-cases (pure functions of deps)
      infrastructure/   # concrete adapters (drizzle, redis, auth, env)
      presentation/     # oRPC routers, context, middleware
  web/          # SPA frontend
    src/
      components/ui/    # shadcn/ui primitives
      hooks/            # global app-wide hooks
      libs/             # orpc client, auth client, tanstack-query, clsx, errors
      routes/           # file-based routing (TanStack Router)
```

**Import rule:** web imports **types only** from `@api`. All runtime calls go over HTTP (`/rpc` or `/api`). Never import backend code into the frontend.

## 14. Backend — Hexagonal (Clean) Architecture

### 14.1 `domain/` — Framework-Free

One folder per aggregate: `user/`, `session/`, `activity/`, `ports/`.

- Entities: plain TS interfaces.
- `ports/`: interfaces for external systems (`AuthService`, `Cache`).
- **NO** `drizzle-orm`, `hono`, `better-auth`, or `ioredis` imports in `domain/`.

```ts
// domain/user/user.ts
export interface User { id: string; name: string; email: string; role: string; banned: boolean; createdAt: Date }

// domain/ports/cache.ts
export interface Cache {
  get<T>(key: string): Promise<T | null>
  set(key: string, value: unknown, ttlSeconds: number): Promise<void>
  del(...keys: string[]): Promise<void>
  ping(): Promise<boolean>
}
```

### 14.2 `application/` — Use-Case Factories

Every use-case: `makeX(deps) → (input, ctx) => Promise<Result>`.

```ts
export interface BanUserInput { userId: string; banReason?: string }
export interface BanUserDeps { auth: AuthService; memberRepo: MemberRepository }

export function makeBanUser(deps: BanUserDeps) {
  return async (input: BanUserInput, ctx: AuthedContext) => {
    assertNotSelf(ctx.session.user.id, input.userId, "ban")
    await deps.auth.banUser(input.userId, input.banReason, { headers: ctx.headers })
    return { success: true as const }
  }
}
```

- `application/shared/`: cross-cutting `context.ts`, `errors.ts`, `authorization.ts`.
- `application/use-cases.ts`: wires everything via `buildUseCases(deps): UseCases`.
- **All side-effects go through deps** — makes unit tests trivial with `vi.fn()`.
- Audit logs via `activityRepo.insert` inside the use-case, not at the router.

### 14.3 `application/shared/errors.ts` — Typed Errors

```ts
export type AppErrorCode = "UNAUTHORIZED" | "FORBIDDEN" | "NOT_FOUND" | "BAD_REQUEST" | "CONFLICT" | "INTERNAL_ERROR"
export class AppError extends Error {
  readonly code: AppErrorCode
  constructor(code: AppErrorCode, message: string) { super(message); this.code = code; this.name = "AppError" }
}
export const unauthorized = (m: string) => new AppError("UNAUTHORIZED", m)
export const forbidden = (m: string) => new AppError("FORBIDDEN", m)
```

oRPC middleware maps `AppError` → `ORPCError` using the same code strings.

### 14.4 `infrastructure/` — Concrete Adapters

One folder per capability: `auth/`, `cache/`, `config/`, `db/`, `observability/`.

- `db/client.ts`: `createDb(url)` using `drizzle-orm/node-postgres`.
- `db/repositories/*-repository.ts`: `create<Name>Repository(db)` — factory returns plain object matching domain interface.
- `config/env.ts`: **Zod-validated** env with `loadEnv()` that throws on invalid.
- Repositories never leak Drizzle types — return domain shapes.

### 14.5 `presentation/` — oRPC

- `orpc/middleware.ts`: `publicProcedure`, `protectedProcedure`, `requireRole(...)`, `requirePermission(...)`.
- `routers/<aggregate>.ts`: `buildXRouter(useCases.x)` — thin handlers that call use-cases.
- `routers/index.ts`: `buildRouter(useCases)` composing all routers. Export type `AppRouter`.

## 15. Frontend Routing (TanStack Router)

- File-based routing: `apps/web/src/routes/` mirrors URL hierarchy.
- **Layout = file + sibling folder pair.** `_authenticated.tsx` is the layout guard + `<Outlet/>`; `_authenticated/` holds children.
- Features start as single files (e.g. `dashboard.tsx`). Promote to folder (`dashboard/index.tsx` + `_apis/ _components/ _hooks/`) when the file outgrows readability.
- `routeFileIgnorePattern: "^(_apis|_components|_data|_hooks)"` — underscore folders invisible to router.
- `_authenticated.tsx` redirects to `/auth/login` when session is null.
- `routeTree.gen.ts` is generated — never edit, exclude from lint.

## 16. Database (Drizzle)

- Schema in `apps/api/src/infrastructure/db/schema.ts`.
- Dev: `pnpm db:push` (direct schema apply). Prod: `pnpm db:generate` → commit SQL → deploy runs `migrate.ts`.
- Seeding: `infrastructure/db/seed.ts` — idempotent, safe to re-run.
- Repositories never leak Drizzle types.

## 17. Testing

- Vitest, test files colocated as `*.test.ts` next to source.
- Unit test use-cases by passing fake deps built with `vi.fn()` — no DB, no HTTP.
- Cross-cutting helpers (`authorization.ts`, `errors.ts`) have their own `.test.ts`.

## 18. Adding a New Feature — Standard Workflow

1. **Domain:** `domain/<entity>/<entity>.ts` (interface), `domain/<entity>/<entity>-repository.ts` (interface).
2. **Schema:** add Drizzle table in `infrastructure/db/schema.ts`.
3. **Repository:** `infrastructure/db/repositories/<entity>-repository.ts`.
4. **Use-cases:** `application/<entity>/{list,create,update,delete}-<entity>.ts` as `makeX(deps)` factories. Colocate tests.
5. **Wire:** add to `application/use-cases.ts`.
6. **Presentation:** add Zod input schemas to `orpc/schemas.ts`, add `routers/<entity>.ts`, compose in `routers/index.ts`.
7. **Composition root:** add repository in `main.ts`, pass into `buildUseCases`.
8. **Migration:** `pnpm db:generate`, commit SQL, `pnpm db:push` locally.
9. **Frontend route:** `routes/_authenticated/$orgSlug/<entity>.tsx` using `useQuery(orpc.admin.list<Entity>.queryOptions())`.
10. **Role gate:** enforce in `beforeLoad` on the page AND `requireRole`/`requirePermission` on the router.
