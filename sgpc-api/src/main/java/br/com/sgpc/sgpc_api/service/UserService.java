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

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
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
    
    private String hashPassword(String password) {
        // Simplificado - em produção usaria BCrypt
        return "hashed_" + password;
    }
    
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<UserDto> getAllActiveUsers() {
        return ((List<User>) userRepository.findAllActiveUsers()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDto);
    }
    
    public Optional<UserDto> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDto);
    }
    
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
    
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        user.setIsActive(false);
        userRepository.save(user);
    }
    
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        user.setIsActive(true);
        userRepository.save(user);
    }
    
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