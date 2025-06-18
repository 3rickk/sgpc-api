# SGPC API - Sistema de Gerenciamento de Projetos de Construção

Sistema completo para gerenciamento de projetos de construção civil, desenvolvido com Spring Boot 3.5.0 e Java 17. O SGPC oferece funcionalidades completas para controle de projetos, tarefas, materiais, custos e equipes com **documentação interativa completa**.

## 🚀 Funcionalidades Principais

- **👥 Gerenciamento de Usuários**: CRUD completo com controle de acesso baseado em roles
- **🏗️ Gestão de Projetos**: Criação, acompanhamento e controle de projetos de construção
- **✅ Controle de Tarefas**: Sistema Kanban para gerenciamento de tarefas com workflow completo
- **📦 Gestão de Materiais**: Controle de estoque com alertas de baixo estoque
- **📋 Solicitações de Materiais**: Workflow de aprovação para requisições de materiais
- **💰 Controle de Custos**: Gestão de serviços, orçamentos e custos realizados
- **📊 Dashboard**: Métricas e estatísticas consolidadas do sistema
- **📈 Relatórios**: Geração de relatórios em JSON e CSV
- **🔐 Autenticação JWT**: Sistema seguro de autenticação e autorização
- **📚 Documentação Completa**: Swagger/OpenAPI 3 + JavaDoc com exemplos práticos

## 📋 Tecnologias

- **Java 17**
- **Spring Boot 3.5.0**
- **Spring Security** com JWT
- **Spring Data JPA** com Hibernate
- **MySQL 8.0+**
- **Maven** para gerenciamento de dependências
- **Flyway** para migrações de banco
- **SpringDoc OpenAPI** para documentação Swagger
- **Lombok** para redução de boilerplate

## 🛠️ Instalação e Configuração

### Pré-requisitos

- Java 17 ou superior
- Maven 3.6+
- MySQL 8.0+
- Git

### 1. Clone o Repositório

```bash
git clone https://github.com/seu-usuario/sgpc-api.git
cd sgpc-api
```

### 2. Configuração do Banco de Dados

#### MySQL Local
```sql
CREATE DATABASE sgpc_db;
CREATE USER 'sgpc_user'@'localhost' IDENTIFIED BY 'sgpc_password';
GRANT ALL PRIVILEGES ON sgpc_db.* TO 'sgpc_user'@'localhost';
FLUSH PRIVILEGES;
```

#### Variáveis de Ambiente (Opcional)
Você pode sobrescrever as configurações padrão:

```bash
export JDBC_URI="jdbc:mysql://localhost:3306/sgpc_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USERNAME="sgpc_user"
export DB_PASSWORD="sgpc_password"
export JWT_SECRET="sgpcSecretKeyForJWTTokenGenerationAndValidation2025"
```

### 3. Executar a Aplicação

```bash
cd sgpc-api
./mvnw spring-boot:run
```

A aplicação estará disponível em: **http://localhost:8080**

## 📚 Documentação da API

### 🌟 **Swagger UI - Interface Interativa**

Acesse a documentação interativa da API em:
```
http://localhost:8080/swagger-ui.html
```

**Recursos disponíveis:**
- 🔐 **Autenticação JWT integrada** - Teste endpoints autenticados diretamente na interface
- 📋 **Documentação completa** - Todos os endpoints documentados com exemplos
- 🧪 **Try it out** - Execute requests diretamente na interface
- 📊 **Schemas detalhados** - Estruturas de dados com exemplos práticos
- 🏷️ **Tags organizadas** - Endpoints agrupados por funcionalidade

### 📖 **OpenAPI Specification**

Acesse as especificações da API:
- **JSON**: `http://localhost:8080/v3/api-docs`
- **YAML**: `http://localhost:8080/v3/api-docs.yaml`

### 🏷️ **Tags da API**

A documentação está organizada nas seguintes categorias:

1. **🔐 Autenticação** - Login, registro e recuperação de senha
2. **📊 Dashboard** - Métricas e estatísticas do sistema
3. **🏗️ Gerenciamento de Projetos** - CRUD de projetos e controle de equipes
4. **✅ Gerenciamento de Tarefas** - Sistema Kanban e controle de progresso
5. **📦 Gerenciamento de Materiais** - Controle de estoque e materiais
6. **📋 Solicitações de Materiais** - Workflow de aprovação de requisições
7. **💰 Gerenciamento de Custos** - Serviços, orçamentos e controle financeiro
8. **👥 Gerenciamento de Usuários** - CRUD de usuários e controle de acesso
9. **📈 Relatórios** - Geração e exportação de relatórios

### 📝 **JavaDoc**

A documentação JavaDoc está disponível em todas as classes principais:
- **Controllers**: Documentação completa dos endpoints
- **Services**: Lógica de negócio e regras
- **Entities**: Modelo de dados e relacionamentos
- **DTOs**: Estruturas de transferência de dados

