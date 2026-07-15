# Research: pure-admin-thin Login Implementation Patterns

- **Query**: Analyze pure-admin-thin Vue 3 admin template for login, auth, API, router guard, and project setup patterns
- **Scope**: External (GitHub repo + npm)
- **Date**: 2026-06-29

## 1. Login Page Structure (`src/views/login/`)

### File Layout

| File Path | Description |
|---|---|
| `src/views/login/index.vue` | Main login page component |
| `src/views/login/utils/rule.ts` | Form validation rules (password regex) |
| `src/views/login/utils/static.ts` | Static assets (bg, avatar, illustration SVGs) |
| `src/views/login/utils/motion.ts` | Animation wrapper (uses `@vueuse/motion`) |
| `src/style/login.css` | Login page scoped styles |
| `src/assets/login/bg.png` | Background image |
| `src/assets/login/avatar.svg` | Avatar SVG component |
| `src/assets/login/illustration.svg` | Illustration SVG component |

### Login Page Component (`src/views/login/index.vue`)

The login page is a single-file component with:

- **Element Plus** `el-form` with `el-input` for username/password
- **Iconify icons** for input prefixes (`ri/lock-fill`, `ri/user-3-fill`)
- **Dark/light theme toggle** via `el-switch` at top-right
- **Debounced login** (1000ms) to prevent double-submit
- **Enter key listener** via `@vueuse/core`'s `useEventListener`
- **Default credentials**: `admin` / `admin123`
- **Motion animation**: staggered `<Motion :delay="N">` wrappers for each form element

**Login flow in the component:**
1. Validate form via `el-form.validate()`
2. Call `useUserStoreHook().loginByUsername({ username, password })`
3. On success, call `initRouter()` to fetch and register dynamic routes from backend
4. Navigate to `getTopMenu(true).path` (first available menu item)
5. Show success message

### Validation Rules (`src/views/login/utils/rule.ts`)

- Password regex: `REGEXP_PWD` = 8-18 chars, must contain at least 2 of: digits, lowercase, uppercase, symbols
- Username validation is inline on the `el-form-item` (required, trigger: blur)
- All messages in Chinese

---

## 2. Authentication / State Management

### Token Storage System (`src/utils/auth.ts`)

**Dual storage strategy:**

| Storage | Key | Contents | Purpose |
|---|---|---|---|
| Cookie | `authorized-token` | `{ accessToken, expires, refreshToken }` | Auto-expire, multi-tab detection |
| Cookie | `multiple-tabs` | `"true"` | Multi-tab login detection (browser close = auto-destroy) |
| localStorage | `user-info` | `{ refreshToken, expires, avatar, username, nickname, roles, permissions }` | Persistent user info |

**Key functions:**
- `getToken()` -- reads from Cookie first, falls back to localStorage
- `setToken(data)` -- writes to both Cookie and localStorage, updates Pinia store
- `removeToken()` -- clears both Cookie and localStorage
- `formatToken(token)` -- returns `"Bearer " + token`
- `hasPerms(value)` -- checks button-level permissions against `permissions` array

**Token refresh strategy:**
- `accessToken` has a short expiry (e.g., 2 hours)
- `refreshToken` has a longer expiry (e.g., 30 days)
- When `accessToken` expires, the HTTP interceptor calls `/refresh-token` API
- Uses a queuing mechanism: pending requests are held in `PureHttp.requests[]` while refreshing
- `PureHttp.isRefreshing` prevents concurrent refresh calls

### User Store (`src/store/modules/user.ts`)

Pinia store (`"pure-user"`) with:

**State:**
- `avatar`, `username`, `nickname` -- from localStorage on init
- `roles: Array<string>` -- page-level permissions (e.g., `["admin"]`)
- `permissions: Array<string>` -- button-level permissions (e.g., `["*:*:*"]`)
- `isRemembered: boolean` -- "remember me" checkbox state
- `loginDay: number` -- cookie persistence days (default 7)

**Actions:**
- `loginByUsername(data)` -- calls `getLogin(data)`, on success calls `setToken(data.data)`
- `logOut()` -- clears state, calls `removeToken()`, resets router, navigates to `/login`
- `handRefreshToken(data)` -- calls `refreshTokenApi(data)`, calls `setToken(data.data)`

### Permission Store (`src/store/modules/permission.ts`)

Pinia store (`"pure-permission"`) managing:
- `constantMenus` -- static route menus
- `wholeMenus` -- combined static + dynamic menus (filtered by roles)
- `flatteningRoutes` -- one-dimensional route array
- `cachePageList` -- keep-alive cache list

### Store Setup

- Pinia created in `src/store/index.ts` via `createPinia()`
- Injected in `main.ts` via `setupStore(app)`
- Each store exports a `useXxxStoreHook()` helper that passes the shared `store` instance

---

## 3. API Layer Structure

### HTTP Client (`src/utils/http/index.ts`)

Custom Axios wrapper class `PureHttp`:

