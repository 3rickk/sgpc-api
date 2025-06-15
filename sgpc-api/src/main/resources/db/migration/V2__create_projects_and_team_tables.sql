-- Criação da tabela projects
CREATE TABLE projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    start_date_planned DATE,
    end_date_planned DATE,
    start_date_actual DATE,
    end_date_actual DATE,
    total_budget DECIMAL(15,2),
    client VARCHAR(255),
    status ENUM('PLANEJAMENTO', 'EM_ANDAMENTO', 'CONCLUIDO', 'SUSPENSO', 'CANCELADO') NOT NULL DEFAULT 'PLANEJAMENTO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project_name (name),
    INDEX idx_project_status (status),
    INDEX idx_project_client (client),
    INDEX idx_project_dates (start_date_planned, end_date_planned)
);

-- Criação da tabela de associação many-to-many entre projetos e usuários (equipe)
CREATE TABLE project_members (
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, user_id),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_project_members_project (project_id),
    INDEX idx_project_members_user (user_id)
);

-- Inserção de dados de exemplo
INSERT INTO projects (name, description, client, total_budget, status, start_date_planned, end_date_planned) VALUES 
('Construção Residencial Alpha', 'Projeto de construção de casa residencial de 200m²', 'João Silva', 250000.00, 'PLANEJAMENTO', '2025-02-01', '2025-08-31'),
('Reforma Comercial Beta', 'Reforma completa de loja comercial no centro da cidade', 'Empresa XYZ Ltda', 150000.00, 'EM_ANDAMENTO', '2025-01-15', '2025-04-30'),
('Edifício Gamma', 'Construção de edifício residencial de 8 andares', 'Construtora ABC', 2500000.00, 'PLANEJAMENTO', '2025-03-01', '2026-02-28');

-- Associação de usuários aos projetos (assumindo que o usuário admin (id=1) participa dos projetos)
INSERT INTO project_members (project_id, user_id) VALUES 
(1, 1),
(2, 1),
(3, 1); 