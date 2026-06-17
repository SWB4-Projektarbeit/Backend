# TimeSy — Session Changes Documentation

This document describes every code change made to the TimeSy project during this development session.
Only final, currently-active changes are listed. Intermediate states that were later revised are omitted.

---

## Overview of what was done

1. Connected the backend to the EPD Management Console (e-ink display server) at `192.168.144.200:9000`
2. Fixed a backend startup crash caused by a debug print statement in `HEOnlineService`
3. Replaced session-based OAuth2 login on the backend with JWT Bearer token validation, so the Angular frontend can authenticate API calls
4. Removed the redundant per-endpoint user null-checks in the controller that blocked all JWT-authenticated requests
5. Replaced `keycloak-angular` (incompatible with Keycloak 26) with a thin direct wrapper around `keycloak-js 26` in the frontend
6. Created a Keycloak `frontend` public client and a test user in the `swt-projekt` realm (manual Keycloak setup)

---

## Backend changes

### `Backend/pom.xml`

**Added dependency:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

**Why:** The backend now validates JWT Bearer tokens sent by the Angular frontend. This starter provides Spring Security's JWT resource server support (JWK Set download, signature verification, claims validation).

---

### `Backend/src/main/resources/application.properties`

**Added/changed:**
```properties
# Was: displayserver.url=192.168.144.200  (missing scheme and port — broke RestClient)
displayserver.url=http://192.168.144.200:9000
displayserver.username=admin
displayserver.password=admin

# New: allows the JWT resource server to validate tokens issued by Keycloak
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/swt-projekt
```

**Why:**
- `displayserver.url` was a bare IP address, which `RestClient` cannot use. Added `http://` scheme and `:9000` port as documented in the EPD Management Console API.
- `displayserver.username` and `displayserver.password` are the credentials required for the EPD server's session authentication endpoint (`POST /api/users/auth`).
- `jwt.issuer-uri` tells Spring Security where to download the Keycloak public keys (JWKS) and what issuer claim to expect in incoming Bearer tokens.

---

### `Backend/src/main/java/de/hsesslingen/timesy/backend/config/SecurityConfig.java`

**Added `http://localhost:4200` to CORS allowed origins and `PATCH` to allowed methods:**
```java
config.setAllowedOrigins(List.of(
    "http://localhost:4200",   // Angular dev server — added
    this.keycloakUrl
));
config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")); // PATCH added
```

**Added JWT resource server to the security filter chain:**
```java
http
    .authorizeHttpRequests(auth ->
        auth.requestMatchers("/actuator/**").permitAll()
            .anyRequest().authenticated())
    .oauth2Login(Customizer.withDefaults())
    .oauth2Client(Customizer.withDefaults())
    .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))  // added
    .csrf(AbstractHttpConfigurer::disable)
    .cors(cors -> cors.configurationSource(corsConfigurationSource()));
```

**Why:**
- The Angular dev server runs on `localhost:4200` and makes cross-origin requests to the backend. Without this origin in the CORS allowlist the browser blocks all API responses.
- `PATCH` is used by the frontend to update room template assignments.
- `oauth2ResourceServer` with JWT support allows the backend to accept and validate `Authorization: Bearer <token>` headers from the Angular frontend. Without this, the backend only understood session cookies from its own `oauth2Login` flow and rejected all API calls from the SPA with a redirect to Keycloak.

---

### `Backend/src/main/java/de/hsesslingen/timesy/backend/controller/Controller.java`

**Removed `@AuthenticationPrincipal OidcUser user` and all `if (user == null)` checks from every endpoint.**

Before (example):
```java
@GetMapping("/rooms")
public ResponseEntity<?> getAllRooms(
        @AuthenticationPrincipal final OidcUser user,   // removed
        @RequestParam(required = false) String building,
        ...) {
    if (user == null) {                                 // removed
        return new ResponseEntity<>("Not a valid user", HttpStatus.UNAUTHORIZED);
    }
    return this.frontendService.getAllRooms(...);
}
```

After:
```java
@GetMapping("/rooms")
public ResponseEntity<?> getAllRooms(
        @RequestParam(required = false) String building,
        ...) {
    return this.frontendService.getAllRooms(...);
}
```

**Why:** `@AuthenticationPrincipal OidcUser` is only populated when authentication goes through the session-based `oauth2Login` flow. When the frontend authenticates with a JWT Bearer token, Spring Security sets the principal to a `Jwt` object — so `OidcUser` is always `null` and every endpoint returned 401 immediately. The null check was also redundant: Spring Security's `anyRequest().authenticated()` rule already rejects unauthenticated requests before they reach the controller.

---

