-- コードマスタ
CREATE TABLE code_master (
    category_id VARCHAR(20) NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    code_id VARCHAR(20) NOT NULL,
    code_name VARCHAR(100) NOT NULL,
    sort_order INT DEFAULT 0,
    PRIMARY KEY (category_id, code_id)
);

-- 利用者マスタ
CREATE TABLE user_master (
    email VARCHAR(255) PRIMARY KEY,
    valid_from DATE NOT NULL,
    valid_to DATE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 要員マスタ
CREATE TABLE employee_master (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    join_year INT NOT NULL,
    rank VARCHAR(50),
    department VARCHAR(100),
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- プロジェクトマスタ
CREATE TABLE project_master (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    customer_id VARCHAR(100),
    end_user VARCHAR(200),
    start_ym INT NOT NULL CHECK (start_ym % 100 BETWEEN 1 AND 12),
    end_ym INT CHECK (end_ym % 100 BETWEEN 1 AND 12),
    parent_id BIGINT REFERENCES project_master(id),
    revenue BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- プロジェクト計画情報
CREATE TABLE project_plan (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES project_master(id) ON DELETE CASCADE,
    plan_version INT NOT NULL DEFAULT 1,
    start_ym INT NOT NULL CHECK (start_ym % 100 BETWEEN 1 AND 12),
    end_ym INT NOT NULL CHECK (end_ym % 100 BETWEEN 1 AND 12),
    resource_count DECIMAL(4,1) NOT NULL,
    production_amount BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (project_id, plan_version),
    CHECK (end_ym >= start_ym)
);

-- アサイン情報
CREATE TABLE assignment (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES project_master(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES employee_master(id) ON DELETE CASCADE,
    start_ym INT NOT NULL CHECK (start_ym % 100 BETWEEN 1 AND 12),
    end_ym INT NOT NULL CHECK (end_ym % 100 BETWEEN 1 AND 12),
    unit_price INT NOT NULL,
    allocation_ratio DECIMAL(3,2) NOT NULL CHECK (allocation_ratio > 0 AND allocation_ratio <= 1.50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (project_id, employee_id, start_ym),
    CHECK (end_ym >= start_ym)
);

-- インデックス
CREATE INDEX idx_project_plan_project_id ON project_plan(project_id);
CREATE INDEX idx_assignment_project_id ON assignment(project_id);
CREATE INDEX idx_assignment_employee_id ON assignment(employee_id);
CREATE INDEX idx_assignment_ym ON assignment(start_ym, end_ym);

-- 初期データ投入
-- コードマスタ
INSERT INTO code_master (category_id, category_name, code_id, code_name, sort_order) VALUES
('REPORT_TYPE', 'レポート種別', 'RESOURCE_ALLOCATION', '要員稼働率一覧', 1),
('REPORT_TYPE', 'レポート種別', 'BUDGET_MISMATCH', '予算不一致プロジェクト', 2),
('REPORT_TYPE', 'レポート種別', 'PROJECT_COST', 'プロジェクトコスト分析', 3),
('RANK', 'ランク', 'JUNIOR', 'ジュニア', 1),
('RANK', 'ランク', 'MIDDLE', 'ミドル', 2),
('RANK', 'ランク', 'SENIOR', 'シニア', 3),
('DEPARTMENT', '部門', 'SALES', '営業部', 1),
('DEPARTMENT', '部門', 'DEVELOPMENT', '開発部', 2),
('DEPARTMENT', '部門', 'CONSULTING', 'コンサル部', 3);

-- 利用者マスタ (password: password123)
INSERT INTO user_master (email, valid_from, valid_to, role, password_hash) VALUES
('admin@example.com', '2025-01-01', NULL, 'ADMIN', '$2a$10$nbLf2YoOSs43TWeS0fwWyeY.ek7fgfbO8bcKDs5IHfWZBU.LmmLpq'),
('user@example.com', '2025-01-01', NULL, 'USER', '$2a$10$nbLf2YoOSs43TWeS0fwWyeY.ek7fgfbO8bcKDs5IHfWZBU.LmmLpq');
