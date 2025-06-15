package br.com.sgpc.sgpc_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseDto {
    
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String email;
    private String fullName;
    private Set<String> roles;
    
    public JwtResponseDto(String token, Long userId, String email, String fullName, Set<String> roles) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
    }
} 