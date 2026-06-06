# Spec: Backend Core — My CMA

## Módulos

El backend se organiza como un **Maven Multi-Module** con 11 módulos:

| Módulo | Artefacto | Responsabilidad |
|--------|-----------|----------------|
| **my-cma-core** | `my-cma-core` | Dominio puro: entidades, value objects, puertos (interfaces). Sin dependencias de frameworks. |
| **my-cma-infrastructure-persistence** | `my-cma-infrastructure-persistence` | Adaptador JPA + SQLite. Implementa repositorios del core. |
| **my-cma-infrastructure-social-facebook** | `my-cma-infrastructure-social-facebook` | Adaptador Facebook Graph API v21. Autenticación OAuth 2.0, publicación, lectura de métricas. |
| **my-cma-infrastructure-social-instagram** | `my-cma-infrastructure-social-instagram` | Adaptador Instagram Graph API. Publicación en feeds y stories, obtención de analytics. |
| **my-cma-infrastructure-social-linkedin** | `my-cma-infrastructure-social-linkedin` | Adaptador LinkedIn Marketing API. Publicación en company pages. |
| **my-cma-infrastructure-ai-openai** | `my-cma-infrastructure-ai-openai` | Adaptador OpenAI (Azure SDK). Generación de copies con GPT-4o e imágenes con DALL-E 3. |
| **my-cma-infrastructure-ai-qwen** | `my-cma-infrastructure-ai-qwen` | Adaptador alternativo Qwen (Alibaba Cloud). Provider de IA pluggable. |
| **my-cma-application-content** | `my-cma-application-content` | Casos de uso de contenido: generar copy, generar imagen, editar borrador. |
| **my-cma-application-publishing** | `my-cma-application-publishing` | Casos de uso de publicación: publicar ahora, programar, reprogramar, cancelar. |
| **my-cma-application-analytics** | `my-cma-application-analytics` | Casos de uso de analytics: obtener métricas de post, resumen de cuenta, tendencias. |
| **my-cma-bootstrap** | `my-cma-bootstrap` | Entry point Spring Boot. Contiene REST controllers, configuración de seguridad, perfiles. Depende de todos los módulos. |

### Mapa de dependencias entre módulos

```
bootstrap ──┬── application-content ──┬── core
             │                        └── infrastructure-ai-openai
             │                        └── infrastructure-ai-qwen
             ├── application-publishing ─┬── core
             │                           └── infrastructure-social-facebook
             │                           └── infrastructure-social-instagram
             │                           └── infrastructure-social-linkedin
             │                           └── infrastructure-persistence
             ├── application-analytics ──┬── core
             │                           └── infrastructure-persistence
             └── infrastructure-persistence ── core
```

Todos los módulos application e infrastructure dependen únicamente de `core`. El dominio nunca depende de infraestructura.

---

## Arquitectura

### Hexagonal Architecture (Puertos y Adaptadores)

```
                            ┌──────────────────┐
                            │    Frontend       │
                            │ (Electron+React)  │
                            └────────┬─────────┘
                                     │ HTTP (localhost:8080)
                            ┌────────▼─────────┐
                            │    Bootstrap      │
                            │ (REST Controllers)│
                            └────────┬─────────┘
                                     │
          ┌──────────────────────────┼──────────────────────────┐
          │           Application    │    (Casos de uso)        │
          │  ┌──────────┐ ┌──────────┐ ┌──────────┐            │
          │  │ Content  │ │Publishing│ │Analytics │            │
          │  │  UseCase │ │  UseCase │ │  UseCase │            │
          │  └────┬─────┘ └────┬─────┘ └────┬─────┘            │
          │       │            │             │                  │
          │       └────────────┼─────────────┘                  │
          │                    │ (usa puertos del core)         │
          ├────────────────────┼────────────────────────────────┤
          │         Core       │  (Dominio puro)               │
          │  ┌─────────────────▼──────────────────────────┐    │
          │  │  Puerto (interfaz)                         │    │
          │  │  SocialMediaPort                           │    │
          │  │  AIGenerationPort                          │    │
          │  │  PostRepository (extends Port)              │    │
          │  │  AccountRepository (extends Port)           │    │
          │  └─────────────────▲──────────────────────────┘    │
          │                    │                                │
          ├────────────────────┼────────────────────────────────┤
          │  Infrastructure    │  (Adaptadores)                 │
          │  ┌─────────────────┴──────────────────────────┐    │
          │  │  FacebookAdapter ── implements SocialMedia  │    │
          │  │  InstagramAdapter ── implements SocialMedia  │    │
          │  │  LinkedInAdapter ── implements SocialMedia   │    │
          │  │  OpenAIAdapter ── implements AIGeneration    │    │
          │  │  QwenAdapter ── implements AIGeneration      │    │
          │  │  JpaPostRepository ── implements PostRepo    │    │
          │  └─────────────────────────────────────────────┘    │
          └─────────────────────────────────────────────────────┘
```

