# sgpc-api
Repositório do BACK-END/API REST do trabalho A3 "ObraFácil"

# Política de Versionamento
- `main`: Código pronto para produção
- `develop`: Versão de desenvolvimento
- `feature/*`: Novas funcionalidades
- `hotfix/*`: Correções urgentes
![PoliticasBranches drawio](https://github.com/user-attachments/assets/9ef3caa1-2484-40ef-87ec-afb587b62ba9)

## Dependências do Backend
- Java 17
- Spring Boot 3.5.0
- Maven
- MySQL 8.0+

## Configuração do Banco de Dados

### MySQL Local
1. Instale o MySQL Server 8.0+
2. Crie o banco de dados:
```sql
CREATE DATABASE sgpc_db;
CREATE USER 'sgpc_user'@'localhost' IDENTIFIED BY 'sgpc_password';
GRANT ALL PRIVILEGES ON sgpc_db.* TO 'sgpc_user'@'localhost';
FLUSH PRIVILEGES;
```

### Variáveis de Ambiente (Opcional)
Você pode sobrescrever as configurações padrão definindo as seguintes variáveis:
- `JDBC_URI`: URL de conexão com o banco
- `DB_DRIVER`: Driver do banco de dados  
- `DB_USERNAME`: Usuário do banco
- `DB_PASSWORD`: Senha do banco

## Como Executar
```bash
cd sgpc-api
./mvnw spring-boot:run
```

A aplicação estará disponível em: http://localhost:8000
