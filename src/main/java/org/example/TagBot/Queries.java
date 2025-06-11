package org.example.TagBot;

public interface Queries {
    String saveAnswersQuery = "INSERT INTO users (chatId, answer) VALUES (?, ?)";
    String printDBQuery = "SELECT * FROM users";
    String getConfigQuery = "SELECT config_key, config_value FROM config";
}
