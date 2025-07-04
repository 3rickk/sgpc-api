-- Criação da tabela roles
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Criação da tabela users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    hourly_rate DECIMAL(10,2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Criação da tabela de relacionamento many-to-many entre users e roles
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Criação da tabela password_reset_tokens
CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Inserção de dados iniciais para roles
INSERT INTO roles (name, description) VALUES 
('ADMIN', 'Administrador do sistema - acesso total'),
('USER', 'Usuário padrão - acesso básico às funcionalidades'),
('MANAGER', 'Gerente - gerenciamento de projetos e aprovações');

-- Inserção de usuário administrador padrão
-- Senha: admin123 (criptografada com BCrypt)
INSERT INTO users (full_name, email, password_hash, is_active) VALUES 
('Administrador', 'admin@sgpc.com', '$2a$12$GGo8LGyW4.d7Rr1fehf6L.cmZ6lIJ4uKINabaIBjsfIb3FeocdjFa', TRUE);

-- Atribuindo apenas role ADMIN ao usuário administrador (role única)
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 1); 