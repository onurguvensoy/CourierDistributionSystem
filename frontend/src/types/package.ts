export interface Package {
  id: string;
  trackingNumber: string;
  pickupAddress: string;
  deliveryAddress: string;
  weight: number;
  description: string;
  fragile: boolean;
  priority: 'NORMAL' | 'EXPRESS' | 'URGENT';
  status: 'PENDING' | 'IN_TRANSIT' | 'DELIVERED' | 'CANCELLED';
  courierUsername?: string;
  createdAt: string;
  deliveryDate?: string;
  specialInstructions?: string;
}

export interface PackageFormData {
  pickupAddress: string;
  deliveryAddress: string;
  weight: string;
  description: string;
  fragile: boolean;
  priority: 'NORMAL' | 'EXPRESS' | 'URGENT';
}

export interface DashboardStats {
  totalPackages: number;
  activeDeliveries: number;
  completedDeliveries: number;
  totalSpent: number;
} 