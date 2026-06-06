/** Electron API exposed via contextBridge */
export interface ElectronAPI {
  openFileDialog: (options: Electron.OpenDialogOptions) => Promise<Electron.OpenDialogReturnValue>;
  saveFileDialog: (options: Electron.SaveDialogOptions) => Promise<Electron.SaveDialogReturnValue>;
  getBackendUrl: () => Promise<string>;
  getVersion: () => Promise<string>;
  getPlatform: () => Promise<string>;
}

declare global {
  interface Window {
    electronAPI?: ElectronAPI;
  }
}

/** Social media account */
export interface SocialAccount {
  id: string;
  provider: 'facebook' | 'instagram' | 'linkedin';
  pageId: string;
  pageName: string;
  connectedAt: string;
  isExpired: boolean;
}

/** Post status */
export type PostStatus = 'DRAFT' | 'SCHEDULED' | 'PUBLISHED' | 'FAILED';

/** Social media post */
export interface Post {
  id: string;
  contentText: string;
  mediaPaths: string[];
  hashtags: string[];
  provider: string;
  scheduledAt?: string;
  publishedAt?: string;
  status: PostStatus;
  errorMessage?: string;
  createdAt: string;
}

/** AI generation request */
export interface AIGenerationRequest {
  prompt: string;
  tone: 'professional' | 'casual' | 'humorous' | 'inspirational';
  maxTokens: number;
}

/** AI generation result */
export interface AIGeneration {
  id: string;
  type: 'COPY' | 'IMAGE';
  originalPrompt: string;
  generatedText?: string;
  generatedImagePath?: string;
  modelUsed: string;
  createdAt: string;
}

/** Analytics data point */
export interface Analytics {
  postId: string;
  provider: string;
  reach: number;
  likes: number;
  comments: number;
  shares: number;
  impressions: number;
  snapshotDate: string;
}

/** Health check response */
export interface HealthResponse {
  status: string;
  app: string;
  version: string;
  timestamp: string;
}
