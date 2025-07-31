-- =============================================================
-- DATOS INICIALES PARA LA APLICACIÓN USER-SERVICE
-- =============================================================

-- Insertar roles básicos del sistema
INSERT INTO roles (authority, created_by, created_date, last_modified_by, last_modified_date) VALUES 
('ROLE_ADMIN', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('ROLE_USER', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- Insertar proveedores de autenticación
INSERT INTO auth_providers (name, created_by, created_date, last_modified_by, last_modified_date) VALUES 
('LOCAL', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
('GOOGLE', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- =============================================================
-- NOTAS:
-- - Los roles deben seguir el formato "ROLE_XXX" para Spring Security
-- - Los proveedores de autenticación son: LOCAL (registro local) y GOOGLE (OAuth2)
-- - created_date, last_modified_date, created_by, last_modified_by son campos heredados de AuditableEntity
-- - Los IDs se generan automáticamente con IDENTITY
-- - 'system' se usa como usuario por defecto para la creación inicial
-- ============================================================= 