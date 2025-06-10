package org.example.TagBot;

import java.io.IOException;
import java.sql.*;

public class DbManager {
    String jdbcUrl = "jdbc:h2:file:./data/testdb;DB_CLOSE_DELAY=-1";
    String user = "sa";
    String password = "";

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
}
