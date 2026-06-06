import { contextBridge, ipcRenderer } from 'electron';

/**
 * Exposes a safe API to the renderer process via contextBridge.
 * No direct access to Node.js or Electron APIs is granted.
 */
contextBridge.exposeInMainWorld('electronAPI', {
  // Dialog
  openFileDialog: (options: Electron.OpenDialogOptions) =>
    ipcRenderer.invoke('dialog:openFile', options),

  saveFileDialog: (options: Electron.SaveDialogOptions) =>
    ipcRenderer.invoke('dialog:saveFile', options),

  // App info
  getBackendUrl: (): Promise<string> =>
    ipcRenderer.invoke('app:getBackendUrl'),

  getVersion: (): Promise<string> =>
    ipcRenderer.invoke('app:getVersion'),

  getPlatform: (): Promise<string> =>
    ipcRenderer.invoke('app:getPlatform'),
});
