import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { loginUser } from "./authActions";
import { jwtDecode } from "jwt-decode";

interface UserState {
    id: string | null;
    name: string | null;
    role: string | null;
}

interface AuthState {
    user: UserState;
    token: string | null;
    isAuthenticated: boolean;
    loading: boolean;
    error: string | null;
}

const token = localStorage.getItem("token");

const getUserFromToken = (token: string | null): UserState | null => {
    if (!token) return null;
    try {
        const decoded: any = jwtDecode(token);
        return {
            id: decoded.userId,
            name: decoded.iss,
            role: decoded.role,
        };
    } catch (error) {
        return null;
    }
};

const initialState: AuthState = {
    user: getUserFromToken(token),
    token,
    isAuthenticated: !!token,
    loading: false,
    error: null,
};

const authSlice = createSlice({
    name: "auth",
    initialState,
    reducers: {
        logout: (state) => {
            state.token = null;
            state.user = null;
            state.isAuthenticated = false;
            localStorage.removeItem("token");
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(loginUser.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(loginUser.fulfilled, (state, action: PayloadAction<{ user: UserState; token: string }>) => {
                state.loading = false;
                state.token = action.payload.token;
                state.user = action.payload.user;
                state.isAuthenticated = true;
            })
            .addCase(loginUser.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            });
    },
});

export const { logout } = authSlice.actions;
export default authSlice.reducer;