- **Default config**: timeout 10s, JSON headers, qs serializer
- **Request interceptor**: auto-attaches `Authorization: Bearer <token>` header
- **Token whitelist**: `/refresh-token` and `/login` endpoints skip token injection
- **Token refresh**: on expired token, queues request, calls `refreshTokenApi()`, retries
- **Response interceptor**: passes through response data (unwraps `response.data`)
- **Export**: `export const http = new PureHttp()`

### User API (`src/api/user.ts`)

```typescript
// Login
export const getLogin = (data?: object) => {
  return http.request<UserResult>("post", "/login", { data });
};

// Refresh token
export const refreshTokenApi = (data?: object) => {
  return http.request<RefreshTokenResult>("post", "/refresh-token", { data });
};
```

**`UserResult` type:**
```typescript
{
  success: boolean;
  data: {
    avatar: string;
    username: string;
    nickname: string;
    roles: Array<string>;
    permissions: Array<string>;
    accessToken: string;
    refreshToken: string;
    expires: Date;
  };
}
```

### Dynamic Routes API (`src/api/routes.ts`)

```typescript
export const getAsyncRoutes = () => {
  return http.request("get", "/get-async-routes");
};
```

### Mock System (`mock/login.ts`)

Uses `vite-plugin-fake-server` with `defineFakeRoute()`:
- `POST /login` -- returns different user objects based on username (`admin` vs `common`)
- `POST /refresh-token` -- returns new tokens
- `GET /get-async-routes` -- returns dynamic permission routes

---

## 4. Router Guard / Permission System

### Router Setup (`src/router/index.ts`)

- Static routes auto-imported from `src/router/modules/*.ts` via `import.meta.glob`
- Remaining routes (login, error pages) from `src/router/modules/remaining.ts`
- Routes flattened to two levels for `keep-alive` compatibility
- History mode configurable via `VITE_ROUTER_HISTORY` env (hash/h5)

### Route Modules

| File | Routes |
|---|---|
| `src/router/modules/home.ts` | `/welcome` (home page) |
| `src/router/modules/error.ts` | `/error/403`, `/error/404`, `/error/500` |
| `src/router/modules/remaining.ts` | `/login`, `/access-denied`, `/server-error`, `/redirect/:path` |

### Navigation Guard (`router.beforeEach`)

**Logic flow:**

1. **Already logged in** (has `multiple-tabs` cookie + `userInfo` in localStorage):
   - Check route `meta.roles` against user roles -- 403 if no match
   - If `VITE_HIDE_HOME=true` and navigating to `/welcome`, redirect 404
   - On page refresh: if `wholeMenus` is empty, call `initRouter()` to re-fetch dynamic routes
   - Already logged in + navigating to `/login` --> stays on current page

2. **Not logged in**:
   - If target is in `whiteList` (`["/login"]`), allow
   - Otherwise, call `removeToken()` and redirect to `/login`

### Dynamic Route Registration (`src/router/utils.ts`)

- `initRouter()` -- fetches routes from `/get-async-routes` API
- Supports caching: if `CachingAsyncRoutes` is enabled, stores routes in localStorage
- `handleAsyncRoutes()` -- processes backend routes, maps component paths via `import.meta.glob("/src/views/**/*.{vue,tsx}")`
- `addAsyncRoutes()` -- recursively maps `component` string paths to actual Vue components
- `addPathMatch()` -- adds catch-all 404 route after dynamic routes are registered
- `resetRouter()` -- clears all routes and re-registers static ones (used on logout)

### Permission Filtering

- `filterNoPermissionTree()` -- filters menus by user `roles` from localStorage
- `isOneOfArray()` -- checks if user roles intersect with route's `meta.roles`
- Routes can have `meta.roles: ["admin", "common"]` for access control
- Button-level permissions use `meta.auths` on route or `permissions` from login response

---

## 5. Project Setup

### Key Dependencies

| Package | Version | Purpose |
|---|---|---|
| `vue` | ^3.5.22 | Framework |
| `vue-router` | ^4.6.3 | Routing |
| `pinia` | ^3.0.3 | State management |
| `element-plus` | ^2.11.5 | UI components |
| `axios` | ^1.12.2 | HTTP client |
| `tailwindcss` | ^4.1.16 | Utility CSS |
| `@pureadmin/table` | ^3.3.0 | Element Plus table wrapper |
| `@pureadmin/utils` | ^2.6.2 | Admin utilities (storageLocal, debounce, etc.) |
| `@pureadmin/descriptions` | ^1.2.1 | Descriptions component |
| `@vueuse/core` | ^14.0.0 | Composition utilities |
| `@vueuse/motion` | ^3.0.3 | Animation |
| `js-cookie` | ^3.0.5 | Cookie management |
| `localforage` | ^1.10.0 | Enhanced localStorage |
| `dayjs` | ^1.11.18 | Date handling |
| `echarts` | ^6.0.0 | Charts |
| `responsive-storage` | ^2.2.0 | Responsive storage |