### Modular Monolith vs Microservicios — Decisión

**Decisión: Modular Monolith.**

Razones:

1. **Aplicación de escritorio local.** My CMA corre en la máquina del usuario, no hay necesidad de desplegar múltiples servicios.
2. **Una sola base de datos SQLite.** Los datos son locales y no requieren escalamiento horizontal.
3. **Simplicidad operativa.** Un solo JAR, un solo proceso. Sin service discovery, sin colas de mensajes, sin orquestación.
4. **Futura evolución.** Si en el futuro se requiere un backend multitenant SaaS, la separación en módulos permite extraer servicios independientes sin reescribir: cada módulo application puede convertirse en un microservicio extrayendo su interfaz REST y adaptadores.

### Flujo de datos

```
Request HTTP
    │
    ▼
Controller (bootstrap)         ← Valida, deserializa, construye request DTO
    │
    ▼
UseCase (application)          ← Orquesta lógica de negocio, llama a puertos
    │
    ▼
Port (core/interfaz)           ← Define contrato, desacopla dominio de infraestructura
    │
    ▼
Adapter (infrastructure)       ← Implementa llamada real: HTTP externo, BD, IA
    │
    ▼
Servicio externo / BD          ← Facebook API, OpenAI, SQLite, etc.
```

Las respuestas siguen el camino inverso: Adapter → Port → UseCase → Controller → HTTP Response.

---

## API REST

### `GET /api/health`

| Campo | Valor |
|-------|-------|
| Método | `GET` |
| Path | `/api/health` |
| Autenticación | No |
| Descripción | Verifica que el backend esté operativo. |

**Response `200 OK`:**

```json
{
  "status": "UP",
  "timestamp": "2026-06-06T12:00:00Z",
  "version": "1.0.0-beta",
  "dbConnected": true,
  "uptime": 3600
}
```

---

### `POST /api/auth/facebook/authorize`

| Campo | Valor |
|-------|-------|
| Método | `POST` |
| Path | `/api/auth/facebook/authorize` |
| Autenticación | No (inicia flujo OAuth) |
| Descripción | Inicia el flujo OAuth 2.0 con Facebook. Recibe el código de autorización y lo intercambia por un access token. |

**Request Body:**

```json
{
  "code": "AQB7...abc123",
  "redirectUri": "http://localhost:5173/auth/callback"
}
```

**Response `200 OK`:**

```json
{
  "accountId": 1,
  "pageName": "Mi Página",
  "pageId": "123456789",
  "connectedAt": "2026-06-06T12:00:00Z"
}
```

**Response `401 Unauthorized`:**

```json
{
  "error": "facebook_auth_failed",
  "message": "El código de autorización es inválido o expiró."
}
```

---

### `GET /api/accounts`

| Campo | Valor |
|-------|-------|
| Método | `GET` |
| Path | `/api/accounts` |
| Autenticación | JWT requerido |
| Descripción | Lista todas las cuentas de redes sociales conectadas. |

**Response `200 OK`:**

```json
[
  {
    "id": 1,
    "provider": "facebook",
    "providerUserId": "123456789",
    "pageName": "Mi Página",
    "pageId": "123456789",
    "connectedAt": "2026-06-06T12:00:00Z",
    "status": "active"
  }
]
```

