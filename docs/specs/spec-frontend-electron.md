# Spec: Frontend Electron — My CMA

## Stack

| Capa | Tecnología | Versión |
|------|-----------|---------|
| Desktop wrapper | Electron | 33.x |
| UI Framework | React | 18.x |
| Lenguaje | TypeScript | 5.x |
| Bundler | Vite | 6.x |
| Estilos | Tailwind CSS | 3.x |
| Estado global | Zustand | 5.x |
| HTTP Client | Axios | 1.x |
| Navegación | React Router DOM | 6.x |
| Iconos | Lucide React | 0.46x |
| Utilidades CSS | clsx | 2.x |

## Arquitectura del Frontend

### Electron Main Process (`electron/main.ts`)

Proceso principal de Electron. Responsabilidades:

- **Backend wrapper**: Spawnea el JAR de Spring Boot (`my-cma-bootstrap-1.0.0-beta.jar`) como proceso hijo en el puerto `8080`. En desarrollo busca el JAR en `backend/my-cma-bootstrap/target/`; en producción lo obtiene de `extraResources`.
- **File dialogs**: Expone `dialog.showOpenDialog` y `dialog.showSaveDialog` vía IPC.
- **App info handlers**: Provee `getBackendUrl`, `getVersion`, `getPlatform` vía IPC.
- **Lifecycle**: Inicia el backend en `app.whenReady`, lo detiene en `window-all-closed` y `before-quit`.
- **Window creation**: `BrowserWindow` de 1280x800 (mínimo 900x600), fondo `#1A1A2E`, `contextIsolation: true`, `nodeIntegration: false`.

### Preload Script (`electron/preload.ts`)

Usa `contextBridge.exposeInMainWorld` para exponer un objeto `electronAPI` seguro al renderer. Sin acceso directo a Node.js ni Electron. API expuesta:

- `openFileDialog(options)` → `ipcRenderer.invoke('dialog:openFile')`
- `saveFileDialog(options)` → `ipcRenderer.invoke('dialog:saveFile')`
- `getBackendUrl()` → `ipcRenderer.invoke('app:getBackendUrl')`
- `getVersion()` → `ipcRenderer.invoke('app:getVersion')`
- `getPlatform()` → `ipcRenderer.invoke('app:getPlatform')`

### React SPA (Renderer Process)

SPA clásica de React montada en `src/main.tsx`. Sin rutas protegidas (app desktop, sin auth login). Toda la lógica de UI vive dentro del `BrowserRouter`.

### Vite Config (`vite.config.ts`)

Usa `vite-plugin-electron` para compilar main + preload en `dist-electron/`, y `vite-plugin-electron-renderer` para exponer módulos de Node seguros al renderer. Alias `@` → `src/`. Dev server en puerto `5173`.

### Flujo de comunicación

```
Componente React
  → api.ts (Axios instance)
    → HTTP request a localhost:8080
      → Spring Boot (backend)
    ← JSON response
  ← actualiza Zustand store
  ← React re-renderiza
```

Para dialogs de archivos locales:

```
Componente React
  → useElectron().openFileDialog()
    → window.electronAPI.openFileDialog()
      → contextBridge → IPC → electron/main.ts
        → dialog.showOpenDialog()
      ← resultado con file paths
    ← paths seleccionados
  ← preview o upload
```

### Variables de entorno

| Variable | Default | Descripción |
|----------|---------|-------------|
| `VITE_API_URL` | `http://localhost:8080` | URL base del backend |
| `VITE_APP_TITLE` | `My CMA` | Título de la app |

## Tipos principales (`src/types/index.ts`)

- `ElectronAPI` — interfaz del API expuesta por preload
- `SocialAccount` — cuenta de red social conectada
- `PostStatus` — `'DRAFT' | 'SCHEDULED' | 'PUBLISHED' | 'FAILED'`
- `Post` — post con contenido, media, hashtags, estado
- `AIGenerationRequest` — request de generación IA
- `AIGeneration` — resultado de generación (copy o imagen)
- `Analytics` — métricas de engagement por post
- `HealthResponse` — respuesta del health check del backend

## Store global (`src/store/index.ts` — Zustand)

