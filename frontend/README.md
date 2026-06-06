# My CMA — Frontend

Aplicación de escritorio para Community Manager Assistant, construida con Electron + React + TypeScript.

## Stack

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| Electron | 33.x | Desktop wrapper nativo (Windows, macOS, Linux) |
| React | 18.x | UI declarativa con componentes |
| TypeScript | 5.x | Tipado estático |
| Vite | 6.x | Bundler ultrarrápido con HMR |
| Tailwind CSS | 3.x | Estilos utilitarios |
| Zustand | 5.x | Estado global liviano |
| Axios | 1.x | Cliente HTTP para comunicación con backend |
| React Router DOM | 6.x | Enrutamiento SPA |
| Lucide React | 0.46x | Iconos SVG |
| clsx | 2.x | Utilidad de clases condicionales |

## Requisitos

- Node.js >= 18
- npm >= 9

## Instalación

```bash
cd frontend
npm install
```

## Desarrollo

### Modo web (solo React, sin Electron)

```bash
npm run dev
```

Abre `http://localhost:5173` en el navegador. Ideal para desarrollo rápido de UI sin Electron. El backend se espera corriendo en `localhost:8080`.

### Modo Electron (app de escritorio completa)

```bash
npm run electron:dev
```

Compila main + preload, inicia Vite, espera a que esté listo y lanza la ventana de Electron. El proceso main spawnea automáticamente el JAR del backend.

### Comandos disponibles

| Comando | Descripción |
|---------|-------------|
| `npm run dev` | Dev server web (Vite) en puerto 5173 |
| `npm run build` | Compila TypeScript + Vite build |
| `npm run electron:dev` | Dev completo con Electron |
| `npm run electron:build` | Build + empaquetado con electron-builder |
| `npm run preview` | Preview del build de producción |
| `npm run lint` | ESLint sobre `src/` |
| `npm run typecheck` | TypeScript type check sin emitir |

## Build para distribución

```bash
npm run build
npm run electron:build
```

Genera un instalador NSIS en `dist-electron/`. La configuración de empaquetado está en `package.json` bajo la clave `"build"`.

## Estructura de carpetas

```
frontend/
├── electron/
│   ├── main.ts              # Main process: ventana, backend JAR, IPC
│   └── preload.ts            # contextBridge seguro para el renderer
├── public/
│   └── icon.png              # Icono de la aplicación
├── src/
│   ├── components/
│   │   ├── layout/           # Sidebar, Dashboard (pantalla principal)
│   │   ├── content/          # Editor de contenido, selector de imágenes, preview
│   │   ├── ai/               # Panel de IA, generación de copy e imágenes
│   │   ├── social/           # Conexión de redes sociales, botón publicar
│   │   └── analytics/        # Gráficos de métricas
│   ├── hooks/
│   │   └── useElectron.ts    # Hook para acceder al API expuesta por preload
│   ├── services/
│   │   └── api.ts            # Instancia de Axios + métodos para cada endpoint
│   ├── store/
│   │   └── index.ts          # Estado global con Zustand
│   ├── types/
│   │   └── index.ts          # Interfaces de TypeScript (Post, SocialAccount, etc.)
│   ├── App.tsx               # Layout raíz con router y health check
│   ├── index.css             # Directivas Tailwind + estilos globales
│   └── main.tsx              # Entry point de React
├── index.html
├── package.json
├── postcss.config.js
├── tailwind.config.js
├── tsconfig.json
└── vite.config.ts
```

## Hooks disponibles

### `useElectron()`

Hook que envuelve el acceso a `window.electronAPI` (expuesto por el preload script). Es seguro llamarlo desde cualquier entorno (Electron o web).

