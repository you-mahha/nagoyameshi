INSERT IGNORE INTO categories (name) VALUES
('和食'),
('洋食'),
('ラーメン'),
('カフェ'),
('イタリアン');

INSERT INTO users (email, password, role, enabled) VALUES
(
 'admin@example.com',
 '$2a$10$COozH2GB6bJphjeaZShKSOMkTHQnBypnoexJk8tOGWhE1xiVnnDea',
 'ADMIN',
 true
);