---

### `POST /api/posts`

| Campo | Valor |
|-------|-------|
| Método | `POST` |
| Path | `/api/posts` |
| Autenticación | JWT requerido |
| Descripción | Crea un nuevo post sin publicar (borrador o programado). |

**Request Body:**

```json
{
  "contentText": "¡Hoy lanzamos nuestro nuevo producto!",
  "mediaPaths": ["/images/producto.png"],
  "hashtags": ["#lanzamiento", "#nuevoproducto"],
  "provider": "facebook",
  "scheduledAt": null,
  "accountId": 1
}
```

**Response `201 Created`:**

```json
{
  "id": 1,
  "status": "draft",
  "createdAt": "2026-06-06T12:00:00Z"
}
```

---

### `POST /api/posts/{id}/publish`

| Campo | Valor |
|-------|-------|
| Método | `POST` |
| Path | `/api/posts/{id}/publish` |
| Autenticación | JWT requerido |
| Descripción | Publica un post existente en la red social configurada. Si el post tiene `scheduledAt`, respeta la programación. Requiere confirmación manual previa. |

**Request Body:**

```json
{
  "confirm": true
}
```

**Response `200 OK`:**

```json
{
  "id": 1,
  "status": "published",
  "providerPostId": "987654321",
  "publishedAt": "2026-06-06T12:00:00Z"
}
```

**Response `409 Conflict`:**

```json
{
  "error": "confirmation_required",
  "message": "Debe confirmar la publicación manualmente."
}
```

---

### `GET /api/posts`

| Campo | Valor |
|-------|-------|
| Método | `GET` |
| Path | `/api/posts` |
| Autenticación | JWT requerido |
| Descripción | Lista todos los posts. Soporta filtros por estado, proveedor y rango de fechas. |

**Query Parameters:**

| Parámetro | Tipo | Opcional | Descripción |
|-----------|------|----------|-------------|
| `status` | string | Sí | `draft`, `scheduled`, `published`, `failed` |
| `provider` | string | Sí | `facebook`, `instagram`, `linkedin` |
| `from` | string (ISO) | Sí | Fecha inicio para filtrar |
| `to` | string (ISO) | Sí | Fecha fin para filtrar |

**Response `200 OK`:**

```json
[
  {
    "id": 1,
    "contentText": "¡Hoy lanzamos nuestro nuevo producto!",
    "mediaPaths": ["/images/producto.png"],
    "hashtags": ["#lanzamiento", "#nuevoproducto"],
    "provider": "facebook",
    "status": "published",
    "scheduledAt": null,
    "publishedAt": "2026-06-06T12:00:00Z",
    "providerPostId": "987654321"
  }
]
```

---

### `POST /api/content/generate-copy`

| Campo | Valor |
|-------|-------|
| Método | `POST` |
| Path | `/api/content/generate-copy` |
| Autenticación | JWT requerido |
| Descripción | Genera un copy publicitario usando IA. Recibe instrucciones y devuelve texto generado. |

**Request Body:**

```json
{
  "prompt": "Escribí un post de lanzamiento para un producto SaaS de gestión de redes sociales",
  "tone": "profesional",
  "maxLength": 280,
  "language": "es"
}
```

**Response `200 OK`:**

```json
{
  "generatedText": "Presentamos My CMA: la herramienta que todo community manager necesita. Gestioná todas tus redes desde un solo lugar, generá contenido con IA y optimizá tu estrategia. 🚀 #MyCMA #CommunityManager",
  "modelUsed": "gpt-4o",
  "tokensUsed": 142,
  "generationId": 1
}
```

---

### `POST /api/content/generate-image`

| Campo | Valor |
|-------|-------|
| Método | `POST` |
| Path | `/api/content/generate-image` |
| Autenticación | JWT requerido |
| Descripción | Genera una imagen usando DALL-E 3 a partir de una descripción. |

**Request Body:**

```json
{
  "prompt": "Un community manager feliz frente a un dashboard colorido",
  "style": "vivid",
  "size": "1024x1024"
}
```

**Response `200 OK`:**

