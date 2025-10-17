-- Sample data for RDPS application

-- Insert sample parameters (using MERGE to avoid duplicates)
MERGE INTO rdps_parameter (param_id, param_name, param_value, param_description) KEY(param_id) VALUES 
(1, 'SYSTEM_NAME', 'Recruitment Data Processing System', 'System name'),
(2, 'SYSTEM_VERSION', '1.0.0', 'System version');

-- Insert sample user roles
MERGE INTO rdps_user_role (role, fcn, active) KEY(role) VALUES 
('ADMIN', 'ROLE_ADMIN', 'Y'),
('USER', 'ROLE_USER', 'Y');

-- Insert sample user profile
MERGE INTO rdps_user_profile (cn, role, effective_date_from, active) KEY(cn, role) VALUES 
('admin', 'ADMIN', CURRENT_DATE, 'Y'); 