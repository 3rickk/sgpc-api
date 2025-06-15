-- Alteração da tabela tasks para incluir campos de custo
ALTER TABLE tasks 
ADD COLUMN labor_cost DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Custo de mão de obra',
ADD COLUMN material_cost DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Custo de materiais',
ADD COLUMN equipment_cost DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Custo de equipamentos',
ADD COLUMN total_cost DECIMAL(15,2) GENERATED ALWAYS AS (COALESCE(labor_cost, 0) + COALESCE(material_cost, 0) + COALESCE(equipment_cost, 0)) STORED COMMENT 'Custo total calculado automaticamente';

-- Alteração da tabela projects para incluir campos de orçamento realizados
ALTER TABLE projects 
ADD COLUMN realized_cost DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Custo realizado do projeto',
ADD COLUMN budget_variance DECIMAL(15,2) GENERATED ALWAYS AS (COALESCE(total_budget, 0) - COALESCE(realized_cost, 0)) STORED COMMENT 'Variação orçamentária (orçado - realizado)',
ADD COLUMN progress_percentage DECIMAL(5,2) DEFAULT 0.00 COMMENT 'Percentual de progresso geral do projeto (0-100)';

-- Criação da tabela de serviços/custos padronizados
CREATE TABLE services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT 'Nome do serviço',
    description TEXT COMMENT 'Descrição detalhada do serviço',
    unit_of_measurement VARCHAR(50) NOT NULL COMMENT 'Unidade de medida (m², m³, horas, etc.)',
    unit_labor_cost DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Custo unitário de mão de obra',
    unit_material_cost DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Custo unitário de materiais',
    unit_equipment_cost DECIMAL(15,2) DEFAULT 0.00 COMMENT 'Custo unitário de equipamentos',
    total_unit_cost DECIMAL(15,2) GENERATED ALWAYS AS (COALESCE(unit_labor_cost, 0) + COALESCE(unit_material_cost, 0) + COALESCE(unit_equipment_cost, 0)) STORED COMMENT 'Custo unitário total',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Indica se o serviço está ativo',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Índices
    INDEX idx_service_name (name),
    INDEX idx_service_active (is_active),
    INDEX idx_service_unit (unit_of_measurement),
    
    -- Constraint para evitar nomes duplicados
    UNIQUE KEY uk_service_name (name)
);

-- Criação da tabela de relacionamento entre tarefas e serviços
CREATE TABLE task_services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    quantity DECIMAL(15,2) NOT NULL DEFAULT 1.00 COMMENT 'Quantidade do serviço utilizada na tarefa',
    unit_cost_override DECIMAL(15,2) NULL COMMENT 'Custo unitário específico para esta tarefa (sobrescreve o padrão do serviço)',
    notes TEXT COMMENT 'Observações específicas para este serviço na tarefa',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Chaves estrangeiras
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE RESTRICT,
    
    -- Índices
    INDEX idx_task_services_task (task_id),
    INDEX idx_task_services_service (service_id),
    
    -- Constraint para evitar duplicação de serviços na mesma tarefa
    UNIQUE KEY uk_task_service (task_id, service_id)
);

-- Inserção de dados de exemplo para serviços padronizados
INSERT INTO services (name, description, unit_of_measurement, unit_labor_cost, unit_material_cost, unit_equipment_cost) VALUES 
('Alvenaria de tijolo comum', 'Execução de alvenaria com tijolo comum, incluindo mão de obra e materiais básicos', 'm²', 25.00, 35.00, 5.00),
('Concretagem estrutural', 'Concretagem de estruturas com concreto usinado, incluindo mão de obra e bombeamento', 'm³', 80.00, 320.00, 60.00),
('Pintura látex interna', 'Pintura com tinta látex em paredes internas, incluindo preparação da superfície', 'm²', 8.00, 12.00, 2.00),
('Instalação elétrica básica', 'Instalação elétrica residencial básica por ponto', 'ponto', 45.00, 35.00, 10.00),
('Revestimento cerâmico', 'Assentamento de revestimento cerâmico em pisos e paredes', 'm²', 30.00, 50.00, 8.00),
('Cobertura com telha cerâmica', 'Execução de cobertura com estrutura de madeira e telha cerâmica', 'm²', 40.00, 85.00, 15.00),
('Fundação corrida', 'Escavação e execução de fundação corrida em concreto armado', 'm', 60.00, 120.00, 25.00),
('Acabamento em gesso', 'Aplicação de gesso liso em paredes e tetos', 'm²', 12.00, 8.00, 3.00);

-- Inserção de dados de exemplo associando serviços às tarefas existentes
INSERT INTO task_services (task_id, service_id, quantity, notes) VALUES 
-- Projeto Residencial Alpha
(3, 7, 50.00, 'Fundação corrida para residência de 200m²'), -- Fundação
(4, 2, 25.00, 'Concretagem da estrutura - pilares e vigas'), -- Estrutura de concreto
(5, 1, 180.00, 'Alvenaria das paredes externas e internas'), -- Alvenaria
(6, 6, 220.00, 'Cobertura completa da residência'), -- Cobertura
(7, 3, 400.00, 'Pintura interna de toda a casa'), -- Acabamentos
(7, 5, 120.00, 'Revestimento cerâmico nos banheiros e cozinha'), -- Acabamentos

-- Projeto Reforma Comercial Beta
(10, 4, 15.00, 'Pontos elétricos para a loja'), -- Instalações elétricas
(11, 8, 150.00, 'Gesso nas paredes danificadas'), -- Gesso e pintura
(11, 3, 180.00, 'Pintura completa da loja'), -- Gesso e pintura
(12, 5, 80.00, 'Piso cerâmico na área de vendas'); -- Piso

-- Atualização dos custos das tarefas baseado nos serviços associados
UPDATE tasks t 
SET 
    labor_cost = (
        SELECT COALESCE(SUM(
            ts.quantity * COALESCE(ts.unit_cost_override, s.unit_labor_cost)
        ), 0)
        FROM task_services ts 
        JOIN services s ON ts.service_id = s.id 
        WHERE ts.task_id = t.id
    ),
    material_cost = (
        SELECT COALESCE(SUM(
            ts.quantity * s.unit_material_cost
        ), 0)
        FROM task_services ts 
        JOIN services s ON ts.service_id = s.id 
        WHERE ts.task_id = t.id
    ),
    equipment_cost = (
        SELECT COALESCE(SUM(
            ts.quantity * s.unit_equipment_cost
        ), 0)
        FROM task_services ts 
        JOIN services s ON ts.service_id = s.id 
        WHERE ts.task_id = t.id
    )
WHERE EXISTS (
    SELECT 1 FROM task_services ts WHERE ts.task_id = t.id
);

-- Atualização do custo realizado dos projetos baseado nas tarefas concluídas
UPDATE projects p 
SET realized_cost = (
    SELECT COALESCE(SUM(t.total_cost), 0)
    FROM tasks t 
    WHERE t.project_id = p.id 
    AND t.status IN ('CONCLUIDA')
);

-- Atualização do progresso dos projetos baseado no progresso das tarefas
UPDATE projects p 
SET progress_percentage = (
    SELECT COALESCE(AVG(t.progress_percentage), 0)
    FROM tasks t 
    WHERE t.project_id = p.id
); 