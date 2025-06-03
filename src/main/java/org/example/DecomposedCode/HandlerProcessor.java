package org.example.DecomposedCode;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

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