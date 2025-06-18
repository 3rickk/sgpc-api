package br.com.sgpc.sgpc_api.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.sgpc.sgpc_api.dto.JwtResponseDto;
import br.com.sgpc.sgpc_api.dto.LoginRequestDto;
import br.com.sgpc.sgpc_api.dto.UserDto;
import br.com.sgpc.sgpc_api.dto.UserRegistrationDto;
import br.com.sgpc.sgpc_api.entity.Role;
import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.exception.InvalidCredentialsException;
import br.com.sgpc.sgpc_api.exception.UserInactiveException;
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
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
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
     * @throws InvalidCredentialsException se credenciais inválidas (email não encontrado ou senha incorreta)
     * @throws UserInactiveException se usuário inativo
     */
    public JwtResponseDto authenticateUser(LoginRequestDto loginRequest) {
        User user = userRepository.findByEmailWithRoles(loginRequest.getEmail())
                .orElse(null);
        
        // Se usuário não encontrado ou senha inválida, lança mesma exceção
        if (user == null || !isPasswordValid(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenciais inválidas");
        }
        
        if (!user.getIsActive()) {
            throw new UserInactiveException("Conta desativada. Entre em contato com o administrador");
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
     * Utiliza BCryptPasswordEncoder para verificar se a senha em texto plano
     * corresponde ao hash BCrypt armazenado no banco de dados.
     * 
     * @param rawPassword senha em texto plano
     * @param hashedPassword hash da senha armazenado no banco
     * @return true se as senhas coincidirem, false caso contrário
     */
    private boolean isPasswordValid(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
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