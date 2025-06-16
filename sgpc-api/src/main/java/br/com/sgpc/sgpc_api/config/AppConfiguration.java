package br.com.sgpc.sgpc_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuração principal da aplicação SGPC.
 * 
 * Esta classe centraliza as configurações globais da aplicação Spring Boot,
 * habilitando funcionalidades essenciais para o funcionamento do sistema.
 * 
 * Funcionalidades habilitadas:
 * 
 * @EnableAsync - Habilita processamento assíncrono
 * - Permite execução de métodos em threads separadas
 * - Usado para operações que não bloqueiam a thread principal
 * - Exemplo: envio de emails, processamento de relatórios
 * 
 * @EnableAspectJAutoProxy - Habilita programação orientada a aspectos (AOP)
 * - Permite interceptação de métodos via aspectos
 * - Usado para auditoria automática via AuditAspect
 * - Exemplo: log automático de operações CRUD
 * 
 * Benefícios:
 * - Separação de responsabilidades (cross-cutting concerns)
 * - Auditoria transparente e automática
 * - Performance melhorada com processamento assíncrono
 * - Código mais limpo e modular
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Configuration
@EnableAsync
@EnableAspectJAutoProxy
public class AppConfiguration {
    // Configurações globais da aplicação
    
    // Nota: Esta classe pode ser expandida para incluir:
    // - Configuração de thread pools para @Async
    // - Beans de configuração customizados
    // - Configuração de cache
    // - Configuração de internacionalização
    // - Configuração de timezone padrão
} 