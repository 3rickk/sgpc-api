package br.com.sgpc.sgpc_api.security;

import java.util.Set;
import java.util.stream.Collectors;

import br.com.sgpc.sgpc_api.entity.User;

public class UserDetailsImpl {
    
    private Long id;
    private String email;
    private String password;
    private Set<String> authorities;
    private boolean isActive;
    
    public UserDetailsImpl(Long id, String email, String password, 
                          Set<String> authorities, boolean isActive) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.isActive = isActive;
    }
    
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
    
    public Set<String> getAuthorities() {
        return authorities;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getUsername() {
        return email;
    }
    
    public boolean isAccountNonExpired() {
        return true;
    }
    
    public boolean isAccountNonLocked() {
        return true;
    }
    
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    public boolean isEnabled() {
        return isActive;
    }
    
    public Long getId() {
        return id;
    }
}