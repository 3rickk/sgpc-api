package br.com.sgpc.sgpc_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal da aplicação SGPC API.
 * 
 * Esta é a classe de inicialização do Sistema de Gerenciamento de Projetos
 * de Construção (SGPC), desenvolvido com Spring Boot 3.5.0 e Java 17.
 * 
 * O sistema oferece funcionalidades completas para:
 * - Gerenciamento de projetos de construção civil
 * - Controle de tarefas com sistema Kanban
 * - Gestão de materiais e estoque
 * - Workflow de aprovação de solicitações
 * - Sistema de custos e orçamentos
 * - Auditoria automática de operações
 * - Relatórios e dashboard gerencial
 * - Autenticação JWT com controle de acesso
 * - API REST documentada com Swagger/OpenAPI
 * 
 * Principais tecnologias utilizadas:
 * - Spring Boot 3.5.0
 * - Spring Security com JWT
 * - Spring Data JPA
 * - Hibernate
 * - PostgreSQL/MySQL
 * - Swagger/OpenAPI 3
 * - Lombok
 * - Maven
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@SpringBootApplication
public class SgpcApiApplication {
    
    /**
     * Método principal que inicia a aplicação Spring Boot.
     * 
     * @param args argumentos de linha de comando
     */
    public static void main(String[] args) {
        SpringApplication.run(SgpcApiApplication.class, args);
    }
}