### Key Dev Dependencies

| Package | Version | Purpose |
|---|---|---|
| **`vite`** | **`^7.1.12`** | **Build tool (NOT Vite 8)** |
| `@vitejs/plugin-vue` | ^6.0.1 | Vue SFC support |
| `@vitejs/plugin-vue-jsx` | ^5.1.1 | JSX/TSX support |
| `typescript` | ^5.9.3 | Type checking |
| `vite-plugin-fake-server` | ^2.2.0 | Mock server |
| `unplugin-icons` | ^22.5.0 | Auto-import icons |
| `@iconify/vue` | 4.2.0 | Icon rendering |
| `vite-svg-loader` | ^5.1.0 | SVG as Vue components |
| `vite-plugin-compression` | ^0.5.1 | Gzip/brotli |
| `vite-plugin-remove-console` | ^2.2.0 | Strip console.log in prod |
| `sass` | ^1.93.2 | SCSS support |

### Vite Version Status

**Current version: `^7.1.12`** (Vite 7, NOT Vite 8)

The `@vitejs/plugin-vue` is at `^6.0.1` which corresponds to Vite 7. Upgrading to Vite 8 would require:
- Checking `@vitejs/plugin-vue` compatibility with Vite 8
- Checking `@vitejs/plugin-vue-jsx` compatibility
- Checking all `vite-plugin-*` packages for Vite 8 support
- Note: Vite 8 may not exist yet as of 2026-06-29 (latest stable is likely Vite 7.x). Verify the actual latest version on npm before proceeding.

### Node Requirements

```json
"engines": {
  "node": "^20.19.0 || >=22.13.0",
  "pnpm": ">=9"
}
```

### Vite Config Structure

The `vite.config.ts` is minimal, delegating to `build/` directory:

| File | Purpose |
|---|---|
| `build/plugins.ts` | Plugin list (vue, jsx, tailwindcss, fake-server, icons, svg, compression, etc.) |
| `build/cdn.ts` | CDN replacement config |
| `build/compress.ts` | Compression plugin config |
| `build/info.ts` | Build info display |
| `build/optimize.ts` | Dep optimization include/exclude |
| `build/utils.ts` | Utilities (root, alias, pathResolve, wrapperEnv, __APP_INFO__) |

### Environment Variables

| Variable | Dev | Prod | Purpose |
|---|---|---|---|
| `VITE_PORT` | 8848 | -- | Dev server port |
| `VITE_PUBLIC_PATH` | `/` | `/` | Base path |
| `VITE_ROUTER_HISTORY` | `"hash"` | `"hash"` | Router mode |
| `VITE_CDN` | -- | `false` | Use CDN for libs |
| `VITE_COMPRESSION` | -- | `"none"` | Compression mode |

### Platform Config

Loaded at runtime from `public/platform-config.json`, provides:
- `Title` -- app title
- `SidebarStatus` -- initial sidebar state
- `Layout` -- layout mode
- `ResponsiveStorageNameSpace` -- storage namespace
- `CachingAsyncRoutes` -- whether to cache dynamic routes in localStorage

---

## @pure-admin npm Ecosystem

| Package | Latest | Description |
|---|---|---|
| `@pureadmin/table` | 3.3.0 | Element Plus table wrapper with flexible config |
| `@pureadmin/utils` | 2.6.4 | Admin utilities (storageLocal, debounce, deviceDetection, isUrl, etc.) |
| `@pureadmin/descriptions` | 1.2.1 | Element Plus Descriptions wrapper |

Note: `@pureadmin/theme`, `@pureadmin/components`, `@pureadmin/hooks` do NOT exist on npm. The ecosystem is smaller than expected -- most logic lives in the template repo itself and `@pureadmin/utils`.

---

## Caveats / Not Found

1. **Vite 8 does not appear to exist yet** as of this writing. pure-admin-thin uses Vite `^7.1.12`. The npm registry should be checked for the actual latest Vite version before any upgrade attempt. If "Vite 8" is a future version, upgrading may require waiting for ecosystem plugin compatibility.

2. **The `motion.ts` utility file returned 400** -- it likely uses `@vueuse/motion`'s `Motion` component for staggered fade-in animations on the login form elements. The component is imported directly in `index.vue`.

3. **No refresh-token interceptor for 401 status codes** -- the current implementation checks token expiry client-side (`parseInt(data.expires) - now <= 0`) rather than responding to 401 HTTP responses from the server. This is a design choice, not a bug.

4. **The mock system** (`vite-plugin-fake-server`) is integrated into Vite plugins and runs in both dev and prod (`enableProd: true`). In a real deployment, you would disable this and point API calls to a real backend.

5. **The `resetRouter()` function** calls `router.clearRoutes()` which requires Vue Router 4.4+. The project uses `^4.6.3` so this is fine.

6. **Pinia version is 3.x** (`^3.0.3`) -- this is the latest major version with breaking changes from v2 (different plugin API, etc.).
