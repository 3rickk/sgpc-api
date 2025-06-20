-- Migration V8: Adicionar coluna created_by_user_id na tabela projects
-- Para controle de acesso e isolamento de dados entre usuários

-- Adiciona coluna para rastrear quem criou o projeto
ALTER TABLE projects 
ADD COLUMN created_by_user_id BIGINT;

-- Adiciona foreign key constraint
ALTER TABLE projects 
ADD CONSTRAINT fk_projects_created_by_user 
FOREIGN KEY (created_by_user_id) REFERENCES users(id);

-- Cria índice para melhorar performance nas consultas
CREATE INDEX idx_projects_created_by_user_id ON projects(created_by_user_id);

-- Para projetos existentes sem criador definido, definir como NULL
-- Isso permite que sejam visíveis para todos os usuários até serem reatribuídos
UPDATE projects SET created_by_user_id = NULL WHERE created_by_user_id IS NULL; 