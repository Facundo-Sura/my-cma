import axios, { AxiosInstance } from 'axios';
import type { HealthResponse, SocialAccount, Post, AIGeneration, AIGenerationRequest, Analytics } from '../types';

/**
 * API client for the My CMA backend (Spring Boot on localhost:8080).
 */
class ApiService {
  private client: AxiosInstance;
  private backendUrl = 'http://localhost:8080';

  constructor() {
    this.client = axios.create({
      baseURL: this.backendUrl,
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Initialize backend URL from Electron if available
    if (window.electronAPI) {
      window.electronAPI.getBackendUrl().then(url => {
        this.backendUrl = url;
        this.client.defaults.baseURL = url;
      });
    }

    // Request interceptor for auth token
    this.client.interceptors.request.use(config => {
      const token = localStorage.getItem('mycma_token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });
  }

  // Health
  async checkHealth(): Promise<HealthResponse> {
    const { data } = await this.client.get('/api/health');
    return data;
  }

  // Social Accounts
  async getAccounts(): Promise<SocialAccount[]> {
    const { data } = await this.client.get('/api/accounts');
    return data;
  }

  async connectFacebook(): Promise<string> {
    const { data } = await this.client.post('/api/auth/facebook/authorize');
    return data.authorizationUrl;
  }

  // Posts
  async getPosts(): Promise<Post[]> {
    const { data } = await this.client.get('/api/posts');
    return data;
  }

  async createPost(post: Partial<Post>): Promise<Post> {
    const { data } = await this.client.post('/api/posts', post);
    return data;
  }

  async publishPost(postId: string): Promise<Post> {
    const { data } = await this.client.post(`/api/posts/${postId}/publish`);
    return data;
  }

  async deletePost(postId: string): Promise<void> {
    await this.client.delete(`/api/posts/${postId}`);
  }

  // AI Content Generation
  async generateCopy(request: AIGenerationRequest): Promise<AIGeneration> {
    const { data } = await this.client.post('/api/content/generate-copy', request);
    return data;
  }

  async generateImage(prompt: string): Promise<AIGeneration> {
    const { data } = await this.client.post('/api/content/generate-image', { prompt });
    return data;
  }

  // Analytics
  async getAnalytics(postId: string): Promise<Analytics> {
    const { data } = await this.client.get(`/api/analytics/${postId}`);
    return data;
  }
}

export const api = new ApiService();
