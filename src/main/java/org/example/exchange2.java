package org.example.TagBot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Handlers {
    private static final Logger log = LoggerFactory.getLogger(Handlers.class);
    private static UserStateManager userStateManager;

    // Пул потоков для выполнения задач
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static void setUserStateManager(UserStateManager manager) {
        userStateManager = manager;
    }

    public static Future<Boolean> validateNameAsync(String input, String chatId) {
        return executor.submit(() -> {
            log.info("Сработал обработчик validateName");
            if (input == null) return false;
            int spaceCount = (int) input.chars().filter(ch -> ch == ' ').count();
            boolean valid = spaceCount >= 2;

            if (!valid) {
                userStateManager.sendText(chatId, "Неверный формат ФИО. Попробуйте еще раз");
            }
            log.info("Проверка ФИО '{}': {} пробелов => {}", input, spaceCount, valid);
            return valid;
        });
    }

    public static Future<Boolean> searchNameAsync(String input, String chatId) {
        return executor.submit(() -> {
            log.info("Сработал обработчик searchName");
            Set<String> usersList = Set.of("Иванов Иван Иванович", "Петров Петр Петрович"); // пример
            boolean isUserInList = usersList.contains(input);
            if (!isUserInList) {
                userStateManager.sendText(chatId, "Пользователь не найден. Попробуйте еще раз");
            }
            return isUserInList;
        });
    }

    public static Future<Boolean> answersAsync(String input, String chatId) {
        return executor.submit(() -> {
            log.info("Сработал обработчик answers");
            String question = "??";
            // здесь можно добавить логику обработки ответа
            return true;
        });
    }

    // Метод для завершения всех потоков — вызывать при остановке приложения
    public static void shutdown() {
        executor.shutdown();
    }
}
