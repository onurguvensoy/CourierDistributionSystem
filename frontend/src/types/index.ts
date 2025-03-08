// Global type definitions
export interface User {
  username: string;
  role: string;
  userId: string;
}

interface AuthResponse {
  token: string;
  user: User;
}

export interface ApiError {
  message: string;
  status: number;
}

export type { User, AuthResponse, ApiError }; 