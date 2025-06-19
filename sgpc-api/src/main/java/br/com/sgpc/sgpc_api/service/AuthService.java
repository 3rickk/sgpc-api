package br.com.sgpc.sgpc_api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sgpc.sgpc_api.dto.JwtResponseDto;
import br.com.sgpc.sgpc_api.dto.LoginRequestDto;
import br.com.sgpc.sgpc_api.dto.UserDto;
import br.com.sgpc.sgpc_api.dto.UserRegistrationDto;
import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.exception.InvalidCredentialsException;
import br.com.sgpc.sgpc_api.exception.UserInactiveException;
import br.com.sgpc.sgpc_api.repository.UserRepository;
import br.com.sgpc.sgpc_api.security.JwtUtil;
import br.com.sgpc.sgpc_api.security.UserDetailsImpl;

/**
 * Service responsável pela autenticação e autorização de usuários.
 */
@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Autentica um usuário e gera token JWT.
     */
    @Transactional(readOnly = true)
    public JwtResponseDto authenticateUser(LoginRequestDto loginRequest) {
        // Busca usuário por email
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);
        
        // Se usuário não encontrado ou senha inválida, lança mesma exceção
        if (user == null || !isPasswordValid(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciais inválidas");
        }
        
        if (!user.isActive()) {
            throw new UserInactiveException("Conta desativada. Entre em contato com o administrador");
        }
        
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        String jwt = jwtUtil.generateToken(userDetails);
        
        // Busca a role do usuário usando query JPA otimizada
        String role = findUserRole(loginRequest.getEmail());
        
        return new JwtResponseDto(jwt, user.getId(), user.getEmail(), user.getFullName(), role);
    }
    
    /**
     * Busca a role de um usuário usando query JPA.
     */
    private String findUserRole(String email) {
        List<String> roleNames = userRepository.findRoleNamesByEmail(email);
        if (!roleNames.isEmpty()) {
            return roleNames.get(0); // Retorna a primeira role
        }
        
        // Fallback: role padrão
        return "USER";
    }
    
    /**
     * Valida se a senha fornecida corresponde ao hash armazenado.
     */
    private boolean isPasswordValid(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
    
    /**
     * Registra um novo usuário no sistema como ADMIN.
     * 
     * Todo usuário que se registra através do endpoint público
     * /api/auth/register será criado com role ADMIN automaticamente.
     */
    public UserDto registerUser(UserRegistrationDto userRegistrationDto) {
        // Força a role para ADMIN em registros públicos
        userRegistrationDto.setRoleName("ADMIN");
        return userService.createUser(userRegistrationDto);
    }
} 