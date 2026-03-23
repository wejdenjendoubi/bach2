package com.example.CWMS.iservice;

import com.example.CWMS.payload.LoginRequest;
import com.example.CWMS.payload.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);
    void logout(HttpServletRequest httpRequest);
}