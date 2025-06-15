package br.com.sgpc.sgpc_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sgpc.sgpc_api.dto.JwtResponseDto;
import br.com.sgpc.sgpc_api.dto.LoginRequestDto;
import br.com.sgpc.sgpc_api.dto.PasswordResetDto;
import br.com.sgpc.sgpc_api.dto.PasswordResetRequestDto;
import br.com.sgpc.sgpc_api.dto.UserDto;
import br.com.sgpc.sgpc_api.dto.UserRegistrationDto;
import br.com.sgpc.sgpc_api.service.AuthService;
import br.com.sgpc.sgpc_api.service.PasswordResetService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequest) {
        try {
            JwtResponseDto response = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer login: " + e.getMessage());
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserRegistrationDto signUpRequest) {
        try {
            UserDto user = authService.registerUser(signUpRequest);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao registrar usuário: " + e.getMessage());
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody PasswordResetRequestDto request) {
        try {
            String token = passwordResetService.generatePasswordResetToken(request);
            return ResponseEntity.ok("Token de recuperação gerado: " + token + " (Em produção seria enviado por email)");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token de recuperação: " + e.getMessage());
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetDto resetDto) {
        try {
            passwordResetService.resetPassword(resetDto);
            return ResponseEntity.ok("Senha alterada com sucesso!");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao alterar senha: " + e.getMessage());
        }
    }
} 