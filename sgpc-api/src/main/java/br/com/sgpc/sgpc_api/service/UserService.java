package br.com.sgpc.sgpc_api.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sgpc.sgpc_api.dto.UserDto;
import br.com.sgpc.sgpc_api.dto.UserRegistrationDto;
import br.com.sgpc.sgpc_api.entity.Role;
import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.repository.RoleRepository;
import br.com.sgpc.sgpc_api.repository.UserRepository;

/**
 * Serviço responsável pelo gerenciamento de usuários do sistema.
 * 
 * Esta classe implementa todas as operações relacionadas aos usuários,
 * incluindo criação, atualização, ativação/desativação e consultas.
 * Gerencia também as funções (roles) dos usuários e validações de dados.
 * 
 * Principais funcionalidades:
 * - CRUD completo de usuários
 * - Gerenciamento de funções (roles)
 * - Ativação/desativação de contas
 * - Validações de email único
 * - Conversão entre entidades e DTOs
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    /**
     * Cria um novo usuário no sistema.
     * 
     * Valida se o email não está em uso, cria o usuário com senha hash,
     * define as funções especificadas ou atribui a função USER por padrão.
     * 
     * @param userRegistrationDto dados do usuário a ser criado
     * @return UserDto dados do usuário criado
     * @throws RuntimeException se o email já estiver em uso ou função não existir
     */
    public UserDto createUser(UserRegistrationDto userRegistrationDto) {
        if (userRepository.existsByEmail(userRegistrationDto.getEmail())) {
            throw new RuntimeException("Email já está em uso!");
        }
        
        User user = new User();
        user.setFullName(userRegistrationDto.getFullName());
        user.setEmail(userRegistrationDto.getEmail());
        user.setPhone(userRegistrationDto.getPhone());
        user.setPasswordHash(hashPassword(userRegistrationDto.getPassword()));
        user.setHourlyRate(userRegistrationDto.getHourlyRate());
        user.setIsActive(true);
        
        // Adicionar roles
        Set<Role> roles = new HashSet<>();
        if (userRegistrationDto.getRoleNames() != null && !userRegistrationDto.getRoleNames().isEmpty()) {
            for (String roleName : userRegistrationDto.getRoleNames()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role não encontrada: " + roleName));
                roles.add(role);
            }
        } else {
            // Se não especificar roles, adicionar USER como padrão
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Role USER não encontrada"));
            roles.add(userRole);
        }
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }
    
    /**
     * Gera hash da senha do usuário.
     * 
     * NOTA: Implementação simplificada para demonstração.
     * Em produção deve usar BCrypt ou similar.
     * 
     * @param password senha em texto plano
     * @return String hash da senha
     */
    private String hashPassword(String password) {
        // Simplificado - em produção usaria BCrypt
        return "hashed_" + password;
    }
    
    /**
     * Lista todos os usuários do sistema.
     * 
     * @return List<UserDto> lista de todos os usuários
     */
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista apenas os usuários ativos do sistema.
     * 
     * @return List<UserDto> lista de usuários ativos
     */
    public List<UserDto> getAllActiveUsers() {
        return ((List<User>) userRepository.findAllActiveUsers()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca um usuário pelo ID.
     * 
     * @param id ID do usuário
     * @return Optional<UserDto> usuário encontrado ou empty
     */
    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDto);
    }
    
    /**
     * Busca um usuário pelo email.
     * 
     * @param email email do usuário
     * @return Optional<UserDto> usuário encontrado ou empty
     */
    public Optional<UserDto> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDto);
    }
    
    /**
     * Atualiza dados de um usuário existente.
     * 
     * Permite atualizar nome, telefone, valor da hora e opcionalmente
     * a senha. O email não pode ser alterado após criação.
     * 
     * @param id ID do usuário a ser atualizado
     * @param userRegistrationDto novos dados do usuário
     * @return UserDto usuário atualizado
     * @throws RuntimeException se o usuário não for encontrado
     */
    public UserDto updateUser(Long id, UserRegistrationDto userRegistrationDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        user.setFullName(userRegistrationDto.getFullName());
        user.setPhone(userRegistrationDto.getPhone());
        user.setHourlyRate(userRegistrationDto.getHourlyRate());
        
        if (userRegistrationDto.getPassword() != null && !userRegistrationDto.getPassword().isEmpty()) {
            user.setPasswordHash(hashPassword(userRegistrationDto.getPassword()));
        }
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }
    
    /**
     * Desativa uma conta de usuário.
     * 
     * O usuário desativado não pode fazer login mas seus dados
     * são preservados no sistema para histórico.
     * 
     * @param id ID do usuário a ser desativado
     * @throws RuntimeException se o usuário não for encontrado
     */
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        user.setIsActive(false);
        userRepository.save(user);
    }
    
    /**
     * Ativa uma conta de usuário.
     * 
     * Permite que um usuário previamente desativado
     * volte a acessar o sistema.
     * 
     * @param id ID do usuário a ser ativado
     * @throws RuntimeException se o usuário não for encontrado
     */
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        user.setIsActive(true);
        userRepository.save(user);
    }
    
    /**
     * Converte entidade User para UserDto.
     * 
     * Inclui todas as informações do usuário exceto dados sensíveis
     * como hash da senha. Também converte as funções em strings.
     * 
     * @param user entidade do usuário
     * @return UserDto dados do usuário para API
     */
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setHourlyRate(user.getHourlyRate());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoles(roleNames);
        
        return dto;
    }
} 