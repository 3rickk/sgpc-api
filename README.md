# SGPC API - Sistema de Gerenciamento de Projetos de Construção

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**Sistema completo para gerenciamento de projetos de construção civil com API REST robusta e documentação interativa**

---

## 📋 Sobre o Projeto

O **SGPC API** é uma solução completa para gerenciamento de projetos de construção civil, desenvolvida com Spring Boot 3.3.5 e Java 17. O sistema oferece uma API REST com autenticação JWT e documentação interativa Swagger.

### 🎯 Principais Funcionalidades

- **🏗️ Gestão de Projetos**: CRUD completo com controle de equipes e orçamentos
- **✅ Sistema de Tarefas**: Workflow Kanban (A_FAZER → EM_ANDAMENTO → CONCLUIDA)
- **📦 Gestão de Materiais**: Controle de estoque com alertas de baixo estoque
- **📋 Solicitações de Materiais**: Workflow de aprovação com rastreabilidade
- **💰 Controle Financeiro**: Gestão de custos por categoria (mão de obra, material, equipamento)
- **📊 Dashboard**: Métricas em tempo real e indicadores de performance
- **📈 Relatórios**: Exportação em JSON, CSV e PDF
- **🔐 Autenticação JWT**: Sistema seguro com controle de acesso por roles

## 🚀 Stack Tecnológica

- **Java 17** - LTS com performance otimizada
- **Spring Boot 3.3.5** - Framework enterprise
- **Spring Security + JWT** - Autenticação e autorização
- **Spring Data JPA + Hibernate** - ORM avançado
- **MySQL 8.0+** - Banco de dados principal
- **Flyway** - Migrações automáticas
- **SpringDoc OpenAPI 3** - Documentação Swagger
- **iText PDF + JFreeChart** - Geração de relatórios
- **Maven + Lombok** - Build e redução de boilerplate

## ⚡ Quick Start

### 1. Clone e Configure o Banco

```bash
# Clone o repositório
git clone https://github.com/seu-usuario/sgpc-api.git
cd sgpc-api/sgpc-api

# Configure MySQL
mysql -u root -p
CREATE DATABASE sgpc_db;
EXIT;

# Execute a aplicação
./mvnw spring-boot:run
```

### 2. Primeiro Acesso

- **Aplicação**: http://localhost:8080
- **Documentação**: http://localhost:8080/swagger-ui.html
- **Usuário padrão**: admin@sgpc.com / admin123

### 3. Teste a API

```bash
# 1. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@sgpc.com","password":"admin123"}'

# 2. Use o token retornado
curl -X GET http://localhost:8080/api/projects \
  -H "Authorization: Bearer SEU_TOKEN_JWT"
```

## 📖 Documentação da API

### 🌟 Swagger UI Interativo

Acesse: **http://localhost:8080/swagger-ui.html**

**Recursos:**
- 🔐 Autenticação JWT integrada
- 📋 Documentação completa com exemplos
- 🧪 Interface "Try it out" para testes
- 📊 Schemas detalhados

### 🏷️ Principais Endpoints

| Módulo | Endpoint | Descrição |
|--------|----------|-----------|
| **Autenticação** | `POST /api/auth/login` | Login com JWT |
| **Dashboard** | `GET /api/dashboard` | Métricas consolidadas |
| **Projetos** | `GET /api/projects` | Listar projetos |
| **Tarefas** | `GET /api/tasks` | Listar tarefas |
| **Materiais** | `GET /api/materials` | Gestão de materiais |
| **Solicitações** | `GET /api/material-requests` | Solicitações de material |
| **Relatórios** | `GET /api/reports/*` | Relatórios diversos |

### 📄 OpenAPI Specs

- **JSON**: http://localhost:8080/v3/api-docs
- **YAML**: http://localhost:8080/v3/api-docs.yaml

## 🛠️ Instalação Completa

### Pré-requisitos

- Java 17+
- Maven 3.6+
- MySQL 8.0+

### Configuração do Banco

```sql
CREATE DATABASE sgpc_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'sgpc_user'@'localhost' IDENTIFIED BY 'sgpc_password';
GRANT ALL PRIVILEGES ON sgpc_db.* TO 'sgpc_user'@'localhost';
FLUSH PRIVILEGES;
```

### Variáveis de Ambiente (Opcional)

```bash
export DATABASE_URL="jdbc:mysql://localhost:3306/sgpc_db"
export DATABASE_USERNAME="sgpc_user"
export DATABASE_PASSWORD="sgpc_password"
export JWT_SECRET="sgpcSecretKeyForJWTTokenGenerationAndValidation2025"
```

### Execução

```bash
# Desenvolvimento
./mvnw spring-boot:run

# Produção
./mvnw clean package -DskipTests
java -jar target/sgpc-api-0.0.1-SNAPSHOT.jar
```

## 👥 Sistema de Permissões

| Role | Descrição | Principais Permissões |
|------|-----------|----------------------|
| **ADMIN** | Administrador | Acesso total ao sistema |
| **MANAGER** | Gerente | Criar/editar projetos, aprovar solicitações |
| **USER** | Usuário padrão | Visualizar projetos, criar tarefas* |

*Apenas para projetos onde é membro da equipe

## 📊 Principais Funcionalidades

### Gestão de Projetos
- CRUD completo com controle de status
- Gestão de equipes e orçamentos
- Cronograma com alertas de atraso
- Upload de anexos

### Sistema de Tarefas
- Workflow Kanban visual
- Prioridades (1-Baixa a 4-Crítica)
- Controle de horas estimadas vs realizadas
- Custos por categoria

### Controle de Materiais
- Estoque atual vs mínimo
- Alertas de baixo estoque
- Histórico de movimentações
- Solicitações com workflow de aprovação

### Dashboard e Relatórios
- Métricas em tempo real
- Relatórios em JSON, CSV e PDF
- Indicadores de performance
- Exportação de dados

## 🐛 Troubleshooting

| Problema | Solução |
|----------|---------|
| **Erro MySQL** | Verificar se MySQL está rodando |
| **Token inválido** | Tokens expiram em 24h - fazer novo login |
| **403 Forbidden** | Verificar permissões do usuário |
| **Upload falha** | Máximo 10MB por arquivo |

## 🚀 Deploy

### Docker
```bash
docker build -t sgpc-api .
docker run -p 8080:8080 \
  -e DATABASE_URL="jdbc:mysql://host:3306/sgpc_db" \
  sgpc-api
```

### Produção
```bash
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET="CHAVE_SUPER_SEGURA"
export DATABASE_URL="jdbc:mysql://host:3306/sgpc_prod"
```

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch: `git checkout -b feature/MinhaFeature`
3. Commit: `git commit -m 'feat: Adiciona MinhaFeature'`
4. Push: `git push origin feature/MinhaFeature`
5. Abra um Pull Request

### Padrões de Commit
- `Feature:` nova funcionalidade
- `HotFix:` correção de bug
- `developer:` documentação
- `main:` refatoração

## 📄 Licença

MIT License - veja [LICENSE](LICENSE) para detalhes.

## 📞 Suporte

- **Documentação**: http://localhost:8080/swagger-ui.html

---

**Desenvolvido com ❤️ para a comunidade de construção civil**
