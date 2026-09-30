package com.example.authservice.controller;

import com.example.authservice.model.LoginRequest;
import com.example.authservice.model.RefreshRequest;
import com.example.authservice.model.TokenResponse;
import com.example.authservice.service.JwtService;
import com.example.authservice.service.UserService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (!userService.authenticate(request.getUsername(), request.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciais invalidas"));
        }
        String accessToken  = jwtService.generateAccessToken(request.getUsername());
        String refreshToken = jwtService.generateRefreshToken(request.getUsername());
        return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
    }

    // POST /api/auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        try {
            if (!jwtService.isRefreshToken(request.getRefreshToken())) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token enviado não e um refresh token"));
            }
            Claims claims = jwtService.parseToken(request.getRefreshToken());
            String username = claims.getSubject();

            String newAccessToken  = jwtService.generateAccessToken(username);
            String newRefreshToken = jwtService.generateRefreshToken(username);
            return ResponseEntity.ok(new TokenResponse(newAccessToken, newRefreshToken));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token expirado ou inválido: " + e.getMessage()));
        }
    }
}
