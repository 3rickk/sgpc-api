package br.com.sgpc.sgpc_api.security;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    
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
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
        
        List<String> roleNames = userRepository.findRoleNamesByEmail(email);
        
        return UserDetailsImpl.buildWithRoleNames(user, roleNames);
    }
} 