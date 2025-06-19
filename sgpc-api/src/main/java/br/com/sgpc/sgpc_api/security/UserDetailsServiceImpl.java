package br.com.sgpc.sgpc_api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.repository.UserRepository;

/**
 * Implementação do serviço de carregamento de detalhes do usuário para Spring Security.
 * 
 * Esta classe é responsável por carregar informações do usuário durante o processo
 * de autenticação, integrando o sistema de usuários do SGPC com o Spring Security.
 * Serve como ponte entre o repositório de usuários e o sistema de autenticação.
 * 
 * Funcionalidades principais:
 * - Carregamento de usuário por email (username)
 * - Conversão de User para UserDetailsImpl
 * - Carregamento de roles associadas ao usuário
 * - Tratamento de usuários não encontrados
 * - Integração transacional para consistência de dados
 * 
 * Processo de autenticação:
 * 1. Recebe email como identificador único
 * 2. Busca usuário no banco com roles carregadas
 * 3. Lança exceção se usuário não existe
 * 4. Converte User para UserDetailsImpl
 * 5. Retorna objeto pronto para Spring Security
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Service
public class UserDetailsServiceImpl {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Carrega os detalhes do usuário pelo email para autenticação.
     * 
     * Este método é chamado automaticamente pelo Spring Security durante
     * o processo de autenticação para carregar as informações completas
     * do usuário, incluindo suas roles.
     * 
     * @param email email do usuário a ser autenticado
     * @return UserDetailsImpl detalhes do usuário para Spring Security
     * @throws RuntimeException se o usuário não for encontrado
     */
    @Transactional
    public UserDetailsImpl loadUserByUsername(String email) throws RuntimeException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + email));
        
        return UserDetailsImpl.build(user);
    }
} 