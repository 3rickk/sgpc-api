package br.com.sgpc.sgpc_api.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sgpc.sgpc_api.dto.PasswordResetDto;
import br.com.sgpc.sgpc_api.dto.PasswordResetRequestDto;
import br.com.sgpc.sgpc_api.entity.PasswordResetToken;
import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.repository.PasswordResetTokenRepository;
import br.com.sgpc.sgpc_api.repository.UserRepository;

/**
 * Serviço responsável pelo processo de redefinição de senha.
 * 
 * Este serviço implementa um fluxo seguro de recuperação de senha
 * através de tokens temporários enviados por email. Inclui validação
 * de tokens, controle de expiração e limpeza automática.
 * 
 * Funcionalidades principais:
 * - Geração de tokens únicos de recuperação
 * - Validação e expiração de tokens (24 horas)
 * - Redefinição segura de senhas
 * - Limpeza automática de tokens expirados
 * - Prevenção de reutilização de tokens
 * 
 * Características de segurança:
 * - Tokens únicos gerados com UUID
 * - Expiração automática em 24 horas
 * - Invalidação após uso único
 * - Remoção de tokens antigos do usuário
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Service
@Transactional
public class PasswordResetService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Gera um token de redefinição de senha para o usuário.
     * 
     * Este método busca o usuário pelo email, remove tokens anteriores
     * para evitar acúmulo, gera um novo token único com UUID e
     * configura expiração para 24 horas.
     * 
     * @param request dados da solicitação contendo email do usuário
     * @return String token gerado para redefinição
     * @throws RuntimeException se o usuário não for encontrado
     */
    public String generatePasswordResetToken(PasswordResetRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com este email"));
        
        // Remove tokens antigos do usuário para evitar acúmulo
        passwordResetTokenRepository.deleteByUser_Id(user.getId());
        
        // Gera novo token único
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24); // Expira em 24 horas
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiresAt(expiresAt);
        
        passwordResetTokenRepository.save(resetToken);
        
        // NOTA: Em produção, deve ser implementado envio por email
        // emailService.sendPasswordResetEmail(user.getEmail(), token);
        
        // Por simplicidade no desenvolvimento, retornamos o token
        return token;
    }
    
    /**
     * Redefine a senha do usuário usando token válido.
     * 
     * Valida o token fornecido, verifica se não expirou,
     * atualiza a senha do usuário e remove o token usado
     * para evitar reutilização.
     * 
     * @param resetDto dados contendo token e nova senha
     * @throws RuntimeException se token inválido ou expirado
     */
    public void resetPassword(PasswordResetDto resetDto) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(resetDto.getToken())
                .orElseThrow(() -> new RuntimeException("Token inválido"));
        
        if (resetToken.isExpired()) {
            throw new RuntimeException("Token expirado");
        }
        
        User user = resetToken.getUser();
        user.setPasswordHash(hashPassword(resetDto.getNewPassword()));
        userRepository.save(user);
        
        // Remove o token usado para evitar reutilização
        passwordResetTokenRepository.delete(resetToken);
    }
    
    /**
     * Gera hash da senha fornecida usando BCrypt.
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
     * Remove tokens expirados do banco de dados.
     * 
     * Método utilitário para limpeza automática de tokens
     * expirados. Deve ser executado periodicamente via
     * scheduler para manter a base limpa.
     */
    public void cleanExpiredTokens() {
        passwordResetTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
} 