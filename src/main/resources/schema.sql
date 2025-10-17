-- RDPS Database Schema for H2
-- This file contains all table creation scripts for the RDPS application

-- Create rdps_parameter table
CREATE TABLE IF NOT EXISTS rdps_parameter (
    param_id INT PRIMARY KEY,
    param_name VARCHAR(100) NOT NULL,
    param_value VARCHAR(500),
    param_description VARCHAR(200),
    created_date DATE DEFAULT CURRENT_DATE,
    created_by VARCHAR(50),
    updated_date DATE,
    updated_by VARCHAR(50)
);

-- Create rdps_user_profile table
CREATE TABLE IF NOT EXISTS rdps_user_profile (
    cn VARCHAR(100),
    role VARCHAR(100),
    effective_date_from DATE DEFAULT CURRENT_DATE,
    effective_date_to DATE,
    active VARCHAR(1) DEFAULT 'Y' NOT NULL,
    timestamp DATE DEFAULT CURRENT_DATE,
    userstamp VARCHAR(100) DEFAULT USER()
);

-- Create rdps_user_role table
CREATE TABLE IF NOT EXISTS rdps_user_role (
    role VARCHAR(100),
    fcn VARCHAR(100),
    active VARCHAR(1) DEFAULT 'Y' NOT NULL,
    timestamp DATE DEFAULT CURRENT_DATE,
    userstamp VARCHAR(100) DEFAULT USER()
);

-- Create rdps_candidate table
CREATE TABLE IF NOT EXISTS rdps_candidate (
    candidate_id INT PRIMARY KEY,
    candidate_no VARCHAR(20) UNIQUE,
    title VARCHAR(10),
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    chinese_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(200),
    district_id INT,
    date_of_birth DATE,
    gender VARCHAR(10),
    nationality VARCHAR(50),
    id_type VARCHAR(20),
    id_number VARCHAR(50),
    is_active INT DEFAULT 1,
    created_date DATE DEFAULT CURRENT_DATE,
    created_by VARCHAR(50),
    updated_date DATE,
    updated_by VARCHAR(50)
);

-- Create rdps_edu_prof_qual table
CREATE TABLE IF NOT EXISTS rdps_edu_prof_qual (
    qual_id INT PRIMARY KEY,
    candidate_id INT NOT NULL,
    edu_level_id INT,
    qual_award_class_id INT,
    qual_award_desc_id INT,
    institution VARCHAR(200),
    study_mode_id INT,
    start_date DATE,
    end_date DATE,
    is_active INT DEFAULT 1,
    created_date DATE DEFAULT CURRENT_DATE,
    created_by VARCHAR(50),
    updated_date DATE,
    updated_by VARCHAR(50),
    FOREIGN KEY (candidate_id) REFERENCES rdps_candidate(candidate_id)
);

-- Create rdps_work_experience table
CREATE TABLE IF NOT EXISTS rdps_work_experience (
    exp_id INT PRIMARY KEY,
    candidate_id INT NOT NULL,
    company_name VARCHAR(200),
    position VARCHAR(100),
    start_date DATE,
    end_date DATE,
    is_current_job INT DEFAULT 0,
    job_description CLOB,
    is_active INT DEFAULT 1,
    created_date DATE DEFAULT CURRENT_DATE,
    created_by VARCHAR(50),
    updated_date DATE,
    updated_by VARCHAR(50),
    FOREIGN KEY (candidate_id) REFERENCES rdps_candidate(candidate_id)
);

-- Create rdps_referee table
CREATE TABLE IF NOT EXISTS rdps_referee (
    referee_id INT PRIMARY KEY,
    candidate_id INT NOT NULL,
    referee_name VARCHAR(100),
    referee_title VARCHAR(100),
    referee_company VARCHAR(200),
    referee_email VARCHAR(100),
    referee_phone VARCHAR(20),
    relationship VARCHAR(100),
    is_active INT DEFAULT 1,
    created_date DATE DEFAULT CURRENT_DATE,
    created_by VARCHAR(50),
    updated_date DATE,
    updated_by VARCHAR(50),
    FOREIGN KEY (candidate_id) REFERENCES rdps_candidate(candidate_id)
);

-- Create rdps_other_info table
CREATE TABLE IF NOT EXISTS rdps_other_info (
    info_id INT PRIMARY KEY,
    candidate_id INT NOT NULL,
    info_type VARCHAR(50),
    info_value CLOB,
    is_active INT DEFAULT 1,
    created_date DATE DEFAULT CURRENT_DATE,
    created_by VARCHAR(50),
    updated_date DATE,
    updated_by VARCHAR(50),
    FOREIGN KEY (candidate_id) REFERENCES rdps_candidate(candidate_id)
);

