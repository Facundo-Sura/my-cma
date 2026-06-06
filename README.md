# My CMA — Community Manager Assistant

![Version](https://img.shields.io/badge/version-1.0.0--beta-blue)
![Backend](https://img.shields.io/badge/backend-Spring%20Boot%203.4-brightgreen)
![Frontend](https://img.shields.io/badge/frontend-Electron%20%2B%20React-blueviolet)
![License](https://img.shields.io/badge/license-MIT-green)

**My CMA** es una aplicación de escritorio diseñada para gestionar, automatizar y optimizar la publicación de contenido en redes sociales. Actúa como un asistente inteligente para Community Managers, combinando generación de contenido con IA y publicación centralizada.

---

## ✨ Funcionalidades

| Funcionalidad | Estado |
|--------------|--------|
| Conexión con Facebook (páginas) | 🔜 MVP |
| Conexión con Instagram Business | 🔜 Futuro |
| Conexión con LinkedIn Company | 🔜 Futuro |
| Editor de contenido con preview | 🔜 MVP |
| Generación de copies con GPT-4o | 🔜 MVP |
| Generación de imágenes con DALL-E 3 | 🔜 MVP |
| Programación de publicaciones | 🔜 MVP |
| Publicación con confirmación manual | 🔜 MVP |
| Dashboard de analytics | 🔜 Futuro |
| Modo sandbox para pruebas | 🔜 MVP |

---

## 🏗️ Arquitectura

```
┌──────────────────────────────────────────┐
│          ELECTRON APP (Desktop)          │
│  ┌────────────────────────────────────┐  │
│  │  React + Tailwind + Zustand (UI)   │  │
│  └──────────────┬─────────────────────┘  │
│                 │ HTTP localhost:8080     │
│  ┌──────────────▼─────────────────────┐  │
│  │     SPRING BOOT (Backend local)    │  │
│  │                                    │  │
│  │  ┌─────────┐  ┌────────────────┐  │  │
│  │  │  Core   │  │  Application   │  │  │
│  │  │(Dominio)│  │  (Casos de uso)│  │  │
│  │  └────┬────┘  └───────┬────────┘  │  │
│  │       │               │           │  │
│  │  ┌────▼───────────────▼────────┐  │  │
│  │  │      Infrastructure         │  │  │
│  │  │  ┌──────┬──────┬────────┐   │  │  │
│  │  │  │Social │  AI  │ Persist│   │  │  │
│  │  │  │Adapter│Adapter│ (SQLite)│  │  │  │
│  │  │  └──────┴──────┴────────┘   │  │  │
│  │  └────────────────────────────┘  │  │
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
```

### Stack técnico

| Capa | Tecnología |
|------|-----------|
| **Backend** | Java 25, Spring Boot 3.4, Spring Security, JPA |
| **Base de datos** | SQLite (embebido, portable) |
| **Frontend** | React 18, TypeScript, Tailwind CSS, Zustand |
| **Desktop** | Electron 33 + Vite |
| **IA** | OpenAI GPT-4o + DALL-E 3 (pluggable: Qwen, Gemini) |
| **Social** | Facebook Graph API v21 (próximamente LinkedIn, IG) |

---

## 📁 Estructura del proyecto

```
my-cma/
├── backend/                          # Maven Multi-Module (Modular Monolith)
│   ├── my-cma-core/                 # Dominio puro, interfaces (puertos)
│   ├── my-cma-infrastructure-*/     # Adaptadores (Facebook, OpenAI, Persistencia)
│   ├── my-cma-application-*/        # Casos de uso (Content, Publishing, Analytics)
│   └── my-cma-bootstrap/            # Spring Boot entry point + REST controllers
│
├── frontend/                         # Electron + React + Vite
│   ├── electron/                     # Main process + preload
│   └── src/                          # React app (components, hooks, store)
│
├── docs/                             # Documentación SDD + ADRs
└── README.md
```

---

## 🚀 Primeros pasos

### Prerrequisitos

- **Java 25+** y **Maven 3.9+**
- **Node.js 20+** y **npm 10+**
- **Git** (para control de versiones)
- **GitHub CLI** (opcional, para gestión de repositorio)

### Desarrollo local

```bash
# 1. Iniciar backend
cd backend
mvn clean install -DskipTests
cd my-cma-bootstrap
mvn spring-boot:run

# 2. En otra terminal, iniciar frontend
cd frontend
npm install
npm run dev         # Solo frontend web
npm run electron:dev # Frontend + Electron
```

### Construir instalador

```bash
cd frontend
npm run electron:build
# El instalador se genera en frontend/dist-electron/
```

---

## 🧪 Modo Sandbox

El modo sandbox permite probar la aplicación sin cuentas reales de redes sociales. Las publicaciones se simulan localmente.

```bash
# Activar modo sandbox
mvn spring-boot:run -Dspring.profiles.active=sandbox
```

---

## 🔐 Seguridad

- Las credenciales de redes sociales se almacenan localmente en SQLite (nunca se envían a servidores externos)
- La publicación requiere confirmación manual (no automática)
- JWT para proteger la API local (localhost solamente)
- Los tokens de acceso se refrescan automáticamente

---

## 📄 Licencia

MIT — Ver archivo [LICENSE](LICENSE) para más detalles.
