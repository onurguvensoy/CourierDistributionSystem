import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import { getStoredToken } from '../../utils/tokenUtils';

export interface Package {
  id: number;
  trackingNumber: string;
  status: 'PENDING' | 'IN_TRANSIT' | 'DELIVERED' | 'CANCELLED';
  pickupAddress: string;
  deliveryAddress: string;
  createdAt: string;
  completedAt?: string;
  weight: number;
  description?: string;
  customerUsername?: string;
  courierUsername?: string;
  courierLocation?: {
    latitude: number;
    longitude: number;
  };
}

export interface LocationUpdate {
  latitude: number;
  longitude: number;
}

export interface UpdateAvailabilityRequest {
  username: string;
  isAvailable: boolean;
}

export interface AssignDeliveryRequest {
  username: string;
  deliveryId: number;
}

export interface UpdatePackageStatusRequest {
  packageId: number;
  status: string;
}

export interface CreatePackageRequest {
  pickupAddress: string;
  deliveryAddress: string;
  weight: number;
  description?: string;
}

export const packagesApi = createApi({
  reducerPath: 'packagesApi',
  baseQuery: fetchBaseQuery({
    baseUrl: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
    prepareHeaders: (headers) => {
      const token = getStoredToken();
      if (token) {
        headers.set('Authorization', `Bearer ${token}`);
      }
      return headers;
    },
  }),
  tagTypes: ['Package', 'DeliveryHistory'],
  endpoints: (builder) => ({
    // Customer endpoints
    getCustomerPackages: builder.query<Package[], void>({
      query: () => '/packages/customer',
      providesTags: ['Package'],
    }),

    getDeliveryHistory: builder.query<Package[], void>({
      query: () => '/customer/delivery-history',
      providesTags: ['DeliveryHistory'],
    }),

    createPackage: builder.mutation<Package, CreatePackageRequest>({
      query: (newPackage) => ({
        url: '/packages',
        method: 'POST',
        body: newPackage,
      }),
      invalidatesTags: ['Package'],
    }),

    cancelPackage: builder.mutation<void, string>({
      query: (packageId) => ({
        url: `/packages/${packageId}/cancel`,
        method: 'PUT',
      }),
      invalidatesTags: ['Package', 'DeliveryHistory'],
    }),


    getAvailablePackages: builder.query<Package[], void>({
      query: () => ({
        url: '/packages/available',
        method: 'GET',
        params: { username: getStoredToken() ? JSON.parse(atob(getStoredToken()!.split('.')[1])).sub : undefined }
      }),
      providesTags: ['Package'],
    }),
    
    getCourierActiveDeliveries: builder.query<Package[], void>({
      query: () => ({
        url: '/packages/courier/active',
        method: 'GET',
        params: { username: getStoredToken() ? JSON.parse(atob(getStoredToken()!.split('.')[1])).sub : undefined }
      }),
      providesTags: ['Package'],
    }),
    
    getCourierDeliveryHistory: builder.query<Package[], string>({
      query: (username) => `delivery-history/courier`,
      providesTags: ['Package'],
    }),

    assignDelivery: builder.mutation<void, { username: string; deliveryId: number }>({
      query: ({ username, deliveryId }) => ({
        url: `/couriers/deliveries/${deliveryId}/assign`,
        method: 'POST',
      }),
      invalidatesTags: ['Package'],
    }),

    unassignDelivery: builder.mutation<void, { username: string; deliveryId: number }>({
      query: ({ username, deliveryId }) => ({
        url: `/couriers/deliveries/${deliveryId}/unassign`,
        method: 'POST',
      }),
      invalidatesTags: ['Package'],
    }),

    updatePackageStatus: builder.mutation<void, { packageId: number; status: string }>({
      query: ({ packageId, status }) => ({
        url: `/packages/${packageId}/status`,
        method: 'PUT',
        body: { status },
      }),
      invalidatesTags: ['Package', 'DeliveryHistory'],
    }),
    
    updateCourierLocation: builder.mutation<void, { username: string; location: LocationUpdate }>({
      query: ({ username, location }) => ({
        url: `/couriers/${username}/location`,
        method: 'PUT',
        body: location,
      }),
    }),
    
    updateCourierAvailability: builder.mutation<void, { username: string; isAvailable: boolean }>({
      query: ({ username, isAvailable }) => ({
        url: `/couriers/${username}/availability`,
        method: 'PUT',
        params: { available: isAvailable },
      }),
    }),
    

    trackPackage: builder.query<Package, string>({
      query: (trackingNumber) => `/packages/track/${trackingNumber}`,
      providesTags: (_result, _error, trackingNumber) => [{ type: 'Package', id: trackingNumber }],
    }),
  }),
});

export const {

  useGetCustomerPackagesQuery,
  useGetDeliveryHistoryQuery,
  useCreatePackageMutation,
  useCancelPackageMutation,
  

  useGetAvailablePackagesQuery,
  useGetCourierActiveDeliveriesQuery,
  useGetCourierDeliveryHistoryQuery,
  useAssignDeliveryMutation,
  useUnassignDeliveryMutation,
  useUpdatePackageStatusMutation,
  useUpdateCourierLocationMutation,
  useUpdateCourierAvailabilityMutation,
  

  useTrackPackageQuery,
} = packagesApi; 