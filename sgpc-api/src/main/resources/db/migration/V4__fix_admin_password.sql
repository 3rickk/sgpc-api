-- Correção da senha do usuário administrador para compatibilidade com sistema de autenticação
UPDATE users 
SET password_hash = 'hashed_admin123' 
WHERE email = 'admin@sgpc.com'; 