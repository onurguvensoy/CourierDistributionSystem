package com.example.courierdistributionsystem.service;

import com.example.courierdistributionsystem.dto.SignupDto;
import com.example.courierdistributionsystem.dto.LoginDto;
import java.util.Map;

public interface IAuthService {
    Map<String, Object> login(LoginDto request);
    Map<String, String> logout(String username);
    Map<String, String> signup(SignupDto request);
} 