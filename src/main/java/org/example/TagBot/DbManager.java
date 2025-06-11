package org.example.TagBot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;

public class DbManager {
    private final Logger log = LoggerFactory.getLogger(DbManager.class);
    private final String jdbcUrl = "jdbc:h2:file:./data/testdb;DB_CLOSE_DELAY=-1";
    private final String user = "sa";
    private final String password = "";

    private String host = "";
    private String token = "";


    public void saveUserAnswer(String chatId, String answer) throws SQLException, IOException {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             Statement stmt = conn.createStatement()) {
            String schemaSql = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("C:\\Users\\SanzharovAA\\TeamsBot\\src\\main\\resources\\schema.sql")));
            stmt.execute(schemaSql);
            try (PreparedStatement pstmt = conn.prepareStatement(Queries.saveAnswersQuery)) {
                pstmt.setString(1, chatId);
                pstmt.setString(2, answer);
                pstmt.executeUpdate();
            }

            // Чтение и вывод
            ResultSet rs = stmt.executeQuery(Queries.printDBQuery);
            while (rs.next()) {
                System.out.printf("%d, %s, %s%n",
                        rs.getInt("id"),
                        rs.getString("chatId"),
                        rs.getString("answer"));
            }
        }
    }

    private void initDatabase() throws SQLException, IOException {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             Statement stmt = conn.createStatement()) {
            String shemaSql = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/main/resources/schema.sql")));
            stmt.execute(shemaSql);
        }
    }

    public void getConfigParams() throws SQLException, IOException {
        initDatabase();
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(Queries.getConfigQuery)) {

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

    public String getHost() {
        return host;
    }

    public String getToken() {
        return token;
    }
}