### `Backend/src/main/java/de/hsesslingen/timesy/backend/service/HEOnlineService.java`

**Removed a debug `System.out.println` call in `getAppointments()`.**

Before (around line 96):
```java
final RestClient.ResponseSpec response = this.restClient.get()
        .uri(this.heOnlineUrl + "/" + this.appointmentsEndpoint)
        ...
        .retrieve();

System.out.println(response.toEntity(String.class));   // removed — caused startup crash
try {
    responseEntity = response.toEntity(APPOINTMENT_TYPE);
} catch (final Exception e) { ... }
```

After:
```java
final RestClient.ResponseSpec response = this.restClient.get()
        .uri(this.heOnlineUrl + "/" + this.appointmentsEndpoint)
        ...
        .retrieve();

try {
    responseEntity = response.toEntity(APPOINTMENT_TYPE);
} catch (final Exception e) { ... }
```

**Why:** In Spring's `RestClient`, calling `.retrieve()` is lazy — it does not send the HTTP request. The request fires when you call `.toEntity()` or similar. The debug `System.out.println` was calling `.toEntity(String.class)` outside the `try/catch`, which immediately sent the HTTP request. If the HE-Online mock was not running, this threw a `ConnectException` that propagated uncaught through the `@PostConstruct` startup chain in `FrontendService` and crashed the entire Spring application context before it could finish starting.

---

### `Backend/src/main/java/de/hsesslingen/timesy/backend/epd/EpdAuthService.java` *(new file)*

**New package: `de.hsesslingen.timesy.backend.epd`**

This service manages the session with the EPD Management Console server.

```java
@Slf4j
@Service
public class EpdAuthService {
    // Authenticates with POST /api/users/auth, extracts the Set-Cookie header,
    // caches the session cookie, and re-authenticates on demand or after a 401.

    public synchronized String getSessionCookie()    // returns cached cookie, authenticates if needed
    public synchronized void invalidateSession()     // clears cache so next call re-authenticates
}
```

**Why:** The EPD Management Console API (v2.7.2) uses session cookie authentication. Every API call must include a valid session cookie obtained from `POST /api/users/auth` with a JSON body `{"data":{"name":"...","password":"..."}}`. The original `DisplayService` had no authentication at all, so all EPD API requests were rejected with 401. This service handles the session lifecycle: lazy authentication, caching, and re-authentication when the session expires.

---

### `Backend/src/main/java/de/hsesslingen/timesy/backend/service/DisplayService.java`

**Constructor now injects `EpdAuthService` instead of building a standalone `RestClient`:**
```java
public DisplayService(
        @Value("${displayserver.url}") final String displayServerUrl,
        final EpdAuthService epdAuthService) {   // added
    this.restClient = RestClient.builder().baseUrl(displayServerUrl).build();
    this.epdAuthService = epdAuthService;
}
```

**All EPD API requests now include the session cookie:**
```java
.header(HttpHeaders.COOKIE, cookie)
```

**401 responses now invalidate the cached session so the next call re-authenticates:**
```java
} catch (final Exception e) {
    if (e.getMessage() != null && e.getMessage().contains("401")) {
        this.epdAuthService.invalidateSession();
    }
    ...
}
```

**`sendImage()` changed from a JSON body to multipart/form-data:**

The EPD `mem_combo` endpoint requires `multipart/form-data` with two parts:
- `dto` — the LocationDTO as a JSON string
- `images[img1]` — the PNG image bytes

```java
final LinkedMultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();

// Part 1: LocationDTO JSON
final HttpHeaders dtoHeaders = new HttpHeaders();
dtoHeaders.setContentType(MediaType.APPLICATION_JSON);
formData.add("dto", new HttpEntity<>(locationDTO, dtoHeaders));

// Part 2: PNG image
final ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
    @Override public String getFilename() { return "schedule.png"; }
};
final HttpHeaders imageHeaders = new HttpHeaders();
imageHeaders.setContentType(MediaType.IMAGE_PNG);
formData.add("images[img1]", new HttpEntity<>(imageResource, imageHeaders));

responseEntity = this.restClient.post()
        .uri(String.format(IMAGE_ENDPOINT, display.getDisplayUid(), slot))
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .header(HttpHeaders.COOKIE, cookie)
        .body(formData)
        .retrieve()
        .toEntity(String.class);
```

**Why:** The original code posted a custom `ImagePostBody` JSON record to the EPD endpoint, which is incorrect. According to the EPD Management Console API v2.7.2 documentation, the `PUT /api/location/{id}/mem_combo/{slot}` endpoint expects `multipart/form-data` with a `dto` field (JSON) and an `images[img1]` field (PNG binary). The original code would have been rejected by the EPD server regardless of authentication.

