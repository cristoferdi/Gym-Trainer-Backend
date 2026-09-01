# AGENTS.md — entrenaback

> Guía para agentes (y humanos) que trabajan en este repositorio. Lee esto antes de modificar código.

## 1. Visión del proyecto

`entrenaback` es el backend del **Gym Trainer Project**: API REST para entrenadores que gestionan alumnos, rutinas, ejercicios y asignaciones. Incluye sincronización offline (`/sync`), autenticación JWT, generación asistida con IA (Gemini) y seed de ejercicios.

- **Idioma del dominio:** entidades y mensajes de error en español (`Entrenador no encontrado`, `Alumno no encontrado`, etc.). Mantener español para mensajes de negocio.
- **Idioma del código:** inglés para identificadores, packages y comentarios técnicos.
- **Estado:** en desarrollo activo. Ver `backlog.json` (~40+ issues priorizadas) — es la fuente de verdad del roadmap.

## 2. Stack técnico

| Capa | Tecnología |
|------|------------|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.0.6 (parent) |
| Build | Maven 3.9 (`mvnw`/`mvnw.cmd` incluidos) |
| DB | PostgreSQL 15 — JPA/Hibernate (`ddl-auto: update`) |
| Auth | Spring Security + JWT (`jjwt 0.12.5/0.12.6`) |
| Tests | JUnit 5 + Testcontainers (PostgreSQL) + `spring-boot-starter-test` |
| Infra | Docker multi-stage (`Dockerfile`), `docker-compose.yml` |
| IA | Google Gemini (`gemini-flash-latest`) vía `RestTemplate` |

Puerto por defecto: `3001` con `server.servlet.context-path=/api` → URLs efectivas como `http://localhost:3001/api/auth/login`.

## 3. Estructura del proyecto

```
entrenaback/
├── pom.xml
├── docker-compose.yml
├── Dockerfile
├── backlog.json            # backlog priorizado (security/arch/database/api)
├── src/
│   ├── main/
│   │   ├── java/com/softech/entrenaback/
│   │   │   ├── EntrenabackApplication.java
│   │   │   ├── config/         # SecurityConfig, WebConfig, JacksonConfig, SeedRunner, GlobalExceptionHandler
│   │   │   ├── auth/           # AuthController/Service, JwtService, JwtAuthFilter, DTOs
│   │   │   ├── trainer/        # Trainer entity + repository
│   │   │   ├── student/        # CRUD alumnos
│   │   │   ├── routine/        # Routine, RoutineDay, ExerciseBlock, SubExerciseDetail + DTOs
│   │   │   ├── assigned/       # AssignedRoutine, AssignedRoutineDay, AssignedBlock
│   │   │   ├── exercise/       # Exercise (catálogo global)
│   │   │   ├── customexercise/ # CustomExercise (ejercicios del entrenador)
│   │   │   ├── sync/           # SyncController/Service + SyncPush/Pull DTOs
│   │   │   └── ai/             # AiController/Service, RateLimiterService
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application.properties
│   │       └── exercises_es_v3.json
│   └── test/
│       └── java/...            # Tests con Testcontainers
├── .mvn/
└── target/
```

### Módulos / dominios clave

- **auth/trainer:** registro, login, refresh, filtro JWT. Todos los servicios resuelven el trainer vía `trainerRepository.findByEmail(email)`.
- **student / routine / customexercise / assigned:** CRUD con ownership check (`trainer_id`). Patrón repetido — ver deuda en `backlog.json` sobre extraer `TrainerContext`.
- **exercise:** catálogo global de solo lectura (seed desde `exercises_es_v3.json` vía `SeedRunner`).
- **sync:** `POST /sync/push` (batch de `SyncOperation`) y `GET /sync/pull?lastSyncTimestamp=...`. Hoy sin transacción atómica ni límite de batch.
- **ai:** `POST /ai/generate` con `RateLimiterService`. Usa `RestTemplate` sin timeouts configurados.

## 4. Configuración y secretos

Variables de entorno (ver `src/main/resources/application.yml`):

| Variable | Descripción | Default (solo dev) |
|----------|-------------|---------------------|
| `PORT` | Puerto HTTP | `3001` |
| `JWT_SECRET` | Clave HMAC para JWT | *(sin default — requerido)* |
| `GEMINI_API_KEY` | API key de Gemini | *(requerido para IA)* |
| `CORS_FRONTEND_ORIGIN` | Origen permitido CORS | `http://localhost:3000` |
| `DATABASE_URL` | JDBC URL | `jdbc:postgresql://localhost:5433/entrenaback` |
| `DB_USERNAME` | Usuario DB | `admin` |
| `DB_PASSWORD` | Password DB | `123andi123` ⚠️ ver nota |
| `DOCKER_HOST` | Para Testcontainers | `tcp://127.0.0.1:2375` (perfil `docker-tcp`) |

