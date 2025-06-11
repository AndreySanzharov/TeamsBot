package org.example.TagBot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class DbManager {
    private final Logger log = LoggerFactory.getLogger(DbManager.class);
    private final String jdbcUrl = "jdbc:h2:file:./data/testdb;DB_CLOSE_DELAY=-1";
    private final String user = "sa";
    private final String password = "";

    private String host = "";
    private String token = "";

    public DbManager() {
        try {
            initDatabase();
        } catch (Exception e) {
            log.error("Ошибка при инициализации БД: {}", e.getMessage(), e);
        }
    }

    private void initDatabase() throws SQLException, IOException {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password)) {
            ResultSet rs = conn.getMetaData().getTables(null, null, "CONFIG", null);
            if (!rs.next()) {
                log.info("Инициализируем базу данных...");
                String schemaSql = Files.readString(Paths.get("src/main/resources/schema.sql"));
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(schemaSql);
                }
            } else {
                log.info("База данных уже инициализирована.");
            }
        }
    }

    public void getConfigParams() {
        String query = "SELECT token, host FROM config LIMIT 1";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                token = rs.getString("token").trim();
                host = rs.getString("host").trim();
                log.info("Конфигурация получена: token='{}', host='{}'", token, host);
            } else {
                log.warn("Нет параметров конфигурации в таблице config.");
            }

        } catch (SQLException e) {
            log.error("Ошибка при получении конфигурации из БД: {}", e.getMessage(), e);
        }
    }

    public void saveUserAnswer(String chatId, String answer) {
        String insertSql = "INSERT INTO users (chatId, answer) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, chatId);
            pstmt.setString(2, answer);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Ошибка при сохранении ответа пользователя: {}", e.getMessage(), e);
        }
    }

    public String getHost() {
        return host;
    }

    public String getToken() {
        return token;
    }
}
