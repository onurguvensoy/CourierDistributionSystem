import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/auth.slice';
import { packagesApi } from './api/packages.api';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    [packagesApi.reducerPath]: packagesApi.reducer,
  },
  devTools: process.env.NODE_ENV !== 'production', // Enable Redux DevTools
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(packagesApi.middleware),
});


export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch; 