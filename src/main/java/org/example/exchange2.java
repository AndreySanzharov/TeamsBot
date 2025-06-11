DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS config;

CREATE TABLE users(
  id INT PRIMARY KEY AUTO_INCREMENT,
  chatId VARCHAR(100) NOT NULL,
  answer VARCHAR(100) NOT NULL
);

CREATE TABLE config(
  id INT PRIMARY KEY AUTO_INCREMENT,
  token VARCHAR(255) NOT NULL,
  host VARCHAR(255) NOT NULL
);

INSERT INTO config (token, host)
     VALUES ('001.1031916963.1477955322:1000000106', 'https://api.vkteams.ext.lukoil.com/');

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
            String insertSql = "INSERT INTO users (chatId, answer) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, chatId);
                pstmt.setString(2, answer);
                pstmt.executeUpdate();
            }

            // Чтение и вывод
            ResultSet rs = stmt.executeQuery("SELECT * FROM users");
            while (rs.next()) {
                System.out.printf("User: %d, %s, %s%n",
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
        String query = "SELECT token, host FROM config LIMIT 1";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                token = rs.getString("token");
                host = rs.getString("host");
            } else {
                log.warn("Нет параметров конфигурации");
            }
        }
    }

    public String getHost() {
        return host;
    }

    public String getToken() {
        return token;
    }
}
Exception in thread "main" java.lang.IllegalArgumentException: Expected URL scheme 'http' or 'https' but no colon was found
	at okhttp3.HttpUrl$Builder.parse(HttpUrl.java:1332)
	at okhttp3.HttpUrl.get(HttpUrl.java:917)
	at ru.mail.im.botapi.api.ApiImplementationFactory.<init>(ApiImplementationFactory.java:32)
	at ru.mail.im.botapi.api.BotApi.<init>(BotApi.java:20)
	at ru.mail.im.botapi.BotApiClient.<init>(BotApiClient.java:88)
	at ru.mail.im.botapi.BotApiClient.<init>(BotApiClient.java:68)
	at ru.mail.im.botapi.BotApiClient.<init>(BotApiClient.java:58)
	at org.example.TagBot.TagBotApplication.main(TagBotApplication.java:21)