---

## Frontend changes

### `Frontend/timesy-frontend/package.json`

**`keycloak-js` upgraded from `^21.1.2` to `^26.2.4`**

**Why:** The Keycloak server running in this project is version **26.0.0**. The `keycloak-js` adapter must match the server major version. Using `keycloak-js` 21 with a Keycloak 26 server caused `keycloak.init()` to silently reject with `undefined` on every call, preventing Angular from bootstrapping (black screen). No version of `keycloak-angular` that supports `keycloak-js` 26 is compatible with Angular 17, so `keycloak-angular` was removed entirely and `keycloak-js` is now used directly.

---

### `Frontend/timesy-frontend/tsconfig.json`

**Changed `moduleResolution` from `"node"` to `"bundler"`:**
```json
"moduleResolution": "bundler"
```

**Why:** `keycloak-js` 26 is an ES module package. It declares its entry point via the `exports` field in `package.json`, not via the legacy `main` field. TypeScript's `"moduleResolution": "node"` only reads the `main` field (CommonJS resolution) and therefore cannot find the `keycloak-js` module at all, causing a `TS2307: Cannot find module 'keycloak-js'` compilation error. `"moduleResolution": "bundler"` supports the `exports` field and is the correct setting for Angular 17 projects that use a bundler (webpack/esbuild).

---

### `Frontend/timesy-frontend/src/app/app.config.ts` *(rewritten)*

**Removed:** `keycloak-angular`, `KeycloakAngularModule`, `KeycloakBearerInterceptor`, `KeycloakService`

**Added:** `AuthService`, `AuthInterceptor`

```typescript
import { APP_INITIALIZER, ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { routes } from './app.routes';
import { AuthService } from './core/services/auth.service';
import { AuthInterceptor } from './core/interceptors/auth.interceptor';

function initializeAuth(auth: AuthService) {
  return () => auth.init();
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeAuth,
      multi: true,
      deps: [AuthService],
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
  ],
};
```

**Why:** `keycloak-angular` 15.x depends on `keycloak-js` 21.x and is not compatible with `keycloak-js` 26. The `keycloak-angular` versions that do support `keycloak-js` 26 (v16+) require Angular 18+, but this project uses Angular 17. The solution is to use `keycloak-js` 26 directly and implement the small amount of Angular integration (init, Bearer interceptor, route guard) without the library.

---

### `Frontend/timesy-frontend/src/app/app.routes.ts`

**Added `authGuard` to all protected routes:**
```typescript
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'rooms', pathMatch: 'full' },
  {
    path: 'rooms',
    canActivate: [authGuard],          // added
    loadComponent: () => import('./pages/room-selection/room-selection.component')...
  },
  {
    path: 'rooms/:roomUid',
    canActivate: [authGuard],          // added
    loadComponent: () => import('./pages/template-selection/template-selection.component')...
  },
  { path: '**', redirectTo: 'rooms' },
];
```

**Why:** With `keycloak-angular` removed there is no longer an `onLoad: 'login-required'` in the `APP_INITIALIZER` that automatically redirects unauthenticated users to Keycloak. The route guard takes over this responsibility: it checks `AuthService.isAuthenticated()` after Keycloak initializes and calls `keycloak.login()` if the user is not logged in.

---

### `Frontend/timesy-frontend/src/app/core/services/auth.service.ts` *(new file)*

Thin Angular service wrapping `keycloak-js` 26 directly:

```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
  private kc: Keycloak = new Keycloak({
    url: 'http://localhost:8080',
    realm: 'swt-projekt',
    clientId: 'frontend',
  });

  async init(): Promise<void>               // called by APP_INITIALIZER; initializes Keycloak without redirecting
  isAuthenticated(): boolean                // returns kc.authenticated
  login(redirectUri?: string): Promise<void>// redirects browser to Keycloak login
  logout(): Promise<void>                   // redirects browser to Keycloak logout
  getToken(): string | undefined            // current access token (may be expired)
  async getValidToken(): Promise<string>    // refreshes if < 30 s remain, re-logs if refresh fails
}
```

**Why:** Provides a single place to manage the Keycloak session lifecycle. `init()` uses `checkLoginIframe: false` to avoid the cross-origin iframe session check that causes initialization failures in modern browsers with strict cookie policies.

---

### `Frontend/timesy-frontend/src/app/core/interceptors/auth.interceptor.ts` *(new file)*

HTTP interceptor that attaches the Bearer token to every outgoing request:

