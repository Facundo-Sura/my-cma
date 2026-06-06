import { create } from 'zustand';
import type { SocialAccount, Post, AIGeneration } from '../types';

interface AppState {
  // Connection
  isBackendConnected: boolean;
  setBackendConnected: (connected: boolean) => void;

  // Social accounts
  accounts: SocialAccount[];
  setAccounts: (accounts: SocialAccount[]) => void;
  addAccount: (account: SocialAccount) => void;
  removeAccount: (id: string) => void;

  // Posts
  posts: Post[];
  setPosts: (posts: Post[]) => void;
  addPost: (post: Post) => void;
  updatePost: (id: string, updates: Partial<Post>) => void;

  // AI history
  generations: AIGeneration[];
  addGeneration: (gen: AIGeneration) => void;

  // UI state
  sidebarOpen: boolean;
  toggleSidebar: () => void;
}

export const useAppStore = create<AppState>((set) => ({
  // Connection
  isBackendConnected: false,
  setBackendConnected: (connected) => set({ isBackendConnected: connected }),

  // Social accounts
  accounts: [],
  setAccounts: (accounts) => set({ accounts }),
  addAccount: (account) => set((state) => ({ accounts: [...state.accounts, account] })),
  removeAccount: (id) => set((state) => ({
    accounts: state.accounts.filter((a) => a.id !== id),
  })),

  // Posts
  posts: [],
  setPosts: (posts) => set({ posts }),
  addPost: (post) => set((state) => ({ posts: [post, ...state.posts] })),
  updatePost: (id, updates) => set((state) => ({
    posts: state.posts.map((p) => (p.id === id ? { ...p, ...updates } : p)),
  })),

  // AI history
  generations: [],
  addGeneration: (gen) => set((state) => ({ generations: [gen, ...state.generations] })),

  // UI state
  sidebarOpen: true,
  toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),
}));
