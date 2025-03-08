import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { authApi } from '../../services/api/auth.api';
import type { AuthFormFields } from '../../components/common/AuthForm';
import { decodeToken, getStoredToken, isTokenExpired } from '../../utils/tokenUtils';

interface SignupData {
  username: string;
  email: string;
  password: string;
  role: string;
}

interface AuthState {
  token: string | null;
  loading: boolean;
  error: string | null;
  user: {
    username: string | null;
    role: string | null;
    userId: string | null;
  } | null;
  isAuthenticated: boolean;
}

// Helper function to extract user data from token
const extractUserFromToken = (token: string) => {
  const decoded = decodeToken(token);
  if (!decoded) return null;
  return {
    username: decoded.sub,
    role: decoded.role?.toUpperCase(),
    userId: String(decoded.userId)
  };
};


const storedToken = localStorage.getItem('token');
const initialUser = storedToken && !isTokenExpired(storedToken) 
  ? extractUserFromToken(storedToken)
  : null;

const initialState: AuthState = {
  token: storedToken,
  loading: false,
  error: null,
  user: initialUser,
  isAuthenticated: !!initialUser
};

export const login = createAsyncThunk(
  'auth/login',
  async (credentials: Pick<AuthFormFields, 'username' | 'password'>, { rejectWithValue }) => {
    try {
      const response = await authApi.login(credentials);
      const user = extractUserFromToken(response.token);
      if (!user) {
        throw new Error('Invalid token received');
      }
      return {
        token: response.token,
        user
      };
    } catch (error) {
      return rejectWithValue(error instanceof Error ? error.message : 'Login failed');
    }
  }
);

export const signup = createAsyncThunk(
  'auth/signup',
  async (userData: SignupData, { rejectWithValue }) => {
    try {
      const response = await authApi.signup(userData);
      const user = extractUserFromToken(response.token);
      if (!user) {
        throw new Error('Invalid token received');
      }
      return {
        token: response.token,
        user
      };
    } catch (error) {
      return rejectWithValue(error instanceof Error ? error.message : 'Signup failed');
    }
  }
);

export const refreshToken = createAsyncThunk(
  'auth/refreshToken',
  async (_, { rejectWithValue }) => {
    try {
      const response = await authApi.refreshToken();
      const user = extractUserFromToken(response.token);
      if (!user) {
        throw new Error('Invalid token received');
      }
      return {
        token: response.token,
        user
      };
    } catch (error) {
      return rejectWithValue(error instanceof Error ? error.message : 'Token refresh failed');
    }
  }
);

export const validateSession = createAsyncThunk(
  'auth/validateSession',
  async (_, { dispatch }) => {
    const token = getStoredToken();
    if (!token) {
      return false;
    }

    if (isTokenExpired(token)) {
      try {
        await dispatch(refreshToken()).unwrap();
        return true;
      } catch (error) {
        return false;
      }
    }

    const decoded = decodeToken(token);
    return !!decoded;
  }
);

export const logout = createAsyncThunk(
  'auth/logout',
  async (_, { rejectWithValue }) => {
    try {
      await authApi.logout();
      return null;
    } catch (error) {
      return rejectWithValue(error instanceof Error ? error.message : 'Logout failed');
    }
  }
);

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    setToken: (state, action) => {
      state.token = action.payload;
      if (action.payload) {
        const user = extractUserFromToken(action.payload);
        localStorage.setItem('token', action.payload);
        if (user) {
          state.user = user;
          state.isAuthenticated = true;
        }
      } else {
        localStorage.removeItem('token');
        state.user = null;
        state.isAuthenticated = false;
      }
    }
  },
  extraReducers: (builder) => {
    builder

      .addCase(login.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(login.fulfilled, (state, action) => {
        state.loading = false;
        state.error = null;
        state.token = action.payload.token;
        state.user = action.payload.user;
        state.isAuthenticated = true;
        localStorage.setItem('token', action.payload.token);
      })
      .addCase(login.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string || 'Login failed';
        state.token = null;
        state.user = null;
        state.isAuthenticated = false;
        localStorage.removeItem('token');
      })

      .addCase(signup.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(signup.fulfilled, (state, action) => {
        state.loading = false;
        state.error = null;
        state.token = action.payload.token;
        state.user = action.payload.user;
        state.isAuthenticated = true;
        localStorage.setItem('token', action.payload.token);
      })
      .addCase(signup.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string || 'Signup failed';
        state.token = null;
        state.user = null;
        state.isAuthenticated = false;
      })

      .addCase(refreshToken.fulfilled, (state, action) => {
        state.token = action.payload.token;
        state.user = action.payload.user;
        state.isAuthenticated = true;
        localStorage.setItem('token', action.payload.token);
      })
      .addCase(refreshToken.rejected, (state) => {
        state.token = null;
        state.user = null;
        state.isAuthenticated = false;
        localStorage.removeItem('token');
      })

      .addCase(logout.fulfilled, (state) => {
        state.token = null;
        state.user = null;
        state.isAuthenticated = false;
        state.error = null;
      })
      .addCase(logout.rejected, (state, action) => {
        state.error = action.payload as string || 'Logout failed';
      });
  },
});

export const { clearError, setToken } = authSlice.actions;
export default authSlice.reducer; 