```json
{
  "imagePath": "/generated/cm-dashboard.png",
  "modelUsed": "dall-e-3",
  "generationId": 2
}
```

---

### `GET /api/analytics/{postId}`

| Campo | Valor |
|-------|-------|
| Método | `GET` |
| Path | `/api/analytics/{postId}` |
| Autenticación | JWT requerido |
| Descripción | Obtiene métricas de un post publicado. |

**Response `200 OK`:**

```json
{
  "postId": 1,
  "provider": "facebook",
  "impressions": 1520,
  "reach": 1340,
  "likes": 89,
  "comments": 12,
  "shares": 34,
  "clicks": 210,
  "engagementRate": 0.089,
  "retrievedAt": "2026-06-06T12:00:00Z"
}
```

**Response `404 Not Found`:**

```json
{
  "error": "post_not_found",
  "message": "El post solicitado no existe o no tiene métricas disponibles."
}
```

---

## Modelo de datos (SQLite)

### `social_accounts`

Almacena las cuentas de redes sociales conectadas.

| Columna | Tipo | Nulable | Descripción |
|---------|------|---------|-------------|
| `id` | `BIGINT PK AUTO_INCREMENT` | No | Identificador único |
| `provider` | `VARCHAR(50)` | No | Proveedor: `facebook`, `instagram`, `linkedin` |
| `access_token` | `VARCHAR(512)` | No | Token de acceso OAuth |
| `refresh_token` | `VARCHAR(512)` | Sí | Token de refresco OAuth |
| `provider_user_id` | `VARCHAR(255)` | Sí | ID del usuario en el proveedor |
| `page_id` | `VARCHAR(255)` | Sí | ID de la página/empresa |
| `page_name` | `VARCHAR(255)` | Sí | Nombre de la página |
| `expires_at` | `TIMESTAMP` | Sí | Fecha de expiración del token |
| `connected_at` | `TIMESTAMP` | No | Fecha de conexión |

**DDL:**

