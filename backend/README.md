# My CMA — Backend

Backend de **Community Manager Assistant**, aplicación de escritorio para gestión y publicación centralizada de contenido en redes sociales con generación asistida por IA.

---

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 25 |
| Framework | Spring Boot 3.4.2 |
| Build | Maven 3.9+ (Multi-Module) |
| Base de datos | SQLite 3.46 (embebido vía JDBC + Hibernate) |
| Seguridad | Spring Security + JWT (local) |
| Social API | Facebook Graph API v21 (próximamente Instagram, LinkedIn) |
| AI Provider | OpenAI GPT-4o / DALL-E 3 (Azure SDK), Qwen (Alibaba Cloud) |
| Testing | JUnit 5 + Mockito + Spring Boot Test |

---

## Arquitectura hexagonal

```
                         ┌──────────────────────┐
                         │    FRONTEND (React)   │
                         │    Electron Desktop   │
                         └─────────┬────────────┘
                                   │ HTTP :8080
                         ┌─────────▼────────────┐
                         │     BOOTSTRAP         │
                         │  REST Controllers     │
                         │  Spring Security      │
                         │  JWT Filter           │
                         └─────────┬────────────┘
                                   │
          ┌────────────────────────┼────────────────────────┐
          │   APPLICATION LAYER    │    (Casos de uso)      │
          │  ┌──────────┐ ┌───────┴────┐ ┌──────────┐      │
          │  │ Content  │ │ Publishing │ │Analytics │      │
          │  │ UseCases │ │  UseCases  │ │ UseCases │      │
          │  └────┬─────┘ └─────┬──────┘ └────┬─────┘      │
          │       │             │             │             │
          │       └──────┬──────┘             │             │
          │              │ (injects ports)    │             │
          ├──────────────┼──────────────────────────────────┤
          │   DOMAIN     │  (Core — sin dependencias)       │
          │  ┌───────────▼────────────────────────────┐    │
          │  │  PUERTOS (interfaces)                  │    │
          │  │  SocialMediaPort   ←── publicar/leer   │    │
          │  │  AIGenerationPort ←── generar copy/img │    │
          │  │  PostRepository   ←── CRUD posts       │    │
          │  │  AccountRepository←── CRUD cuentas     │    │
          │  └───────────▲────────────────────────────┘    │
          │              │                                  │
          ├──────────────┼──────────────────────────────────┤
          │ INFRASTRUCT. │  (Adaptadores)                   │
          │  ┌───────────┴───────┬──────────────┬────────┐ │
          │  │ Social Adapters   │ AI Adapters  │ Persist│ │
          │  │ ┌─────────────┐  ┌───────┐    ┌──────┐ │ │
          │  │ │FacebookGraph│  │OpenAI │    │ JPA  │ │ │
          │  │ │Instagram    │  │Qwen   │    │SQLite│ │ │
          │  │ │LinkedIn     │  └───────┘    └──────┘ │ │
          │  │ └─────────────┘                       │ │
          │  └───────────────────────────────────────┘ │
          └────────────────────────────────────────────┘
```

### Principios

- **Dominio puro** (`my-cma-core`): cero dependencias de frameworks. Solo contiene entidades, value objects e interfaces de puerto.
- **Inversión de dependencias**: los módulos de aplicación dependen de interfaces (puertos) definidas en core. Los adaptadores implementan esas interfaces.
- **Los adaptadores no conocen los casos de uso**: la comunicación es siempre unidireccional hacia adentro.

---

## Cómo compilar

```bash
cd backend
mvn clean install -DskipTests
```

Esto compila todos los módulos en orden: `core` → `infrastructure-*` → `application-*` → `bootstrap`.

Para compilar sin tests (recomendado hasta que se implementen):

```bash
mvn clean install -DskipTests
```

---

## Cómo ejecutar

```bash
cd backend/my-cma-bootstrap
mvn spring-boot:run
```

O desde el JAR empaquetado:

```bash
cd backend
mvn clean package -DskipTests
java -jar my-cma-bootstrap/target/my-cma-bootstrap-1.0.0-beta.jar
```

La aplicación arranca en `http://localhost:8080`. El health check está en `http://localhost:8080/api/health`.

---

## Variables de entorno

| Variable | Requerida | Descripción |
|----------|-----------|-------------|
| `OPENAI_API_KEY` | Sí | API key de OpenAI (Azure) para generación de contenido |
| `OPENAI_ENDPOINT` | Sí | Endpoint del recurso Azure OpenAI |
| `OPENAI_DEPLOYMENT_GPT` | Sí | Deployment name para GPT-4o |
| `OPENAI_DEPLOYMENT_DALLE` | Sí | Deployment name para DALL-E 3 |
| `FACEBOOK_APP_ID` | Sí | App ID de Facebook Developer |
| `FACEBOOK_APP_SECRET` | Sí | App Secret de Facebook Developer |
| `JWT_SECRET` | No | Clave secreta para firmar JWT. Si no se provee, se genera una aleatoria al iniciar |
| `SPRING_PROFILES_ACTIVE` | No | Perfil activo: `dev`, `sandbox`, `prod` (default: `dev`) |
| `MY_CMA_DATA_DIR` | No | Directorio donde se almacena SQLite y archivos generados (default: `~/.my-cma`) |

