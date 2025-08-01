package br.com.sgpc.sgpc_api.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sgpc.sgpc_api.dto.UserCreateDto;
import br.com.sgpc.sgpc_api.dto.UserDto;
import br.com.sgpc.sgpc_api.dto.UserRegistrationDto;
import br.com.sgpc.sgpc_api.entity.Role;
import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.exception.EmailAlreadyExistsException;
import br.com.sgpc.sgpc_api.exception.UserNotFoundException;
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
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Cria um novo usuário no sistema.
     * 
     * Valida se o email não está em uso, cria o usuário com senha hash,
     * define as funções especificadas ou atribui a função USER por padrão.
     * 
     * @param userRegistrationDto dados do usuário a ser criado
     * @return UserDto dados do usuário criado
     * @throws EmailAlreadyExistsException se o email já estiver em uso
     * @throws RuntimeException se função não existir
     */
    public UserDto createUser(UserRegistrationDto userRegistrationDto) {
        if (userRepository.existsByEmail(userRegistrationDto.getEmail())) {
            throw new EmailAlreadyExistsException("Este email já está cadastrado no sistema");
        }
        
        User user = new User();
        user.setFullName(userRegistrationDto.getFullName());
        user.setEmail(userRegistrationDto.getEmail());
        user.setPhone(userRegistrationDto.getPhone());
        user.setPasswordHash(hashPassword(userRegistrationDto.getPassword()));
        user.setHourlyRate(userRegistrationDto.getHourlyRate());
        user.setIsActive(true);
        
        // Adicionar role única
        Set<Role> roles = new HashSet<>();
        String roleName = userRegistrationDto.getRoleName();
        if (roleName != null && !roleName.isEmpty()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Perfil não encontrado: " + roleName));
                roles.add(role);
        } else {
            // Se não especificar role, adicionar USER como padrão
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Perfil padrão não encontrado. Entre em contato com o administrador"));
            roles.add(userRole);
        }
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }
    
    /**
     * Gera hash da senha do usuário usando BCrypt.
     * 
     * Utiliza BCryptPasswordEncoder para criar hash seguro da senha.
     * 
     * @param password senha em texto plano
     * @return String hash da senha usando BCrypt
     */
    private String hashPassword(String password) {
        return passwordEncoder.encode(password);
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
     * Os roles são preservados se não forem enviados novos.
     * 
     * @param id ID do usuário a ser atualizado
     * @param userRegistrationDto novos dados do usuário
     * @return UserDto usuário atualizado
     * @throws UserNotFoundException se o usuário não for encontrado
     */
    public UserDto updateUser(Long id, UserRegistrationDto userRegistrationDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário com ID " + id + " não foi encontrado"));
        
        user.setFullName(userRegistrationDto.getFullName());
        user.setPhone(userRegistrationDto.getPhone());
        user.setHourlyRate(userRegistrationDto.getHourlyRate());
        
        if (userRegistrationDto.getPassword() != null && !userRegistrationDto.getPassword().isEmpty()) {
            user.setPasswordHash(hashPassword(userRegistrationDto.getPassword()));
        }
        
        // Atualizar role se fornecida, caso contrário preservar a existente
        if (userRegistrationDto.getRoleName() != null && !userRegistrationDto.getRoleName().isEmpty()) {
            // Remover todos os relacionamentos user-role existentes via query nativa
            userRepository.removeAllUserRoles(user.getId());
            
            // Limpar roles do objeto para manter sincronização
            user.getRoles().clear();
            
            // Adicionar nova role
            Set<Role> newRoles = new HashSet<>();
            String roleName = userRegistrationDto.getRoleName();
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Perfil não encontrado: " + roleName));
                newRoles.add(role);
            user.setRoles(newRoles);
        }
        // Se roleName for null ou vazio, a role existente é preservada automaticamente
        
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
     * @throws UserNotFoundException se o usuário não for encontrado
     */
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário com ID " + id + " não foi encontrado"));
        
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
     * @throws UserNotFoundException se o usuário não for encontrado
     */
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário com ID " + id + " não foi encontrado"));
        
        user.setIsActive(true);
        userRepository.save(user);
    }
    
    /**
     * Cria um novo usuário por administrador.
     * 
     * Permite que administradores criem usuários com roles USER ou MANAGER.
     * Usado pelos endpoints administrativos.
     * 
     * @param userCreateDto dados do usuário a ser criado
     * @return UserDto dados do usuário criado
     * @throws EmailAlreadyExistsException se o email já estiver em uso
     * @throws RuntimeException se função não existir
     */
    public UserDto createUserByAdmin(UserCreateDto userCreateDto) {
        if (userRepository.existsByEmail(userCreateDto.getEmail())) {
            throw new EmailAlreadyExistsException("Este email já está cadastrado no sistema");
        }
        
        User user = new User();
        user.setFullName(userCreateDto.getFullName());
        user.setEmail(userCreateDto.getEmail());
        user.setPhone(userCreateDto.getPhone());
        user.setPasswordHash(hashPassword(userCreateDto.getPassword()));
        user.setHourlyRate(userCreateDto.getHourlyRate());
        user.setIsActive(true);
        
        // Adicionar role específica
        Set<Role> roles = new HashSet<>();
        Role role = roleRepository.findByName(userCreateDto.getRoleName())
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado: " + userCreateDto.getRoleName()));
        roles.add(role);
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
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
        
        // Pegar apenas a primeira role (sistema agora usa role única)
        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse(null);
        dto.setRole(roleName);
        
        return dto;
    }
} 