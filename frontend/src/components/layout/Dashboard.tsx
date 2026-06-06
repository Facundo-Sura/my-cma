import { useEffect, useState } from 'react';
import { useAppStore } from '../../store';
import { api } from '../../services/api';
import type { HealthResponse } from '../../types';

export default function Dashboard() {
  const { accounts, posts, generations, isBackendConnected } = useAppStore();
  const [health, setHealth] = useState<HealthResponse | null>(null);

  useEffect(() => {
    api.checkHealth().then(setHealth).catch(() => setHealth(null));
  }, [isBackendConnected]);

  const stats = [
    {
      label: 'Cuentas conectadas',
      value: accounts.length.toString(),
      sub: accounts.length === 0 ? 'Sin conexiones' : 'Active',
      color: 'text-[#960018]',
    },
    {
      label: 'Posts publicados',
      value: posts.filter((p) => p.status === 'PUBLISHED').length.toString(),
      sub: `${posts.filter((p) => p.status === 'DRAFT').length} borradores`,
      color: 'text-[#003366]',
    },
    {
      label: 'Generaciones IA',
      value: generations.length.toString(),
      sub: 'Contenido creado',
      color: 'text-[#B0B0B0]',
    },
    {
      label: 'Backend',
      value: isBackendConnected ? 'Online' : 'Offline',
      sub: health?.version ?? '—',
      color: isBackendConnected ? 'text-green-500' : 'text-red-500',
    },
  ];

  return (
    <div>
      <h2 className="text-2xl font-bold text-white mb-6">Dashboard</h2>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {stats.map((stat) => (
          <div key={stat.label} className="bg-[#1a1a2e] rounded-xl p-5 border border-gray-800">
            <p className="text-sm text-gray-400 mb-1">{stat.label}</p>
            <p className={`text-3xl font-bold ${stat.color}`}>{stat.value}</p>
            <p className="text-xs text-gray-500 mt-1">{stat.sub}</p>
          </div>
        ))}
      </div>

      {/* Quick actions */}
      <div className="bg-[#1a1a2e] rounded-xl p-5 border border-gray-800">
        <h3 className="text-lg font-semibold text-white mb-4">Acciones rápidas</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
          <button className="flex items-center gap-3 p-4 rounded-lg bg-[#960018]/10 border border-[#960018]/20 hover:bg-[#960018]/20 transition-colors text-left">
            <div className="w-10 h-10 rounded-full bg-[#960018]/20 flex items-center justify-center">
              <svg className="w-5 h-5 text-[#960018]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
            </div>
            <div>
              <p className="text-sm font-medium text-white">Nuevo post</p>
              <p className="text-xs text-gray-400">Crear contenido</p>
            </div>
          </button>

          <button className="flex items-center gap-3 p-4 rounded-lg bg-[#003366]/10 border border-[#003366]/20 hover:bg-[#003366]/20 transition-colors text-left">
            <div className="w-10 h-10 rounded-full bg-[#003366]/20 flex items-center justify-center">
              <svg className="w-5 h-5 text-[#003366]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <div>
              <p className="text-sm font-medium text-white">Generar con IA</p>
              <p className="text-xs text-gray-400">Texto o imágenes</p>
            </div>
          </button>

          <button className="flex items-center gap-3 p-4 rounded-lg bg-gray-800/50 border border-gray-700 hover:bg-gray-700/50 transition-colors text-left">
            <div className="w-10 h-10 rounded-full bg-gray-700 flex items-center justify-center">
              <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
              </svg>
            </div>
            <div>
              <p className="text-sm font-medium text-white">Conectar red</p>
              <p className="text-xs text-gray-400">Facebook, LinkedIn</p>
            </div>
          </button>
        </div>
      </div>
    </div>
  );
}