---

## Estructura de módulos

```
backend/
├── pom.xml                                    # Parent POM (multi-module)
├── my-cma-core/                               # Dominio puro
│   └── src/main/java/com/mycma/core/
│       ├── domain/                            # Entidades y value objects
│       ├── port/                              # Interfaces de puerto (inbound/outbound)
│       └── vo/                                # Value objects (SocialProvider, PostStatus, etc.)
├── my-cma-infrastructure-persistence/         # Persistencia JPA + SQLite
│   └── src/main/java/com/mycma/infrastructure/persistence/
│       ├── entity/                            # Entidades JPA
│       ├── repository/                        # Implementación Spring Data JPA
│       └── mapper/                            # Mappers JPA → dominio
├── my-cma-infrastructure-social-facebook/     # Adaptador Facebook Graph API
│   └── src/main/java/com/mycma/infrastructure/social/facebook/
│       ├── client/                            # Cliente HTTP Graph API
│       ├── auth/                              # Flujo OAuth 2.0
│       └── mapper/                            # Respuestas → dominio
├── my-cma-infrastructure-social-instagram/    # Adaptador Instagram Graph API
├── my-cma-infrastructure-social-linkedin/     # Adaptador LinkedIn Marketing API
├── my-cma-infrastructure-ai-openai/           # Adaptador OpenAI (Azure SDK)
│   └── src/main/java/com/mycma/infrastructure/ai/openai/
│       ├── client/                            # Cliente Azure OpenAI
│       └── mapper/                            # Respuestas → dominio
├── my-cma-infrastructure-ai-qwen/             # Adaptador Qwen (alternativo)
├── my-cma-application-content/               # Casos de uso: generación de contenido
│   └── src/main/java/com/mycma/application/content/
│       ├── usecase/                           # GenerateCopyUseCase, GenerateImageUseCase
│       └── dto/                               # Request/Response DTOs
├── my-cma-application-publishing/            # Casos de uso: publicación
│   └── src/main/java/com/mycma/application/publishing/
│       ├── usecase/                           # PublishPostUseCase, SchedulePostUseCase
│       └── dto/
├── my-cma-application-analytics/             # Casos de uso: analytics
│   └── src/main/java/com/mycma/application/analytics/
│       ├── usecase/                           # GetPostAnalyticsUseCase
│       └── dto/
└── my-cma-bootstrap/                          # Entry point + REST controllers
    └── src/main/java/com/mycma/bootstrap/
        ├── config/                            # Spring Config (Security, JWT, CORS)
        ├── controller/                        # REST controllers
        └── MyCmaApplication.java              # @SpringBootApplication
```

---

## Perfiles de Spring

### `dev` (default)

- JWT validation deshabilitada (o token fijo para desarrollo)
- Logs en nivel DEBUG
- Habilita CORS para `http://localhost:5173` (Vite dev server)
- Base de datos: `~/.my-cma/my-cma-dev.db`

### `sandbox`

- Simula respuestas de redes sociales sin llamadas reales
- Publicaciones simuladas: el post se marca como `published` sin llamar a la API externa
- Ideal para pruebas de integración del frontend
- Base de datos: `~/.my-cma/my-cma-sandbox.db`

### `prod`

- JWT validation obligatoria
- Logs en nivel INFO/WARN
- CORS deshabilitado (solo localhost)
- Todos los adaptadores reales habilitados
- Base de datos: `~/.my-cma/my-cma.db`

---

## Testing (futuro)

| Tipo | Framework | Objetivo |
|------|-----------|---------|
| Unitarios | JUnit 5 + Mockito | Entidades de dominio, value objects, reglas de negocio |
| De integración | Spring Boot Test | Repositorios JPA, adaptadores mockeados, casos de uso |
| De contrato | REST Assured | Controllers, validación de requests/responses |
| End-to-end | Playwright + Testcontainers | Flujo completo frontend → backend → SQLite |

Los tests unitarios del core no deben depender de Spring ni de infraestructura.

Patrón recomendado:

```java
class PublishPostUseCaseTest {

    private final SocialMediaPort socialMedia = mock(SocialMediaPort.class);
    private final PostRepository postRepo = mock(PostRepository.class);
    private final PublishPostUseCase useCase = new PublishPostUseCase(socialMedia, postRepo);

    @Test
    void shouldPublishSuccessfully() {
        // Given
        var post = Post.createDraft("Hello!", Provider.FACEBOOK);
        when(postRepo.findById(1L)).thenReturn(Optional.of(post));
        when(socialMedia.publish(post)).thenReturn("fb_post_123");

        // When
        var result = useCase.execute(new PublishRequest(1L, true));

        // Then
        assertThat(result.status()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(result.providerPostId()).isEqualTo("fb_post_123");
        verify(postRepo).save(any());
    }
}
```
