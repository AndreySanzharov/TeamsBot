package org.example.DecomposedCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class HandlerProcessor {
    private static final Logger log = LoggerFactory.getLogger(HandlerProcessor.class);
    private static UserStateManager userStateManager;

    public HandlerProcessor(UserStateManager userStateManager) {
        HandlerProcessor.userStateManager = userStateManager;
        Handlers.setUserStateManager(userStateManager); // Передаём в Handlers
    }

    public static boolean processHandler(String handler, String input, String chatId) {
        if (handler == null) {
            return true;
        }
        try {
            Method method = Handlers.class.getDeclaredMethod(handler, String.class, String.class);
            return (boolean) method.invoke(null, input, chatId);
        } catch (NoSuchMethodException e) {
            log.warn("Обработчик '{}' не найден.", handler);
        } catch (Exception e) {
            log.error("Ошибка при выполнении обработчика '{}': {}", handler, e.getMessage());
        }
        return true;
    }
}

package org.example.DecomposedCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class Handlers {
    private static final Logger log = LoggerFactory.getLogger(Handlers.class);
    private static UserStateManager userStateManager;

    public static void setUserStateManager(UserStateManager manager) {
        userStateManager = manager;
    }

    public static boolean validateName(String input, String chatId) {
        if (input == null) return false;
        int spaceCount = (int) input.chars().filter(ch -> ch == ' ').count();
        boolean valid = spaceCount >= 2;

        if (!valid) {
            userStateManager.sendText(chatId, "Неверный формат ФИО. Попробуйте еще раз");
        }
        log.info("Проверка ФИО '{}': {} пробелов => {}", input, spaceCount, valid);
        return valid;
    }

    public static boolean searchName(String input, String chatId) {
        Set<String> usersList = Set.of("Иванов Иван Иванович", "Петров Петр Петрович"); // пример списка
        boolean isUserInList = usersList.contains(input);
        if (!isUserInList) {
            userStateManager.sendText(chatId, "Пользователь не найден. Попробуйте еще раз");
        }
        return isUserInList;
    }

    // Добавляйте сюда новые методы по мере необходимости, и вызывайте их по имени из JSON.
}
