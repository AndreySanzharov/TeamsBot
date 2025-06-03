package org.example.DecomposedCode;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;

public class HandlerProcessor {
    private static final Logger log = LoggerFactory.getLogger(HandlerProcessor.class);
    private static UserStateManager userStateManager;

    public HandlerProcessor(UserStateManager userStateManager) {
        this.userStateManager = userStateManager;
    }

    public static boolean processHandler(String handler, String input, String chatId) {
        if (handler == null) {
            return true;
        }
        switch (handler) {
            case "validateName":
                return validateName(input, chatId);
            default:
                log.warn("Неизвестный обработчик: {}", handler);
                return true;
        }
    }

    private static boolean validateName(String input, String chatId) {
        if (input == null) {
            return false;
        }
        int spaceCount = (int) input.chars().filter(ch -> ch == ' ').count();
        boolean valid = spaceCount >= 2;

        if (!valid) {
            userStateManager.sendText(chatId, "Неверный формат ФИО. Попробуйте еще раз");
        }
        log.info("Проверка ФИО '{}': {}  пробелов => {}", input, spaceCount, valid);
        return valid;
    }

    private static boolean searchName(String input, String chatId, Set<String> usersList) {
        boolean isUserInList = usersList.contains(input);
        if (!isUserInList) {
            userStateManager.sendText(chatId, "Пользователь не найден. Попробуйте еще раз");
        }
        return isUserInList;
    }
}