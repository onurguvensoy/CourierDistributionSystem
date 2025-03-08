import axios from "axios";
import { createAsyncThunk } from "@reduxjs/toolkit";
import  { jwtDecode } from "jwt-decode";
import { message } from "antd";

interface LoginResponse {
    token: string;
}

interface DecodedToken {
    userId: string;
    iss: string;
    role: string;
}

export const loginUser = createAsyncThunk(
    "auth/loginUser",
    async (credentials: { username: string; password: string }, { rejectWithValue }) => {
        try {
            const response = await axios.post<LoginResponse>("http://localhost:8080/api/auth/login", credentials, {
                withCredentials: true,
            });

            const token = response.data.token;
            if (!token) throw new Error("No token received");

            const decoded: DecodedToken = jwtDecode(token);
            if (!decoded) throw new Error("Invalid token format");

            localStorage.setItem("token", token);

            return {
                user: {
                    id: decoded.userId,
                    name: decoded.iss,
                    role: decoded.role,
                },
                token,
            };
        } catch (error) {
            if (error instanceof Error) {
                message.error(error.message);
            } else {
                message.error('An unexpected error occurred');
            }
            throw error;
        }
    }
);
