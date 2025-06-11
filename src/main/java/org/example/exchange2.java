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



public void getConfigParams() {
    String query = "SELECT config_key, config_value FROM config";
    try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {

        while (rs.next()) {
            String key = rs.getString("config_key").trim();
            String value = rs.getString("config_value").trim();
            switch (key) {
                case "token" -> token = value;
                case "host" -> host = value;
                default -> log.warn("Неизвестный параметр конфигурации: {}", key);
            }
        }

        log.info("Конфигурация получена: token='{}', host='{}'", token, host);

    } catch (SQLException e) {
        log.error("Ошибка при получении конфигурации: {}", e.getMessage(), e);
    }
}