## 🔐 Autenticação

A API utiliza autenticação JWT Bearer Token. Para acessar endpoints protegidos:

### 1. Fazer Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@sgpc.com",
  "password": "admin123"
}
```

### 2. Usar o Token
Inclua o token no header das requisições:
```http
Authorization: Bearer {seu-token-jwt}
```

## 🌐 Principais Endpoints

### Autenticação
- `POST /api/auth/login` - Fazer login
- `POST /api/auth/register` - Registrar usuário
- `POST /api/auth/forgot-password` - Solicitar recuperação de senha
- `POST /api/auth/reset-password` - Redefinir senha

### Projetos
- `GET /api/projects` - Listar todos os projetos
- `POST /api/projects` - Criar novo projeto
- `GET /api/projects/{id}` - Obter projeto por ID
- `PUT /api/projects/{id}` - Atualizar projeto
- `DELETE /api/projects/{id}` - Excluir projeto
- `GET /api/projects/status/{status}` - Projetos por status
- `GET /api/projects/delayed` - Projetos atrasados

### Tarefas
- `GET /api/tasks` - Listar tarefas
- `POST /api/tasks` - Criar nova tarefa
- `PUT /api/tasks/{id}` - Atualizar tarefa
- `PUT /api/tasks/{id}/status` - Atualizar status da tarefa
- `GET /api/tasks/project/{projectId}` - Tarefas por projeto

### Materiais
- `GET /api/materials` - Listar materiais
- `POST /api/materials` - Criar material
- `PUT /api/materials/{id}` - Atualizar material
- `GET /api/materials/low-stock` - Materiais com estoque baixo

### Relatórios
- `GET /api/reports/dashboard` - Dashboard geral
- `GET /api/reports/projects` - Relatório de projetos
- `GET /api/reports/costs` - Relatório de custos
- `GET /api/reports/stock` - Relatório de estoque

## 🧪 Testes

### Testando com Swagger UI
1. Acesse http://localhost:8080/swagger-ui.html
2. Clique em "Authorize" no topo da página
3. Faça login usando o endpoint `/api/auth/login`
4. Copie o token retornado
5. Cole no campo "Value" como: `Bearer {token}`
6. Agora você pode testar todos os endpoints protegidos

### Usuário Padrão para Testes
- **Email**: admin@sgpc.com
- **Senha**: admin123

## 📂 Estrutura do Projeto

```
sgpc-api/
├── src/
│   ├── main/
│   │   ├── java/br/com/sgpc/sgpc_api/
│   │   │   ├── config/          # Configurações (OpenAPI, Security)
│   │   │   ├── controller/      # Controllers REST
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── entity/         # Entidades JPA
│   │   │   ├── enums/          # Enumeradores
│   │   │   ├── repository/     # Repositórios JPA
│   │   │   ├── service/        # Lógica de negócio
│   │   │   └── security/       # Configurações de segurança
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/   # Scripts Flyway
│   └── test/                   # Testes unitários
├── target/                     # Arquivos compilados
├── pom.xml                     # Dependências Maven
└── README.md
```

## 🔧 Configurações Avançadas

### Perfis de Execução
- **Desenvolvimento**: `spring.profiles.active=dev`
- **Produção**: `spring.profiles.active=prod`
- **Testes**: `spring.profiles.active=test`

### Upload de Arquivos
- Tamanho máximo: 10MB
- Diretório padrão: `uploads/`
- Configurável via: `app.file.upload-dir`

### Notificações por Email
Configure as variáveis SMTP:
```bash
export MAIL_HOST="smtp.gmail.com"
export MAIL_USERNAME="seu-email@gmail.com"
export MAIL_PASSWORD="sua-app-password"
```

## 🐛 Troubleshooting

### Problemas Comuns

1. **Erro de Conexão com Banco**
   - Verifique se o MySQL está rodando
   - Confirme as credenciais no application.properties

2. **Token JWT Inválido**
   - Verifique se o token não expirou (24h padrão)
   - Confirme o formato: `Bearer {token}`

3. **Erro 403 Forbidden**
   - Usuário não tem permissão para o endpoint
   - Token JWT não fornecido ou inválido

4. **Erro de CORS**
   - Configurado para aceitar requests de qualquer origem
   - Verifique se está usando a porta correta (8080)

## 📄 Licença

Este projeto está licenciado sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 👥 Contribuição

### Política de Branches
- `main`: Código pronto para produção
- `develop`: Versão de desenvolvimento  
- `feature/*`: Novas funcionalidades
- `hotfix/*`: Correções urgentes

### Como Contribuir
1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

---

**📞 Suporte**: Para dúvidas ou problemas, abra uma issue no GitHub ou entre em contato através do email: contato@sgpc.com.br
