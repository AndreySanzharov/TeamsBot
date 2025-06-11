DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS config;

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    chatId VARCHAR(100) NOT NULL,
    answer VARCHAR(100) NOT NULL
);

CREATE TABLE config (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value VARCHAR(255) NOT NULL
);

-- Заполнение конфигурации
INSERT INTO config (config_key, config_value) VALUES 
    ('token', '001.1031916963.1477955322:1000000106'),
    ('host', 'https://api.vkteams.ext.lukoil.com/');
