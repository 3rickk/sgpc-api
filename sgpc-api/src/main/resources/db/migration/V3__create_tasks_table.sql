-- Criação da tabela tasks
CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status ENUM('A_FAZER', 'EM_ANDAMENTO', 'CONCLUIDA', 'BLOQUEADA', 'CANCELADA') NOT NULL DEFAULT 'A_FAZER',
    start_date_planned DATE,
    end_date_planned DATE,
    start_date_actual DATE,
    end_date_actual DATE,
    progress_percentage INT NOT NULL DEFAULT 0,
    priority INT NOT NULL DEFAULT 1,
    estimated_hours INT,
    actual_hours INT,
    notes TEXT,
    project_id BIGINT NOT NULL,
    assigned_user_id BIGINT,
    created_by_user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Chaves estrangeiras
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    
    -- Índices para otimização
    INDEX idx_task_project (project_id),
    INDEX idx_task_status (status),
    INDEX idx_task_assigned_user (assigned_user_id),
    INDEX idx_task_created_by (created_by_user_id),
    INDEX idx_task_priority (priority),
    INDEX idx_task_dates (start_date_planned, end_date_planned),
    INDEX idx_task_title_project (project_id, title),
    
    -- Constraint para evitar títulos duplicados no mesmo projeto
    UNIQUE KEY uk_task_title_project (project_id, title)
);

-- Inserção de dados de exemplo para o projeto "Construção Residencial Alpha" (id=1)
INSERT INTO tasks (title, description, status, priority, start_date_planned, end_date_planned, estimated_hours, project_id, created_by_user_id) VALUES 
('Projeto arquitetônico', 'Elaboração do projeto arquitetônico da residência', 'CONCLUIDA', 3, '2025-02-01', '2025-02-15', 80, 1, 1),
('Aprovação na prefeitura', 'Dar entrada nos documentos na prefeitura para aprovação', 'EM_ANDAMENTO', 3, '2025-02-16', '2025-03-01', 20, 1, 1),
('Fundação', 'Escavação e execução da fundação', 'A_FAZER', 4, '2025-03-15', '2025-04-15', 200, 1, 1),
('Estrutura de concreto', 'Execução da estrutura de concreto armado', 'A_FAZER', 4, '2025-04-16', '2025-06-15', 300, 1, 1),
('Alvenaria', 'Execução das paredes de alvenaria', 'A_FAZER', 2, '2025-06-16', '2025-07-15', 150, 1, 1),
('Cobertura', 'Instalação da cobertura e telhado', 'A_FAZER', 3, '2025-07-16', '2025-08-15', 100, 1, 1),
('Acabamentos', 'Pintura, revestimentos e acabamentos finais', 'A_FAZER', 2, '2025-08-16', '2025-08-31', 120, 1, 1);

-- Inserção de dados de exemplo para o projeto "Reforma Comercial Beta" (id=2)
INSERT INTO tasks (title, description, status, priority, start_date_planned, end_date_planned, estimated_hours, project_id, assigned_user_id, created_by_user_id) VALUES 
('Demolição', 'Demolição das paredes internas existentes', 'CONCLUIDA', 3, '2025-01-15', '2025-01-25', 60, 2, 1, 1),
('Projeto elétrico', 'Elaboração do novo projeto elétrico', 'CONCLUIDA', 3, '2025-01-26', '2025-02-05', 40, 2, 1, 1),
('Instalações elétricas', 'Execução das novas instalações elétricas', 'EM_ANDAMENTO', 4, '2025-02-06', '2025-02-20', 80, 2, 1, 1),
('Gesso e pintura', 'Aplicação de gesso e pintura das paredes', 'A_FAZER', 2, '2025-02-21', '2025-03-15', 100, 2, 1, 1),
('Piso', 'Instalação do novo piso', 'A_FAZER', 3, '2025-03-16', '2025-04-01', 120, 2, 1, 1),
('Móveis planejados', 'Instalação dos móveis planejados', 'A_FAZER', 2, '2025-04-02', '2025-04-20', 80, 2, 1, 1),
('Limpeza final', 'Limpeza geral para entrega', 'A_FAZER', 1, '2025-04-21', '2025-04-30', 20, 2, 1, 1);

-- Inserção de dados de exemplo para o projeto "Edifício Gamma" (id=3)
INSERT INTO tasks (title, description, status, priority, start_date_planned, end_date_planned, estimated_hours, project_id, created_by_user_id) VALUES 
('Estudo de viabilidade', 'Análise de viabilidade técnica e financeira do projeto', 'A_FAZER', 4, '2025-03-01', '2025-03-15', 120, 3, 1),
('Projeto estrutural', 'Elaboração do projeto estrutural do edifício', 'A_FAZER', 4, '2025-03-16', '2025-04-30', 300, 3, 1),
('Licenças ambientais', 'Obtenção das licenças ambientais necessárias', 'A_FAZER', 3, '2025-04-01', '2025-05-31', 80, 3, 1),
('Terraplenagem', 'Preparação do terreno e escavações', 'A_FAZER', 3, '2025-06-01', '2025-07-15', 250, 3, 1),
('Fundação profunda', 'Execução da fundação com estacas', 'A_FAZER', 4, '2025-07-16', '2025-09-30', 400, 3, 1); 