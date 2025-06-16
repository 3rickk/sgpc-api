package br.com.sgpc.sgpc_api.security;

import java.util.Set;
import java.util.stream.Collectors;

import br.com.sgpc.sgpc_api.entity.User;

/**
 * Implementação personalizada dos detalhes do usuário para Spring Security.
 * 
 * Esta classe adapta a entidade User do sistema para o formato esperado
 * pelo Spring Security, implementando a interface UserDetails de forma
 * customizada para as necessidades do sistema SGPC.
 * 
 * Funcionalidades principais:
 * - Integração entre entidade User e Spring Security
 * - Conversão de roles para authorities do Spring Security
 * - Controle de status de conta (ativo/inativo)
 * - Fornecimento de credenciais para autenticação
 * - Suporte a validações de conta e credenciais
 * 
 * Características de segurança:
 * - Roles são automaticamente prefixadas com "ROLE_"
 * - Todas as validações de conta retornam true por padrão
 * - Status ativo/inativo controlado via isEnabled()
 * - Email usado como username para login
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
public class UserDetailsImpl {
    
    /**
     * ID único do usuário.
     */
    private Long id;
    
    /**
     * Email do usuário (usado como username).
     */
    private String email;
    
    /**
     * Hash da senha para autenticação.
     */
    private String password;
    
    /**
     * Lista de authorities/roles do usuário.
     */
    private Set<String> authorities;
    
    /**
     * Status ativo/inativo do usuário.
     */
    private boolean isActive;
    
    /**
     * Construtor completo da classe.
     * 
     * @param id ID único do usuário
     * @param email email do usuário (usado como username)
     * @param password hash da senha para autenticação
     * @param authorities conjunto de authorities/roles do usuário
     * @param isActive status ativo/inativo do usuário
     */
    public UserDetailsImpl(Long id, String email, String password, 
                          Set<String> authorities, boolean isActive) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.isActive = isActive;
    }
    
    /**
     * Método factory para criar UserDetailsImpl a partir de entidade User.
     * 
     * Converte uma entidade User do sistema para UserDetailsImpl,
     * transformando roles em authorities com prefixo "ROLE_" conforme
     * padrão do Spring Security.
     * 
     * @param user entidade User do sistema SGPC
     * @return UserDetailsImpl pronto para uso no Spring Security
     */
    public static UserDetailsImpl build(User user) {
        Set<String> authorities = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .collect(Collectors.toSet());
        
        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                authorities,
                user.getIsActive()
        );
    }
    
    /**
     * Retorna as authorities do usuário.
     * 
     * @return Set<String> conjunto de authorities/roles do usuário
     */
    public Set<String> getAuthorities() {
        return authorities;
    }
    
    /**
     * Retorna o hash da senha para autenticação.
     * 
     * @return String hash da senha do usuário
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Retorna o username (email) do usuário.
     * 
     * No sistema SGPC, o email é usado como identificador único
     * para login em vez de um username tradicional.
     * 
     * @return String email do usuário usado como username
     */
    public String getUsername() {
        return email;
    }
    
    /**
     * Indica se a conta do usuário não está expirada.
     * 
     * Por padrão, contas não expiram no sistema SGPC.
     * 
     * @return boolean sempre true (contas não expiram)
     */
    public boolean isAccountNonExpired() {
        return true;
    }
    
    /**
     * Indica se a conta do usuário não está bloqueada.
     * 
     * Por padrão, contas não são bloqueadas automaticamente
     * no sistema SGPC.
     * 
     * @return boolean sempre true (contas não são bloqueadas)
     */
    public boolean isAccountNonLocked() {
        return true;
    }
    
    /**
     * Indica se as credenciais do usuário não estão expiradas.
     * 
     * Por padrão, credenciais não expiram no sistema SGPC.
     * 
     * @return boolean sempre true (credenciais não expiram)
     */
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    /**
     * Indica se o usuário está habilitado/ativo.
     * 
     * Controla se o usuário pode fazer login no sistema.
     * Usuários inativos não podem se autenticar.
     * 
     * @return boolean true se o usuário está ativo
     */
    public boolean isEnabled() {
        return isActive;
    }
    
    /**
     * Retorna o ID único do usuário.
     * 
     * @return Long ID do usuário no banco de dados
     */
    public Long getId() {
        return id;
    }
}