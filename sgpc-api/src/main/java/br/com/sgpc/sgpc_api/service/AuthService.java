package br.com.sgpc.sgpc_api.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.sgpc.sgpc_api.dto.JwtResponseDto;
import br.com.sgpc.sgpc_api.dto.LoginRequestDto;
import br.com.sgpc.sgpc_api.dto.UserDto;
import br.com.sgpc.sgpc_api.dto.UserRegistrationDto;
import br.com.sgpc.sgpc_api.entity.Role;
import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.repository.UserRepository;
import br.com.sgpc.sgpc_api.security.JwtUtil;
import br.com.sgpc.sgpc_api.security.UserDetailsImpl;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public JwtResponseDto authenticateUser(LoginRequestDto loginRequest) {
        User user = userRepository.findByEmailWithRoles(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Verificar senha (simplificado)
        if (!isPasswordValid(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Senha inválida");
        }
        
        if (!user.getIsActive()) {
            throw new RuntimeException("Usuário inativo");
        }
        
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        String jwt = jwtUtil.generateToken(userDetails);
        
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        
        return new JwtResponseDto(jwt, user.getId(), user.getEmail(), user.getFullName(), roles);
    }
    
    private boolean isPasswordValid(String rawPassword, String hashedPassword) {
        // Simplificado - em produção usar BCrypt
        return ("hashed_" + rawPassword).equals(hashedPassword);
    }
    
    public UserDto registerUser(UserRegistrationDto userRegistrationDto) {
        return userService.createUser(userRegistrationDto);
    }
} 