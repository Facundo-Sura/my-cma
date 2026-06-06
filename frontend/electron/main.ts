import { app, BrowserWindow, ipcMain, dialog } from 'electron';
import { spawn, ChildProcess } from 'child_process';
import path from 'path';
import fs from 'fs';

let mainWindow: BrowserWindow | null = null;
let backendProcess: ChildProcess | null = null;

const BACKEND_PORT = 8080;
const BACKEND_URL = `http://localhost:${BACKEND_PORT}`;
const isDev = !app.isPackaged;

function getBackendJarPath(): string {
  if (isDev) {
    // Development: look for the JAR in the target directory
    return path.join(__dirname, '..', '..', 'backend', 'my-cma-bootstrap', 'target', 'my-cma-bootstrap-1.0.0-beta.jar');
  }
  // Production: bundled as extraResource
  return path.join(process.resourcesPath, 'backend', 'my-cma-backend.jar');
}

function startBackend(): void {
  const jarPath = getBackendJarPath();

  if (!fs.existsSync(jarPath)) {
    console.warn(`Backend JAR not found at: ${jarPath}. Starting without backend.`);
    return;
  }

  const userDataDir = path.join(app.getPath('userData'), 'data');
  if (!fs.existsSync(userDataDir)) {
    fs.mkdirSync(userDataDir, { recursive: true });
  }

  backendProcess = spawn('java', [
    '-jar', jarPath,
    `--server.port=${BACKEND_PORT}`,
    `--mycma.data-dir=${userDataDir}`,
  ], {
    stdio: ['ignore', 'pipe', 'pipe'],
  });

  backendProcess.stdout?.on('data', (data: Buffer) => {
    console.log(`[Backend] ${data.toString().trim()}`);
  });

  backendProcess.stderr?.on('data', (data: Buffer) => {
    console.error(`[Backend ERR] ${data.toString().trim()}`);
  });

  backendProcess.on('exit', (code: number | null) => {
    console.log(`Backend exited with code ${code}`);
    backendProcess = null;
  });

  console.log(`Backend starting on port ${BACKEND_PORT}...`);
}

async function stopBackend(): Promise<void> {
  if (backendProcess) {
    console.log('Stopping backend...');
    backendProcess.kill('SIGTERM');
    backendProcess = null;
    // Give it a moment to shut down gracefully
    await new Promise(resolve => setTimeout(resolve, 2000));
  }
}

function createWindow(): void {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 800,
    minWidth: 900,
    minHeight: 600,
    title: 'My CMA',
    backgroundColor: '#1A1A2E',
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      nodeIntegration: false,
      contextIsolation: true,
    },
    show: false,
  });

  if (isDev) {
    mainWindow.loadURL('http://localhost:5173');
    mainWindow.webContents.openDevTools();
  } else {
    mainWindow.loadFile(path.join(__dirname, '..', 'dist', 'index.html'));
  }

  mainWindow.once('ready-to-show', () => {
    mainWindow?.show();
  });

  mainWindow.on('closed', () => {
    mainWindow = null;
  });
}

// IPC Handlers
ipcMain.handle('dialog:openFile', async (_event, options: Electron.OpenDialogOptions) => {
  const result = await dialog.showOpenDialog(mainWindow!, options);
  return result;
});

ipcMain.handle('dialog:saveFile', async (_event, options: Electron.SaveDialogOptions) => {
  const result = await dialog.showSaveDialog(mainWindow!, options);
  return result;
});

ipcMain.handle('app:getBackendUrl', () => BACKEND_URL);
ipcMain.handle('app:getVersion', () => app.getVersion());
ipcMain.handle('app:getPlatform', () => process.platform);

// App lifecycle
app.whenReady().then(async () => {
  startBackend();
  createWindow();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    }
  });
});

app.on('window-all-closed', async () => {
  await stopBackend();
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('before-quit', async () => {
  await stopBackend();
});