| Slice | Estado | Acciones |
|-------|--------|----------|
| Connection | `isBackendConnected` | `setBackendConnected` |
| Social accounts | `accounts: SocialAccount[]` | `setAccounts`, `addAccount`, `removeAccount` |
| Posts | `posts: Post[]` | `setPosts`, `addPost`, `updatePost` |
| AI history | `generations: AIGeneration[]` | `addGeneration` |
| UI | `sidebarOpen: boolean` | `toggleSidebar` |

## Componentes (Átomos, Moléculas, Organismos)

### Layout

#### Sidebar
- **Ruta**: `src/components/layout/Sidebar.tsx`
- **Props**: `isOpen: boolean`, `onToggle: () => void`
- **Estados**:
  - **Open**: width `w-60`, muestra logo y navegación
  - **Collapsed**: width `w-0 -ml-60`, hidden
  - **Active route**: item activo con `bg-[#960018]/10` y borde izquierdo carmín
  - **Inactive route**: texto gris, hover a blanco
- **Navegación**: Dashboard, Contenido, Generar con IA, Analytics, Configuración
- **Logo**: "My" en gris `#B0B0B0`, "CM" en rojo `#960018`, "A" en azul `#003366`

#### TopBar
- **Definida inline en `App.tsx`** (header dentro del layout)
- **Indicador de conexión**: punto verde/rojo + texto "Backend conectado/desconectado"
- **Botón hamburguesa**: toggle sidebar
- **Estados**:
  - **Connected**: punto verde, texto "Backend conectado"
  - **Disconnected**: punto rojo, texto "Backend desconectado"
  - **Loading**: (no aplica — usa pooling cada 30s)

#### Dashboard
- **Ruta**: `src/components/layout/Dashboard.tsx`
- **Props**: ninguna (lee del store)
- **Estados**:
  - **Loading**: skeleton/cards con shimmer (futuro)
  - **Empty**: stats en 0, subtexto "Sin conexiones"
  - **Data**: grid de 4 stat cards + acciones rápidas
  - **Error**: si health check falla, `isBackendConnected = false`, health stats muestran "Offline"
- **Stat cards**: Cuentas conectadas, Posts publicados, Generaciones IA, Backend online/offline
- **Acciones rápidas**: Nuevo post, Generar con IA, Conectar red

### Content

#### ContentEditor
- **Ruta futura**: `src/components/content/`
- **Props**: `post?: Post` (edición) | `undefined` (nuevo)
- **Estados**:
  - **Loading**: editor deshabilitado, spinner
  - **Empty**: textarea vacío, placeholder "Escribí tu contenido..."
  - **Draft**: contenido parcial + hashtags + preview
  - **Saving**: indicador de guardado, debounce automático
  - **Error**: snackbar/alert si falla el guardado
  - **Success**: confirmación visual
- **Subcomponentes**: toolbar de formato (rich text), input de hashtags, selector de media

#### ImageSelector
- **Ruta futura**: `src/components/content/`
- **Props**: `onSelect: (files: string[]) => void`, `maxFiles?: number`
- **Estados**:
  - **Empty**: zona de drop + botón "Seleccionar imágenes"
  - **Preview**: thumbnails de imágenes seleccionadas
  - **Loading**: spinner durante file dialog (Electron) o upload
  - **Error**: si el archivo no es válido o excede tamaño
  - **Too many**: alerta si excede `maxFiles`
- **Comportamiento Electron**: llama a `useElectron().openFileDialog()` con filtro de imágenes
- **Comportamiento Web**: fallback a `<input type="file">`

#### PostPreview
- **Ruta futura**: `src/components/content/`
- **Props**: `post: Post`, `platform: 'facebook' | 'instagram' | 'linkedin'`
- **Estados**:
  - **Empty**: "Sin contenido para previsualizar"
  - **Preview**: renderiza mock de cómo se ve el post en cada red
  - **Error**: si faltan datos obligatorios para la red

### AI

#### AIPanel
- **Ruta futura**: `src/components/ai/`
- **Props**: `onGenerate: (text: string) => void` (callback para insertar en editor)
- **Panel lateral** dentro de la vista de creación de contenido
- **Estados**:
  - **Idle**: selector de tipo (copy/image) + campo de prompt
  - **Loading**: spinner + "Generando..."
  - **Success**: resultado mostrado con opción "Insertar" o "Regenerar"
  - **Error**: mensaje de error + botón reintentar
  - **Empty**: "No se generó contenido aún"

