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

/**
 * Service responsável pela autenticação e autorização de usuários.
 * 
 * Este service gerencia:
 * - Autenticação via email/senha
 * - Geração de tokens JWT
 * - Validação de credenciais
 * - Registro de novos usuários
 * - Verificação de status ativo do usuário
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Autentica um usuário e gera token JWT.
     * 
     * Processo de autenticação:
     * 1. Busca usuário por email
     * 2. Valida senha fornecida
     * 3. Verifica se usuário está ativo
     * 4. Gera token JWT com roles
     * 5. Retorna resposta com token e dados do usuário
     * 
     * @param loginRequest dados de login (email e senha)
     * @return JwtResponseDto com token e informações do usuário
     * @throws RuntimeException se usuário não encontrado, senha inválida ou usuário inativo
     */
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
    
    /**
     * Valida se a senha fornecida corresponde ao hash armazenado.
     * 
     * NOTA: Implementação simplificada para desenvolvimento.
     * Em produção, deve utilizar BCryptPasswordEncoder para hash seguro.
     * 
     * @param rawPassword senha em texto plano
     * @param hashedPassword hash da senha armazenado no banco
     * @return true se as senhas coincidirem, false caso contrário
     */
    private boolean isPasswordValid(String rawPassword, String hashedPassword) {
        // Simplificado - em produção usar BCrypt
        return ("hashed_" + rawPassword).equals(hashedPassword);
    }
    
    /**
     * Registra um novo usuário no sistema.
     * 
     * Delega a criação para o UserService que possui toda
     * a lógica de validação e persistência de usuários.
     * 
     * @param userRegistrationDto dados do usuário a ser registrado
     * @return UserDto com dados do usuário criado
     * @throws RuntimeException se ocorrer erro na criação
     */
    public UserDto registerUser(UserRegistrationDto userRegistrationDto) {
        return userService.createUser(userRegistrationDto);
    }
} 