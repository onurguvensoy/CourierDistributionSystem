export interface CourierStats {
  totalDeliveries: number;
  activeDeliveries: number;
  completedDeliveries: number;
  rating: number;
}

export interface Package {
  id: string;
  customerName: string;
  pickupAddress: string;
  deliveryAddress: string;
  status: 'PENDING' | 'PICKED_UP' | 'IN_TRANSIT' | 'DELIVERED' | 'FAILED';
}

export interface AdminStats {
  totalUsers: number;
  totalPackages: number;
  activeDeliveries: number;
  revenue: number;
}

export interface User {
  username: string;
  role: 'ADMIN' | 'COURIER' | 'CUSTOMER';
  email: string;
  status: string;
  available?: boolean;
  createdAt: string;
}

export interface DeliveryPackage {
  package_id: string;
  customer?: {
    username: string;
  };
  courier?: {
    username: string;
  };
  status: string;
  pickupAddress: string;
  deliveryAddress: string;
  createdAt: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: {
    username: string;
    role: 'ADMIN' | 'COURIER' | 'CUSTOMER';
    email: string;
  }
} 