```ts
import { useElectron } from '../hooks/useElectron';

function MyComponent() {
  const { isElectron, openFileDialog, saveFileDialog, getVersion, getPlatform } = useElectron();

  const handleSelectImage = async () => {
    if (!isElectron) {
      // fallback a <input type="file"> nativo del browser
      return;
    }
    const result = await openFileDialog({
      properties: ['openFile', 'multiSelections'],
      filters: [{ name: 'Images', extensions: ['jpg', 'png', 'webp'] }],
    });
    if (!result.canceled && result.filePaths.length > 0) {
      // usar result.filePaths
    }
  };
}
```

**API retornada:**

| Propiedad | Tipo | Descripción |
|-----------|------|-------------|
| `isElectron` | `boolean` | `true` si se ejecuta dentro de Electron |
| `openFileDialog` | `(options?) => Promise` | Abre diálogo nativo de selección de archivos |
| `saveFileDialog` | `(options?) => Promise` | Abre diálogo nativo de guardado |
| `getVersion` | `() => Promise<string>` | Versión de la app |
| `getPlatform` | `() => Promise<string>` | Plataforma actual (`win32`, `darwin`, `linux`, `web`) |

## Servicios

### `api.ts` (`src/services/api.ts`)

Instancia de Axios con base URL configurable vía `VITE_API_URL`.

```ts
import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const api = axios.create({
  baseURL: API_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});
```

Endpoints implementados (según tipos en `src/types/index.ts`):

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/health` | Health check del backend |
| `GET` | `/api/posts` | Listar posts |
| `POST` | `/api/posts` | Crear post |
| `PUT` | `/api/posts/{id}` | Actualizar post |
| `DELETE` | `/api/posts/{id}` | Eliminar post |
| `GET` | `/api/social/accounts` | Listar cuentas conectadas |
| `POST` | `/api/social/{provider}/connect` | Iniciar OAuth |
| `POST` | `/api/social/{provider}/disconnect` | Desconectar cuenta |
| `POST` | `/api/ai/generate-copy` | Generar texto con IA |
| `POST` | `/api/ai/generate-image` | Generar imagen con IA |
| `GET` | `/api/analytics/{postId}` | Obtener analytics de un post |
| `GET` | `/api/analytics/summary` | Resumen de analytics |

## Variables de entorno

Crear archivo `.env` en `frontend/`:

```env
VITE_API_URL=http://localhost:8080
VITE_APP_TITLE=My CMA
```

| Variable | Obligatorio | Default | Descripción |
|----------|-------------|---------|-------------|
| `VITE_API_URL` | No | `http://localhost:8080` | URL base del backend Spring Boot |
| `VITE_APP_TITLE` | No | `My CMA` | Título de la aplicación |

## Store global (Zustand)

El estado global se maneja con Zustand en `src/store/index.ts`. Slice principal:

- **Connection**: `isBackendConnected` — estado del backend vía health check periódico
- **Social accounts**: `accounts[]` — cuentas de redes sociales conectadas
- **Posts**: `posts[]` — posts creados con sus estados
- **AI history**: `generations[]` — historial de generaciones IA
- **UI**: `sidebarOpen` — estado del sidebar colapsable

## Comunicación con backend

El frontend se comunica con el backend Spring Boot via HTTP REST en `localhost:8080`. En modo Electron, el proceso main spawnea el JAR automáticamente. En modo web, el backend debe estar corriendo por separado.

El health check se ejecuta al montar la app y cada 30 segundos. Si el backend no responde, se muestra un indicador rojo en la TopBar y las operaciones que dependen del backend muestran estados de error controlados.

## Notas

- Los componentes dentro de `src/components/content/`, `ai/`, `social/` y `analytics/` están planificados pero aún no implementados. La app funcional actual incluye Layout (Sidebar + Dashboard + TopBar con routing).
- El hook `useElectron` es seguro de usar en web (devuelve valores default cuando `window.electronAPI` no existe).
- Los estilos siguen la paleta de colores de la marca: fondos `#0f0f1a` y `#1a1a2e`, acentos en `#960018` (carmín), `#C41E3A` (carmín claro), `#003366` (azul náutico), y `#B0B0B0` (gris claro).
