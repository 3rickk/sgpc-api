package br.com.sgpc.sgpc_api.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sgpc.sgpc_api.dto.PasswordResetDto;
import br.com.sgpc.sgpc_api.dto.PasswordResetRequestDto;
import br.com.sgpc.sgpc_api.entity.PasswordResetToken;
import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.repository.PasswordResetTokenRepository;
import br.com.sgpc.sgpc_api.repository.UserRepository;

@Service
@Transactional
public class PasswordResetService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    @Autowired
    private UserService userService;
    
    public String generatePasswordResetToken(PasswordResetRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com este email"));
        
        // Remove tokens antigos do usuário
        passwordResetTokenRepository.deleteByUser_Id(user.getId());
        
        // Gera novo token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24); // Expira em 24 horas
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiresAt(expiresAt);
        
        passwordResetTokenRepository.save(resetToken);
        
        // TODO: Aqui deveria enviar email com o token
        // Por simplicidade, vamos retornar o token (em produção nunca fazer isso)
        return token;
    }
    
    public void resetPassword(PasswordResetDto resetDto) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(resetDto.getToken())
                .orElseThrow(() -> new RuntimeException("Token inválido"));
        
        if (resetToken.isExpired()) {
            throw new RuntimeException("Token expirado");
        }
        
        User user = resetToken.getUser();
        user.setPasswordHash(hashPassword(resetDto.getNewPassword()));
        userRepository.save(user);
        
        // Remove o token usado
        passwordResetTokenRepository.delete(resetToken);
    }
    
    private String hashPassword(String password) {
        // Simplificado - usar BCrypt em produção
        return "hashed_" + password;
    }
    
    public void cleanExpiredTokens() {
        passwordResetTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
} 