```sql
CREATE TABLE social_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider VARCHAR(50) NOT NULL,
    access_token VARCHAR(512) NOT NULL,
    refresh_token VARCHAR(512),
    provider_user_id VARCHAR(255),
    page_id VARCHAR(255),
    page_name VARCHAR(255),
    expires_at TIMESTAMP,
    connected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### `posts`

Almacena los posts creados, su estado y resultados de publicación.

| Columna | Tipo | Nulable | Descripción |
|---------|------|---------|-------------|
| `id` | `BIGINT PK AUTO_INCREMENT` | No | Identificador único |
| `content_text` | `TEXT` | No | Contenido textual del post |
| `media_paths_json` | `TEXT` | Sí | JSON array de rutas de archivos multimedia |
| `hashtags_json` | `TEXT` | Sí | JSON array de hashtags |
| `provider` | `VARCHAR(50)` | No | Proveedor objetivo: `facebook`, `instagram`, `linkedin` |
| `provider_post_id` | `VARCHAR(255)` | Sí | ID del post en el proveedor (después de publicado) |
| `scheduled_at` | `TIMESTAMP` | Sí | Fecha programada para publicación |
| `published_at` | `TIMESTAMP` | Sí | Fecha real de publicación |
| `status` | `VARCHAR(20)` | No | Estado: `draft`, `scheduled`, `publishing`, `published`, `failed` |
| `error_message` | `TEXT` | Sí | Mensaje de error si la publicación falló |

**DDL:**

```sql
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content_text TEXT NOT NULL,
    media_paths_json TEXT,
    hashtags_json TEXT,
    provider VARCHAR(50) NOT NULL,
    provider_post_id VARCHAR(255),
    scheduled_at TIMESTAMP,
    published_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    error_message TEXT
);
```

### `ai_generations`

Almacena el historial de generaciones de IA.

| Columna | Tipo | Nulable | Descripción |
|---------|------|---------|-------------|
| `id` | `BIGINT PK AUTO_INCREMENT` | No | Identificador único |
| `type` | `VARCHAR(20)` | No | Tipo: `copy`, `image` |
| `original_prompt` | `TEXT` | No | Prompt enviado al modelo |
| `generated_text` | `TEXT` | Sí | Texto generado (para copies) |
| `generated_image_path` | `VARCHAR(512)` | Sí | Ruta de imagen generada (para imágenes) |
| `model_used` | `VARCHAR(100)` | No | Modelo utilizado: `gpt-4o`, `dall-e-3`, `qwen-max` |
| `tokens_used` | `INT` | Sí | Cantidad de tokens consumidos |

**DDL:**

```sql
CREATE TABLE ai_generations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    original_prompt TEXT NOT NULL,
    generated_text TEXT,
    generated_image_path VARCHAR(512),
    model_used VARCHAR(100) NOT NULL,
    tokens_used INT
);
```

---

## Seguridad

### JWT para API local

- Se genera un JWT al iniciar la aplicación (modo dev) o al autenticar con el frontend.
- El JWT se firma con una clave secreta generada por aplicación (almacenada en `jwt.secret`).
- TTL: 24 horas. En modo `dev` se puede deshabilitar la validación.
- El frontend Electron almacena el JWT en memoria (nunca en `localStorage`).
- Cada request a `/api/*` (excepto `/api/health` y `/api/auth/*`) debe incluir:

```
Authorization: Bearer <jwt>
```

### Spring Security Config

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // API stateless, sin CSRF
            .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

### Almacenamiento seguro de tokens

- Los tokens de acceso de redes sociales se almacenan en SQLite **en la máquina local del usuario**.
- Nunca se envían a servidores externos (la app es local).
- Los tokens expirados se refrescan automáticamente usando `refresh_token` antes de cada publicación.
- Si el refresh falla, la cuenta se marca como `expired` y se notifica al usuario.

---

## Escenarios de prueba

### Escenario 1: Conexión exitosa con Facebook

```
Given: el usuario tiene un código de autorización de Facebook válido
  And: el backend tiene configurado FACEBOOK_APP_ID y FACEBOOK_APP_SECRET
When: el usuario envía POST /api/auth/facebook/authorize con el código
Then: el backend intercambia el código por un access token
  And: el token se almacena en social_accounts
  And: se devuelve HTTP 200 con accountId, pageName y pageId
```

### Escenario 2: Publicación exitosa de un post

```
Given: existe un post en estado "draft" con contentText válido
  And: existe una cuenta de Facebook conectada y con token vigente
When: el usuario envía POST /api/posts/{id}/publish con confirm: true
Then: el backend llama a Facebook Graph API para publicar
  And: el post cambia a estado "published"
  And: se guarda providerPostId devuelto por Facebook
  And: se devuelve HTTP 200 con el post actualizado
```

### Escenario 3: Publicación fallida por token expirado

```
Given: existe un post en estado "draft"
  And: la cuenta de Facebook asociada tiene un token expirado
  And: el refresh_token también es inválido
When: el usuario intenta publicar el post
Then: el backend detecta que el token expiró
  And: intenta refrescar el token automáticamente
  And: el refresh falla
  And: el post cambia a estado "failed"
  And: se guarda el mensaje de error "token_expired"
  And: la cuenta se marca como "expired"
  And: se devuelve HTTP 401 con error facebook_auth_failed
```

### Escenario 4: Generación de copy con IA

```
Given: el backend tiene configurada OPENAI_API_KEY
When: el usuario envía POST /api/content/generate-copy
  And: el body incluye prompt, tone, maxLength, language
Then: el backend llama a OpenAI GPT-4o con el prompt
  And: se recibe el texto generado
  And: se guarda el registro en ai_generations
  And: se devuelve HTTP 200 con generatedText, modelUsed y tokensUsed
```

### Escenario 5: Programación de post para fecha futura

```
Given: el usuario completa el formulario de nuevo post
  And: el campo scheduledAt contiene una fecha futura
When: el usuario envía POST /api/posts con scheduledAt definido
Then: el post se crea en estado "scheduled"
  And: se programa la publicación para la fecha indicada
  And: se devuelve HTTP 201 con id y status "scheduled"
  And: el scheduler ejecuta la publicación automáticamente a la hora programada
```
