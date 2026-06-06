import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { useEffect } from 'react';
import { useAppStore } from './store';
import { api } from './services/api';
import Dashboard from './components/layout/Dashboard';
import Sidebar from './components/layout/Sidebar';

function App() {
  const { isBackendConnected, setBackendConnected, sidebarOpen, toggleSidebar } = useAppStore();

  // Check backend health on mount
  useEffect(() => {
    const checkHealth = async () => {
      try {
        const health = await api.checkHealth();
        setBackendConnected(health.status === 'UP');
      } catch {
        setBackendConnected(false);
      }
    };

    checkHealth();
    const interval = setInterval(checkHealth, 30000); // every 30s
    return () => clearInterval(interval);
  }, [setBackendConnected]);

  return (
    <BrowserRouter>
      <div className="flex h-screen bg-[#0f0f1a] overflow-hidden">
        {/* Sidebar */}
        <Sidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />

        {/* Main content */}
        <div className="flex-1 flex flex-col overflow-hidden">
          {/* Top bar */}
          <header className="h-14 bg-[#1a1a2e] border-b border-gray-800 flex items-center justify-between px-6 shrink-0">
            <div className="flex items-center gap-3">
              <button
                onClick={toggleSidebar}
                className="text-gray-400 hover:text-white transition-colors"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                </svg>
              </button>
              <h1 className="text-lg font-semibold text-white">My CMA</h1>
            </div>

            <div className="flex items-center gap-2">
              <span className={`w-2 h-2 rounded-full ${isBackendConnected ? 'bg-green-500' : 'bg-red-500'}`} />
              <span className="text-xs text-gray-400">
                {isBackendConnected ? 'Backend conectado' : 'Backend desconectado'}
              </span>
            </div>
          </header>

          {/* Page content */}
          <main className="flex-1 overflow-y-auto p-6">
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/content" element={<div className="text-gray-400">Editor de contenido (próximamente)</div>} />
              <Route path="/ai" element={<div className="text-gray-400">Generación IA (próximamente)</div>} />
              <Route path="/analytics" element={<div className="text-gray-400">Analytics (próximamente)</div>} />
              <Route path="/settings" element={<div className="text-gray-400">Configuración (próximamente)</div>} />
            </Routes>
          </main>
        </div>
      </div>
    </BrowserRouter>
  );
}

export default App;
