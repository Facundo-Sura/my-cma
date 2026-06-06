/**
 * Hook for accessing Electron APIs exposed via preload script.
 * Safe to use in both Electron and browser environments.
 */
export function useElectron() {
  const electronAPI = window.electronAPI;

  const isElectron = !!electronAPI;

  async function openFileDialog(options?: Partial<Electron.OpenDialogOptions>) {
    if (!electronAPI) return null;
    const defaults: Electron.OpenDialogOptions = {
      properties: ['openFile'],
      filters: [
        { name: 'Images', extensions: ['jpg', 'png', 'jpeg', 'gif', 'webp'] },
        { name: 'Videos', extensions: ['mp4', 'mov', 'avi', 'webm'] },
      ],
    };
    return electronAPI.openFileDialog({ ...defaults, ...options });
  }

  async function saveFileDialog(options?: Partial<Electron.SaveDialogOptions>) {
    if (!electronAPI) return null;
    return electronAPI.saveFileDialog(options ?? {});
  }

  return {
    isElectron,
    openFileDialog,
    saveFileDialog,
    getVersion: electronAPI?.getVersion ?? (() => Promise.resolve('1.0.0-beta')),
    getPlatform: electronAPI?.getPlatform ?? (() => Promise.resolve('web')),
  };
}
