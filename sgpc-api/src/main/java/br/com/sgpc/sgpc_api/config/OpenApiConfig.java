package br.com.sgpc.sgpc_api.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Configuração do OpenAPI/Swagger para documentação da API.
 * 
 * Esta classe configura a documentação automática da API usando SpringDoc OpenAPI,
 * incluindo informações gerais da API, autenticação JWT e esquemas de segurança.
 * 
 * @author Sistema SGPC
 * @version 1.0
 * @since 2024
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configura a documentação OpenAPI da aplicação.
     * 
     * Define informações gerais da API como título, descrição, versão, contato
     * e licença. Também configura o esquema de segurança JWT Bearer Token.
     * 
     * @return OpenAPI configuração completa da documentação
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Gerenciamento de Projetos de Construção - API")
                        .description("API REST para gerenciamento completo de projetos de construção civil, " +
                                   "incluindo controle de tarefas, materiais, custos, relatórios e dashboards. " +
                                   "Desenvolvida com Spring Boot 3, Spring Security e JWT para autenticação.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe SGPC")
                                .email("contato@sgpc.com.br")
                                .url("https://sgpc.com.br"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("https://sgpc-api.koyeb.app")
                                .description("Servidor de Produção (Koyeb)"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desenvolvimento Local")
                ))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    /**
     * Cria o esquema de autenticação JWT Bearer Token.
     * 
     * Configura o esquema de segurança para autenticação via JWT Bearer Token
     * no cabeçalho Authorization das requisições HTTP.
     * 
     * @return SecurityScheme esquema de segurança JWT configurado
     */
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("Token JWT obtido através do endpoint de autenticação (/auth/login). " +
                           "Formato: Bearer {token}");
    }
} 