```typescript
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(req, next): Observable<HttpEvent<unknown>> {
    if (!this.auth.isAuthenticated()) return next.handle(req);
    return from(this.auth.getValidToken()).pipe(
      switchMap(token => {
        const authReq = token
          ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
          : req;
        return next.handle(authReq);
      }),
    );
  }
}
```

**Why:** Replaces `KeycloakBearerInterceptor` from `keycloak-angular`. Every API call to the backend must include a valid JWT in the `Authorization: Bearer` header so the backend's JWT resource server can authenticate the request.

---

### `Frontend/timesy-frontend/src/app/core/guards/auth.guard.ts` *(new file)*

Functional Angular route guard:

```typescript
export const authGuard: CanActivateFn = async (_route, state) => {
  const auth = inject(AuthService);
  if (auth.isAuthenticated()) return true;
  await auth.login(window.location.origin + state.url);
  return false;
};
```

**Why:** Replaces the class-based `KeycloakAuthGuard` from `keycloak-angular`. If the user is not authenticated when they navigate to a protected route, this guard calls `auth.login()` with a `redirectUri` pointing back to the intended route, so after Keycloak login the user lands on the page they were trying to access.

---

### `Frontend/timesy-frontend/src/app/components/navbar/navbar.component.ts`

**Replaced `KeycloakService` with `AuthService`:**

```typescript
// Before
import { KeycloakService } from 'keycloak-angular';
private keycloak: KeycloakService
logout() { this.keycloak.logout(window.location.origin); }

// After
import { AuthService } from '../../core/services/auth.service';
private auth: AuthService
logout() { this.auth.logout(); }
```

**Why:** `keycloak-angular` and `KeycloakService` were removed from the project. The logout button in the navbar must use the replacement `AuthService`.

---

## Keycloak manual setup (one-time, not in source code)

### Created `frontend` public client in realm `swt-projekt`

| Setting | Value |
|---|---|
| Client type | OpenID Connect |
| Client ID | `frontend` |
| Client authentication | OFF (public client — no secret) |
| Standard flow | ON |
| Valid redirect URIs | `http://localhost:4200/*` |
| Web origins | `http://localhost:4200` |

**Why:** The Angular frontend authenticates users directly with Keycloak using `keycloak-js`. This requires a dedicated Keycloak client for the SPA. A public client (no secret) is correct for browser-based applications because secrets cannot be kept confidential in client-side code. The existing `swt-project-bff` client is a confidential backend-only client used by the Spring Boot application for machine-to-machine authentication with HE-Online.

### Created a test user in realm `swt-projekt`

A user account was created in the `swt-projekt` Keycloak realm with **Temporary** password set to **OFF** so the user can log in directly without being forced to change the password on first login.

---

## File summary

| File | Status | What changed |
|---|---|---|
| `Backend/pom.xml` | Modified | Added `spring-boot-starter-oauth2-resource-server` |
| `Backend/src/main/resources/application.properties` | Modified | Fixed EPD URL/credentials, added JWT issuer URI |
| `Backend/src/main/java/.../config/SecurityConfig.java` | Modified | Added CORS origin + PATCH method, added JWT resource server |
| `Backend/src/main/java/.../controller/Controller.java` | Modified | Removed `OidcUser` parameter and null-checks from all endpoints |
| `Backend/src/main/java/.../service/HEOnlineService.java` | Modified | Removed crashing debug `System.out.println` |
| `Backend/src/main/java/.../service/DisplayService.java` | Modified | Added EPD auth, fixed image upload to multipart/form-data |
| `Backend/src/main/java/.../epd/EpdAuthService.java` | **New file** | EPD session authentication service |
| `Frontend/timesy-frontend/package.json` | Modified | `keycloak-js` upgraded from 21 to 26 |
| `Frontend/timesy-frontend/tsconfig.json` | Modified | `moduleResolution`: `node` → `bundler` |
| `Frontend/timesy-frontend/src/app/app.config.ts` | Modified | Removed `keycloak-angular`, wired up `AuthService` + `AuthInterceptor` |
| `Frontend/timesy-frontend/src/app/app.routes.ts` | Modified | Added `authGuard` to all protected routes |
| `Frontend/timesy-frontend/src/app/components/navbar/navbar.component.ts` | Modified | Replaced `KeycloakService` with `AuthService` for logout |
| `Frontend/timesy-frontend/src/app/core/services/auth.service.ts` | **New file** | keycloak-js 26 wrapper service |
| `Frontend/timesy-frontend/src/app/core/interceptors/auth.interceptor.ts` | **New file** | HTTP interceptor adding Bearer token |
| `Frontend/timesy-frontend/src/app/core/guards/auth.guard.ts` | **New file** | Route guard redirecting unauthenticated users to Keycloak |