#### CopyGenerator
- **Ruta futura**: `src/components/ai/`
- **Props**: `prompt: string`, `tone: 'professional' | 'casual' | 'humorous' | 'inspirational'`
- **Estados**:
  - **Idle**: formulario prompt + selector de tono
  - **Loading**: skeleton de texto animado
  - **Success**: texto generado + botones "Copiar" / "Insertar en editor" / "Regenerar"
  - **Error**: mensaje específico del error + reintentar

#### ImageGenerator
- **Ruta futura**: `src/components/ai/`
- **Props**: `prompt: string`, `size?: 'square' | 'landscape' | 'portrait'`
- **Estados**:
  - **Idle**: input prompt + selector tamaño
  - **Loading**: skeleton de imagen animado
  - **Success**: preview de imagen generada + botones "Descargar" / "Insertar"
  - **Error**: mensaje de error + reintentar

### Social

#### AccountConnect
- **Ruta futura**: `src/components/social/`
- **Props**: `provider: 'facebook' | 'instagram' | 'linkedin'`
- **Estados**:
  - **Disconnected**: botón "Conectar [red]" con logo de la red
  - **Connecting**: spinner + "Abriendo navegador para autenticación..."
  - **Connected**: card con info de la cuenta + botón "Desconectar"
  - **Expired**: badge "Token expirado" + botón "Reconectar"
  - **Error**: mensaje de error de autenticación
- **Flujo OAuth**: Componente → api.ts → backend (`/auth/{provider}/url`) → abre `shell.openExternal` → callback → backend intercambia code por token → store.addAccount

#### PublishButton
- **Ruta futura**: `src/components/social/`
- **Props**: `post: Post`, `onPublish: () => Promise<void>`
- **Estados**:
  - **Ready**: botón "Publicar" primario
  - **Confirming**: modal de doble confirmación con resumen del post
  - **Publishing**: spinner + "Publicando en [red]..."
  - **Success**: checkmark + "Publicado exitosamente"
  - **Error**: mensaje de error + botón reintentar
  - **Scheduled**: botón "Programar" → date picker → "Programado"

### Analytics

#### AnalyticsChart
- **Ruta futura**: `src/components/analytics/`
- **Props**: `data: Analytics[]`, `metric: 'reach' | 'likes' | 'comments' | 'shares' | 'impressions'`
- **Estados**:
  - **Loading**: skeleton chart animado
  - **Empty**: "Sin datos de analytics — conectá una red social y publicá contenido"
  - **Data**: gráfico de línea/barra con métricas
  - **Error**: mensaje de error + reintentar
  - **No accounts**: "No hay cuentas conectadas para mostrar analytics"

## Flujos de usuario (escenarios)

### 1. Conexión con red social

1. Usuario navega a Settings → Social
2. Ve lista de proveedores (Facebook, Instagram, LinkedIn)
3. Click "Conectar" en el proveedor deseado
4. Backend devuelve URL de OAuth via `GET /auth/{provider}/url`
5. Electron abre la URL en el navegador del sistema (`shell.openExternal`)
6. Usuario autoriza en el navegador
7. Backend recibe callback con `code`, lo intercambia por `access_token`
8. Backend guarda token y devuelve `SocialAccount`
9. Frontend recibe vía polling o callback manual → `addAccount(account)`
10. UI muestra cuenta conectada con checkmark

**Extensión futura**: Server-Sent Events o WebSocket para callback automático.

### 2. Crear y publicar post

1. Usuario navega a "/content" o click "Nuevo post" en Dashboard
2. ContentEditor se muestra vacío
3. Usuario escribe contenido, agrega hashtags, selecciona imágenes
4. ImageSelector: en Electron usa file dialog nativo; en web usa `<input type="file">`
5. PostPreview muestra cómo se verá (opcional, seleccionando red)
6. Usuario selecciona red(es) destino
7. Click "Publicar"
8. PublishButton muestra modal de confirmación
9. Usuario confirma
10. `POST /api/posts` al backend
11. Backend publica en la(s) red(es) vía su API
12. UI muestra "Publicado exitosamente" + actualiza store

### 3. Generar contenido con IA

