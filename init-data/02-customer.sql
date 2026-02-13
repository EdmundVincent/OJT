-- 顧客マスタ
CREATE TABLE customer_master (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 初期データ投入
INSERT INTO customer_master (name, contact_person, email, phone) VALUES
('株式会社A', '山田 太郎', 'taro.yamada@example.com', '03-1234-5678'),
('株式会社B', '鈴木 花子', 'hanako.suzuki@example.com', '03-8765-4321'),
('株式会社C', '佐藤 次郎', 'jiro.sato@example.com', '06-1234-5678');
