-- Migration V4: Criar tabela de materiais
-- Atende aos requisitos RF10 e RF12

CREATE TABLE materials (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    unit_of_measure VARCHAR(50) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    supplier VARCHAR(255),
    current_stock DECIMAL(10,3) NOT NULL DEFAULT 0.000,
    minimum_stock DECIMAL(10,3) NOT NULL DEFAULT 0.000,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- Índices para melhorar performance
CREATE INDEX idx_materials_name ON materials(name);
CREATE INDEX idx_materials_supplier ON materials(supplier);
CREATE INDEX idx_materials_active ON materials(is_active);
CREATE INDEX idx_materials_stock_alert ON materials(current_stock, minimum_stock, is_active);

-- Restrições
ALTER TABLE materials ADD CONSTRAINT uk_materials_name UNIQUE (name);
ALTER TABLE materials ADD CONSTRAINT chk_materials_unit_price CHECK (unit_price > 0);
ALTER TABLE materials ADD CONSTRAINT chk_materials_current_stock CHECK (current_stock >= 0);
ALTER TABLE materials ADD CONSTRAINT chk_materials_minimum_stock CHECK (minimum_stock >= 0); 