1. Usuario navega a "/ai" o click "Generar con IA" en Dashboard
2. AIPanel muestra selector: "Copy" o "Imagen"
3. **Copy**:
   a. CopyGenerator muestra campo de prompt + selector de tono
   b. Usuario escribe prompt ej: "Anuncio para nuevo producto de skincare"
   c. Selecciona tono "professional"
   d. Click "Generar"
   e. `POST /api/ai/generate-copy` → backend → LLM
   f. Resultado aparece en panel
   g. Usuario puede "Insertar en editor" (navega a "/content" con texto precargado)
4. **Imagen**:
   a. ImageGenerator muestra campo de prompt + selector de tamaño
   b. Usuario escribe prompt
   c. Click "Generar"
   d. `POST /api/ai/generate-image` → backend → Stable Diffusion / DALL-E
   e. Preview de imagen generada
   f. Usuario puede "Descargar" o "Insertar en post"

### 4. Programar publicación

1. Usuario completa ContentEditor normalmente
2. En vez de click "Publicar", click "Programar"
3. Date picker (nativo con `dialog.showSaveDialog` o custom) aparece
4. Usuario selecciona fecha y hora futura
5. Click "Programar"
6. `POST /api/posts` con `scheduledAt` seteado
7. UI muestra "Post programado para [fecha]"
8. Post aparece en store con `status: 'SCHEDULED'`

## Estados globales de la UI

| Estado | Condición | Comportamiento |
|--------|-----------|----------------|
| Backend desconectado | health check falla | Punto rojo en TopBar. Posts no se pueden crear/publicar. Mostrar banner "Backend no disponible — funcionalidades limitadas" |
| Backend conectado | health check OK | Punto verde. Operaciones normales |
| Sin cuentas conectadas | `accounts.length === 0` | Dashboard muestra "Sin conexiones". PublishButton deshabilitado. Analytics muestra "Conectá una red social" |
| Sin posts | `posts.length === 0` | Dashboard muestra 0. Vacio con CTA "Crear tu primer post" |
| Sin generaciones IA | `generations.length === 0` | AIPanel muestra empty state con ejemplo de prompt |

## Colores de la marca

| Elemento | Hex | Uso |
|----------|-----|-----|
| My (gris claro) | `#B0B0B0` | "My" en logo, texto secundario |
| C (rojo carmín) | `#960018` | "CM" en logo, acentos, hover states |
| M (rojo carmín claro) | `#C41E3A` | Active nav link, hover alternativo |
| A (azul náutico) | `#003366` | "A" en logo, backgrounds acento |
| Fondo oscuro | `#0f0f1a` | Fondo principal |
| Fondo paneles | `#1a1a2e` | Sidebar, cards, headers |
| Borde | `#1f2937` (gray-800) | Separadores, bordes de cards |
| Texto primario | `#ffffff` | Títulos, labels |
| Texto secundario | `#9ca3af` (gray-400) | Subtítulos, descripciones |
| Texto terciario | `#6b7280` (gray-500) | Footers, metadata |

## Estructura de carpetas del frontend

```
frontend/
├── electron/
│   ├── main.ts              # Electron main process
│   └── preload.ts            # Context bridge seguro
├── public/
│   └── icon.png              # Icono de la app
├── src/
│   ├── components/
│   │   ├── layout/           # Sidebar, Dashboard, TopBar
│   │   ├── content/          # ContentEditor, ImageSelector, PostPreview
│   │   ├── ai/               # AIPanel, CopyGenerator, ImageGenerator
│   │   ├── social/           # AccountConnect, PublishButton
│   │   └── analytics/        # AnalyticsChart
│   ├── hooks/
│   │   └── useElectron.ts    # Hook para acceder a Electron API
│   ├── services/
│   │   └── api.ts            # Axios instance + endpoints
│   ├── store/
│   │   └── index.ts          # Zustand store global
│   ├── types/
│   │   └── index.ts          # Interfaces de TypeScript
│   ├── App.tsx               # Root component con routing
│   ├── index.css             # Estilos globales + Tailwind
│   └── main.tsx              # Entry point React
├── index.html                # HTML template
├── package.json
├── postcss.config.js
├── tailwind.config.js
├── tsconfig.json
└── vite.config.ts
```
