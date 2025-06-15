package br.com.sgpc.sgpc_api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sgpc.sgpc_api.entity.User;
import br.com.sgpc.sgpc_api.repository.UserRepository;

@Service
public class UserDetailsServiceImpl {
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public UserDetailsImpl loadUserByUsername(String email) throws RuntimeException {
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + email));
        
        return UserDetailsImpl.build(user);
    }
} 