-- Create rdps_email_template table
CREATE TABLE IF NOT EXISTS rdps_email_template (
    template_id INT PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL,
    template_subject VARCHAR(200),
    template_body CLOB,
    is_active INT DEFAULT 1,
    created_date DATE DEFAULT CURRENT_DATE,
    created_by VARCHAR(50),
    updated_date DATE,
    updated_by VARCHAR(50)
);

-- Create rdps_email_job table
CREATE TABLE IF NOT EXISTS rdps_email_job (
    job_id INT PRIMARY KEY,
    template_id INT,
    recipient_email VARCHAR(100),
    recipient_name VARCHAR(100),
    subject VARCHAR(200),
    body CLOB,
    status VARCHAR(20) DEFAULT 'PENDING',
    sent_date DATE,
    error_message CLOB,
    retry_count INT DEFAULT 0,
    created_date DATE DEFAULT CURRENT_DATE,
    created_by VARCHAR(50),
    updated_date DATE,
    updated_by VARCHAR(50),
    FOREIGN KEY (template_id) REFERENCES rdps_email_template(template_id)
);

-- Create rdps_system_log table
CREATE TABLE IF NOT EXISTS rdps_system_log (
    log_id INT PRIMARY KEY,
    log_level VARCHAR(10),
    log_message CLOB,
    log_source VARCHAR(100),
    log_date DATE DEFAULT CURRENT_DATE,
    user_id VARCHAR(50),
    ip_address VARCHAR(50)
);

-- Create rdps_shedlock table
CREATE TABLE IF NOT EXISTS rdps_shedlock (
    name VARCHAR(64) PRIMARY KEY,
    lock_until TIMESTAMP,
    locked_at TIMESTAMP,
    locked_by VARCHAR(255)
);

-- Create LOV tables
CREATE TABLE IF NOT EXISTS rdps_lov_district (
    district_id INT PRIMARY KEY,
    district_name VARCHAR(100) NOT NULL,
    district_code VARCHAR(20),
    is_active INT DEFAULT 1,
    created_date DATE DEFAULT CURRENT_DATE,
    created_by VARCHAR(50),
    updated_date DATE,
    updated_by VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS rdps_lov_edu_level (
    edu_level_id INT PRIMARY KEY,
    edu_level_name VARCHAR(100) NOT NULL,
    edu_level_code VARCHAR(20),
    is_active INT DEFAULT 1,
    created_date DATE DEFAULT CURRENT_DATE,
    created_by VARCHAR(50),
    updated_date DATE,
    updated_by VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS rdps_lov_qual_award_class (
    qual_award_class_id INT PRIMARY KEY,
    qual_award_class_name VARCHAR(100) NOT NULL,
    qual_award_class_code VARCHAR(20),
    is_active INT DEFAULT 1,
    created_date DATE DEFAULT CURRENT_DATE,
    created_by VARCHAR(50),
    updated_date DATE,
    updated_by VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS rdps_lov_qual_award_desc (
    qual_award_desc_id INT PRIMARY KEY,
    qual_award_desc_name VARCHAR(100) NOT NULL,
    qual_award_desc_code VARCHAR(20),
    is_active INT DEFAULT 1,
    created_date DATE DEFAULT CURRENT_DATE,
    created_by VARCHAR(50),
    updated_date DATE,
    updated_by VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS rdps_lov_study_mode (
    study_mode_id INT PRIMARY KEY,
    study_mode_name VARCHAR(100) NOT NULL,
    study_mode_code VARCHAR(20),
    is_active INT DEFAULT 1,
    created_date DATE DEFAULT CURRENT_DATE,
    created_by VARCHAR(50),
    updated_date DATE,
    updated_by VARCHAR(50)
);

-- Create sample tables
CREATE TABLE IF NOT EXISTS sample_booking (
    booking_id INT PRIMARY KEY,
    candidate_id INT,
    booking_date DATE,
    booking_time VARCHAR(10),
    status VARCHAR(20) DEFAULT 'PENDING',
    notes CLOB,
    created_date DATE DEFAULT CURRENT_DATE,
    created_by VARCHAR(50),
    updated_date DATE,
    updated_by VARCHAR(50),
    FOREIGN KEY (candidate_id) REFERENCES rdps_candidate(candidate_id)
);

CREATE TABLE IF NOT EXISTS test_schedule_table (
    schedule_id INT PRIMARY KEY,
    test_name VARCHAR(100),
    test_date DATE,
    test_time VARCHAR(10),
    location VARCHAR(200),
    max_participants INT,
    current_participants INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_date DATE DEFAULT CURRENT_DATE,
    created_by VARCHAR(50),
    updated_date DATE,
    updated_by VARCHAR(50)
); 