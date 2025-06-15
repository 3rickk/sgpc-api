-- Migration para criar tabelas de requisições de materiais
-- Versão: V5
-- Descrição: Criação das tabelas material_requests e material_request_items

-- Tabela de requisições de materiais (cabeçalho)
CREATE TABLE material_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    request_date DATE NOT NULL,
    needed_date DATE,
    status ENUM('PENDENTE', 'APROVADA', 'REJEITADA') NOT NULL DEFAULT 'PENDENTE',
    rejection_reason VARCHAR(1000),
    approved_by_id BIGINT,
    approved_at DATETIME,
    observations VARCHAR(2000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    
    -- Chaves estrangeiras
    CONSTRAINT fk_material_requests_project 
        FOREIGN KEY (project_id) REFERENCES projects(id),
    
    CONSTRAINT fk_material_requests_requester 
        FOREIGN KEY (requester_id) REFERENCES users(id),
    
    CONSTRAINT fk_material_requests_approver 
        FOREIGN KEY (approved_by_id) REFERENCES users(id),
    
    -- Índices
    INDEX idx_material_requests_project (project_id),
    INDEX idx_material_requests_requester (requester_id),
    INDEX idx_material_requests_status (status),
    INDEX idx_material_requests_request_date (request_date),
    INDEX idx_material_requests_created_at (created_at)
);

-- Tabela de itens das requisições de materiais
CREATE TABLE material_request_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    material_request_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,
    quantity DECIMAL(10,3) NOT NULL,
    unit_price DECIMAL(10,2),
    observations VARCHAR(1000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    
    -- Chaves estrangeiras
    CONSTRAINT fk_material_request_items_request 
        FOREIGN KEY (material_request_id) REFERENCES material_requests(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_material_request_items_material 
        FOREIGN KEY (material_id) REFERENCES materials(id),
    
    -- Índices
    INDEX idx_material_request_items_request (material_request_id),
    INDEX idx_material_request_items_material (material_id),
    
    -- Constraints de validação
    CONSTRAINT chk_material_request_items_quantity_positive 
        CHECK (quantity > 0),
    
    CONSTRAINT chk_material_request_items_unit_price_positive 
        CHECK (unit_price IS NULL OR unit_price >= 0)
);

-- Comentários nas tabelas
ALTER TABLE material_requests 
    COMMENT = 'Tabela para armazenar requisições de materiais dos projetos';

ALTER TABLE material_request_items 
    COMMENT = 'Tabela para armazenar os itens das requisições de materiais'; 