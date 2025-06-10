CREATE TABLE IF NOT EXISTS user_answers (
    id IDENTITY PRIMARY KEY,
    chat_id VARCHAR(255) NOT NULL,
    question VARCHAR(1000) NOT NULL,
    answer VARCHAR(1000) NOT NULL
);
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class UserAnswersDao {
    private static final String JDBC_URL = "jdbc:h2:./data/tagbotdb;INIT=RUNSCRIPT FROM 'classpath:schema.sql'";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public UserAnswersDao() {
        // schema.sql запускается автоматически при первом подключении
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    public void saveAnswer(String chatId, String question, String answer) {
        String sql = "INSERT INTO user_answers (chat_id, question, answer) VALUES (?, ?, ?)";
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