> ⚠️ **Deuda de seguridad:** `DB_PASSWORD` tiene fallback hardcodeado y `docker-compose.yml` expone `123andi123` en texto plano. `backlog.json` prioriza migrar a `${POSTGRES_PASSWORD}` + `.env` + `.env.example`. **Nunca commitees secretos reales.** Si tocas config, documenta variables en `.env.example`.

`SecurityConfig.java:40-44` — endpoints públicos: `/auth/register`, `/auth/login`, `/auth/refresh`, `/assigned-routines/shared/**`. Todo lo demás requiere JWT. `anonymous.disable()` está activo (bug reportado en backlog).

## 5. Cómo ejecutar

### Prerrequisitos

- JDK 21, Maven (o usar `./mvnw`), Docker + Docker Compose, PostgreSQL 15 (o usar compose).

### Levantar DB

```bash
docker-compose up -d postgres
# o
docker compose up -d postgres
```

DB en `localhost:5433`, `admin / 123andi123 / entrenaback` (credenciales dev).

### Variables de entorno mínimas (dev)

Crea un `.env` (no commiteado) o exporta:

```bash
export JWT_SECRET="cambia-esto-por-un-secreto-largo-de-al-menos-256-bits"
export GEMINI_API_KEY="tu-key-si-usas-IA"
export DB_PASSWORD="123andi123"
export CORS_FRONTEND_ORIGIN="http://localhost:3000"
```

### Run en local

```bash
./mvnw spring-boot:run
# API en http://localhost:3001/api
```

### Docker (build multi-stage)

```bash
docker build -t entrenaback .
docker run -p 3000:3000 --env-file .env entrenaback
# Nota: Dockerfile expone 3000 pero application.yml usa 3001 (PORT). Ajusta PORT=3000 al correr en contenedor.
```

## 6. Build, tests y calidad

```bash
# Compilar (sin tests)
./mvnw clean package -DskipTests

# Tests (requiere Docker para Testcontainers; perfil docker-tcp activo por defecto)
./mvnw test

# Un solo test
./mvnw -Dtest=StudentControllerTest test

# Verificar que compila después de refactor
./mvnw clean verify
```

- Tests usan **Testcontainers** con PostgreSQL real — Docker debe estar corriendo.
- En Windows, el perfil `docker-tcp` setea `DOCKER_HOST=tcp://127.0.0.1:2375`. Si usas Docker Desktop con named pipe, puede requerir exponer el daemon en TCP o desactivar el perfil: `./mvnw test -P '!docker-tcp'`.
- No hay linter/formatter configurado — respeta el estilo existente (Spring idiomático, 4 espacios).

## 7. Convenciones para agentes

### Principios generales

- **Lee `backlog.json` antes de planificar** — prioriza `priority: critical` de seguridad si el usuario no especifica otra cosa.
- **No rompas el contrato de API** sin versionar. Hoy no hay `/api/v1`; cualquier cambio breaking debe discutirse.
- **Ownership:** cada recurso pertenece a un `trainer_id`. Verifica ownership antes de leer/escribir. Patrón actual: `findByEmail` + `IllegalArgumentException("Entrenador no encontrado")`.
- **Mensajes de error** en español, consistentes. No introduzcas i18n sin acordarlo.
- **No agregues dependencias** sin justificarlas. Preferir librerías ya presentes.

### Qué hacer

- Extraer lógica duplicada (ej. `RoutineService.create/update` comparten ~50 líneas; `findByEmail` repetido en 8+ servicios).
- Usar DTOs para respuestas — no exponer entidades JPA crudas (ver `SyncPullResponse` con `List<Student>`).
- Añadir validación Bean Validation (`@NotBlank`, `@Size`, `@Valid`) en DTOs, especialmente en `SyncPushRequest`/`SyncOperation`.
- Mantener transacciones coherentes (`@Transactional` en `SyncService.push()`).
- Preservar IDs en updates (no borrar y recrear `RoutineDay` si se puede hacer diff).

### Qué NO hacer

