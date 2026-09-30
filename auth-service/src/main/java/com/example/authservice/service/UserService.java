package com.example.authservice.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final Map<String, String> users = new HashMap<>();

    public UserService() {
        // Usuários em memória para demonstração
        users.put("admin", encoder.encode("admin123"));
        users.put("user",  encoder.encode("user123"));
    }

    public boolean authenticate(String username, String password) {
        String stored = users.get(username);
        return stored != null && encoder.matches(password, stored);
    }
}
