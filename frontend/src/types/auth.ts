export interface User {
  username: string;
  role: string;
  userId: string;
  lastLogin: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  role: string;
  userId: string;
}

export interface LoginFormData {
  username: string;
  password: string;
}

export interface SignupFormData {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  role: string;
  phoneNumber: string;
  vehicleType?: 'MOTORCYCLE' | 'CAR' | 'VAN';
}

export interface ApiError {
  response?: {
    data?: {
      message?: string;
      error?: string;
      errors?: Record<string, string>;
    };
    status?: number;
  };
} 