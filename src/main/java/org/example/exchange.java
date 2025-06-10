import java.sql.*;

public class H2Example {
    public static void main(String[] args) {
        String jdbcUrl = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"; // In-memory БД
        String user = "sa";
        String password = "";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             Statement stmt = conn.createStatement()) {

            // Загружаем schema.sql
            String schemaSql = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("schema.sql")));
            stmt.execute(schemaSql);

            // Вставка данных
            String insertSql = "INSERT INTO users (name, email) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, "Alice");
                pstmt.setString(2, "alice@example.com");
                pstmt.executeUpdate();

                pstmt.setString(1, "Bob");
                pstmt.setString(2, "bob@example.com");
                pstmt.executeUpdate();
            }

            // Чтение и вывод
            ResultSet rs = stmt.executeQuery("SELECT * FROM users");
            while (rs.next()) {
                System.out.printf("User: %d, %s, %s%n",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