- No hardcodear secretos, orígenes CORS con `*`, ni passwords con fallback.
- No usar `new ObjectMapper()` — inyecta el bean de Spring (ver deuda en `SeedRunner`/`AiService` + `JacksonConfig` que pisa el auto-configurado).
- No silenciar errores (`catch (JsonProcessingException) { return "[]"; }` sin log).
- No usar `CascadeType.ALL` + `orphanRemoval` agresivo sin auditar impacto.
- No introducir `LocalDateTime.now()` — backlog pide migrar a `Instant`/`OffsetDateTime`.
- No crear `*.md` de documentación sin que el usuario lo pida explícitamente.

### Commits y PRs

- Mensajes concisos, en inglés o español consistente con el repo.
- No hagas `commit`/`push`/`PR` sin que el usuario lo pida explícitamente.
- Antes de commit, revisa `git status` y `git diff`; stagea solo lo intencionado, nunca secretos.

## 8. Endpoints principales

| Método | Ruta (prefijo `/api`) | Auth | Descripción |
|--------|------------------------|------|-------------|
| POST | `/auth/register` | no | Registro |
| POST | `/auth/login` | no | Login (retorna JWT) |
| POST | `/auth/refresh` | no* | Refresh (hoy exige token válido — ver backlog) |
| GET/POST/PUT/DELETE | `/students/**` | sí | CRUD alumnos |
| GET/POST/PUT/DELETE | `/routines/**` | sí | CRUD rutinas (Routine + Days + Blocks) |
| GET/POST/PUT/DELETE | `/custom-exercises/**` | sí | CRUD ejercicios custom |
| GET | `/exercises/**` | sí | Catálogo (con paginación, `success/metadata/data`) |
| GET/POST | `/assigned-routines/**` | sí | Asignar rutinas |
| GET | `/assigned-routines/shared/**` | no | Link compartido (token UUID) |
| POST | `/sync/push` | sí | Push de operaciones offline |
| GET | `/sync/pull` | sí | Pull incremental por `lastSyncTimestamp` |
| POST | `/ai/generate` | sí | Generación con Gemini (rate-limited) |

*Ver `backlog.json` — se planea separar access/refresh tokens y blacklist para logout.

## 9. Deuda técnica destacada (resumen de backlog.json)

- **Seguridad (critical):** CORS `*`, password hardcodeado, `DB_PASSWORD` con fallback, `UUID.randomUUID` para share tokens, sin rate limit en `/auth/login`, password validation débil (`@Size(min=6)` solo), sin blacklist JWT.
- **Arquitectura (high):** entidades expuestas en `SyncPullResponse`, `SyncPushRequest` sin validación, `SyncService.push()` sin `@Transactional`, `RoutineService.update()` recrea IDs, `new ObjectMapper()` en `SeedRunner`/`AiService`, cero logging SLF4J, `IllegalArgumentException` genérico, `SeedRunner` debe migrar a Flyway/Liquibase, `RestTemplate` sin timeouts, `JacksonConfig` pisa ObjectMapper auto-configurado.
- **DB (critical/high):** `ddl-auto: update` en prod, campos CSV en `Exercise`, formatos de lista inconsistentes (JSONB vs pipe vs JSON string), `AssignedBlock.blockData` como JSON blob, sin FKs, sin índices, sin constraints, `LocalDateTime` sin timezone.
- **API/Calidad:** sin paginación en listados, envelope de respuesta inconsistente, sin límite de batch en sync, etc. Ver `backlog.json` completo para lista exhaustiva.

## 10. Flujo de trabajo recomendado para un agente

1. Lee `backlog.json` y el issue asignado.
2. Explora los archivos citados (líneas indicadas en `body` del backlog).
3. Ejecuta `./mvnw test` (o el subset relevante) para baseline verde.
4. Implementa el fix mínimo, siguiendo convenciones de §7.
5. Añade/ajusta tests (Testcontainers) y verifica `./mvnw clean verify`.
6. No toques `application.yml`/`docker-compose.yml` para secretos sin crear `.env.example`.
7. Pide revisión si el cambio afecta contrato de API, schema DB o seguridad.

## 11. Referencias rápidas

- `pom.xml:6-9` — Spring Boot parent `4.0.6`, Java 21
- `src/main/resources/application.yml:1-28` — config central (port, JWT, datasource, CORS)
- `src/main/java/com/softech/entrenaback/config/SecurityConfig.java` — CORS, filter chain, endpoints públicos
- `src/main/java/com/softech/entrenaback/config/GlobalExceptionHandler.java` — manejo central de errores
- `docker-compose.yml:1-9` — Postgres dev
- `Dockerfile:1-18` — build multi-stage Maven → JRE

---

*Última actualización: 2026-08-31. Mantén este archivo sincronizado cuando cambien stack, config o convenciones.*
