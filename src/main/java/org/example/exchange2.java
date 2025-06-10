import java.sql.*;
import java.util.*;

public class UserAnswersDao {
    private static final String JDBC_URL = "jdbc:h2:./data/tagbotdb"; // файл БД в папке проекта
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public UserAnswersDao() {
        initDb();
    }

    private void initDb() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS user_answers (" +
                    "chat_id VARCHAR(255)," +
                    "question VARCHAR(1000)," +
                    "answer VARCHAR(1000)," +
                    "PRIMARY KEY(chat_id, question))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    public void saveAnswer(String chatId, String question, String answer) {
        String sql = "MERGE INTO user_answers (chat_id, question, answer) KEY (chat_id, question) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chatId);
            ps.setString(2, question);
            ps.setString(3, answer);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getAnswers(String chatId) {
        Map<String, String> answers = new HashMap<>();
        String sql = "SELECT question, answer FROM user_answers WHERE chat_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chatId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    answers.put(rs.getString("question"), rs.getString("answer"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answers;
    }
}
