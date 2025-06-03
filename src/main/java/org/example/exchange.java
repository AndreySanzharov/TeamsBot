package org.example.DecomposedCode;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.im.botapi.BotApiClient;
import ru.mail.im.botapi.BotApiClientController;
import ru.mail.im.botapi.fetcher.event.CallbackQueryEvent;
import ru.mail.im.botapi.fetcher.event.Event;
import ru.mail.im.botapi.fetcher.event.NewMessageEvent;

import java.io.IOException;

class EventHandler {
    private final BotApiClient client;
    private final BotApiClientController controller;
    private final UserStateManager stateManager;
    private static final Logger log = LoggerFactory.getLogger(EventHandler.class);

    public EventHandler(BotApiClient client, BotApiClientController controller, UserStateManager stateManager) {
        this.client = client;
        this.controller = controller;
        this.stateManager = stateManager;
    }

    public void handleEvent(Event event) {
        switch (event) {
            case NewMessageEvent message -> handleMessageEvent(message);
            case CallbackQueryEvent callback -> handleCallbackEvent(callback);
            default -> log.warn("Неизвестный тип события: {}", event.getType());
        }
    }

    private void handleMessageEvent(NewMessageEvent message) {
        String chatId = message.getChat().getChatId();
        log.info("Новое сообщение от пользователя: {} = {{}}", chatId, message.getText());
        try {
            stateManager.processTextMessage(chatId, message);
            if (!stateManager.hasUserState(chatId)) {
                JSONObject root = stateManager.loadRootNode();
                stateManager.setUserState(chatId, root);
                stateManager.sendQuestionWithButtons(chatId, root);
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения: {}", e.getMessage());
            stateManager.sendText(chatId, "Ошибка загрузки меню: " + e.getMessage());
        }
    }

    private void handleCallbackEvent(CallbackQueryEvent callback) {
        String chatId = callback.getFrom().getUserId();
        String queryId = callback.getQueryId();
        String data = callback.getCallbackData();

        log.info("Callback от пользователя: {}| Кнопка: {}", chatId, data);
        stateManager.processCallback(chatId, data);

        try {
            client.messages().answerCallbackQuery(queryId, "", false, "");
        } catch (IOException e) {
            log.error("Ошибка подтверждения callback: {}", e.getMessage());
        }
    }
}
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
package org.example.DecomposedCode;

import org.example.DecomposedCode.UserStateManager;
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
        Set<String> usersList = Set.of("Иванов Иван Иванович", "Петров Петр Петрович"); // пример списка. Надо удалить
        boolean isUserInList = usersList.contains(input);
        if (!isUserInList) {
            userStateManager.sendText(chatId, "Пользователь не найден. Попробуйте еще раз");
        }
        return isUserInList;
    }
}
package org.example.DecomposedCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.im.botapi.BotApiClient;
import ru.mail.im.botapi.BotApiClientController;

public class TagBotApplication {
    private static final String TOKEN = "001.1031916963.1477955322:1000000106";
    private static final String HOST = "https://api.vkteams.ext.lukoil.com/";
    private static final Logger log = LoggerFactory.getLogger(TagBotApplication.class);

    public static void main(String[] args) {
        log.info("Бот запускается...");
        BotApiClient client = new BotApiClient(HOST, TOKEN, 0, 60);
        BotApiClientController controller = BotApiClientController.startBot(client);
        UserStateManager stateManager = new UserStateManager(client, controller);
        EventHandler handler = new EventHandler(client, controller, stateManager);

        UserStateManager userStateManager = new UserStateManager(client, controller);
        HandlerProcessor processor = new HandlerProcessor(userStateManager);

        client.addOnEventFetchListener(events -> events.forEach(handler::handleEvent));
        client.start();
        log.info("Бот запустился.");
    }
}
package org.example.DecomposedCode;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.im.botapi.BotApiClient;
import ru.mail.im.botapi.BotApiClientController;
import ru.mail.im.botapi.api.entity.InlineKeyboardButton;
import ru.mail.im.botapi.api.entity.SendTextRequest;
import ru.mail.im.botapi.fetcher.event.NewMessageEvent;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

class UserStateManager {
    private static final String JSON_PATH = "C:\\Users\\SanzharovAA\\TeamsBot\\src\\main\\resources\\questions.json";
    private final Map<String, JSONObject> userStates = new HashMap<>();
    private final Map<String, List<String>> userAnswers = new HashMap<>();
    private final Set<String> waitingForInput = new HashSet<>();
    private static final Logger log = LoggerFactory.getLogger(UserStateManager.class);
    private BotApiClient client;
    private BotApiClientController controller;


    public UserStateManager(BotApiClient client, BotApiClientController controller) {
        this.client = client;
        this.controller = controller;
    }

    public boolean hasUserState(String chatId) {
        return userStates.containsKey(chatId);
    }

    public void setUserState(String chatId, JSONObject state) {
        userStates.put(chatId, state);
    }

    public JSONObject loadRootNode() throws IOException {
        try (FileInputStream fis = new FileInputStream(JSON_PATH)) {
            return new JSONObject(new org.json.JSONTokener(fis));
        }
    }

    public void sendText(String chatId, String text) {
        try {
            controller.sendTextMessage(new SendTextRequest().setChatId(chatId).setText(text));
        } catch (IOException e) {
            log.error("Ошибка отправки текста: {}", e.getMessage());
        }
    }

    public void processTextMessage(String chatId, NewMessageEvent message) throws IOException {
        if (waitingForInput.contains(chatId)) {
            waitingForInput.remove(chatId);
            String userInput = message.getText();
            log.info("Пользователь написал сообщение: {}", userInput);
            JSONObject current = userStates.get(chatId);

            if (current != null && current.has("handler")) {
                String handler = current.getString("handler");
                if (!HandlerProcessor.processHandler(handler, userInput, chatId)) { // удалить последний параметр
                    waitingForInput.add(chatId);
                    return;
                }
            }

            saveUserAnswer(chatId, userInput);
            sendText(chatId, "Вы ввели: " + userInput);

            //JSONObject current = userStates.get(chatId);
            if (current != null && current.has("next")) {
                JSONObject next = current.getJSONObject("next");
                userStates.put(chatId, next);
                sendQuestionWithButtons(chatId, next);
            } else {
                getUserAnswers(chatId);
                sendText(chatId, "Диалог завершен. Напишите что-нибудь в чат, чтобы начать заново.");
                userAnswers.remove(chatId);
                userStates.remove(chatId);
            }
        }
    }

    public void processCallback(String chatId, String data) {
        JSONObject current = userStates.get(chatId);
        if (current == null) {
            log.warn("Нет состояния для пользователя: {}", chatId);
            return;
        }

        JSONArray options = current.optJSONArray("options");
        if (options == null) {
            log.warn("Нет опций у текущего состояния пользователя: {}", chatId);
            return;
        }

        for (int i = 0; i < options.length(); i++) {
            JSONObject option = options.getJSONObject(i);
            if (option.optString("text").equals(data)) {
                saveUserAnswer(chatId, data);
                if (option.has("next")) {
                    JSONObject next = option.getJSONObject("next");
                    log.debug("Переход к следующему узлу для пользователя: {}", chatId);
                    userStates.put(chatId, next);
                    sendQuestionWithButtons(chatId, next);
                } else {
                    sendText(chatId, "Вы выбрали: " + data);
                    getUserAnswers(chatId);
                    sendText(chatId, "Диалог завершен. Напишите что-нибудь в чат, чтобы начать заново.");
                    log.info("Диалог завершен для пользователя: {}", chatId);
                    userAnswers.remove(chatId);
                    userStates.remove(chatId);
                }
                break;
            }
        }
    }

    public void saveUserAnswer(String chatId, String answer) {
        userAnswers.computeIfAbsent(chatId, k -> new ArrayList<>()).add(answer);
        log.debug("Ответ пользователя {}: {}", chatId, answer);
    }

    public void getUserAnswers(String chatId) {
        sendText(chatId, "Ответы пользователя: " + chatId + "\n" + String.join("\n ", userAnswers.getOrDefault(chatId, Collections.emptyList())));
    }

    public void sendQuestionWithButtons(String chatId, JSONObject node) {
        String description = node.optString("description", "Выберите действие:");
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        JSONArray options = node.optJSONArray("options");
        boolean hasMessageTag = false;
        if (options != null) {
            for (int i = 0; i < options.length(); i++) {
                JSONObject option = options.getJSONObject(i);
                String tag = option.optString("tag");
                String text = option.optString("text", "");
                switch (tag) {
                    case "button" -> buttons.add(Collections.singletonList(
                            InlineKeyboardButton.callbackButton(text, text, "primary")
                    ));

                    case "message" -> {
                        sendText(chatId, text);
                        userStates.put(chatId, option);
                        waitingForInput.add(chatId);
                        hasMessageTag = true;
                    }


                    case "stop" -> {
                        sendText(chatId, "Составление заявки отменено");
                        try {
                            JSONObject root = loadRootNode();
                            userStates.put(chatId, root);
                        } catch (IOException e) {
                            log.error("Ошибка при возврате в меню: {}", e.getMessage());
                        }
                    }
                }
            }
        }
        if (!buttons.isEmpty()) {
            try {
                client.messages().sendText(chatId, description, null, null, null, null, null, buttons);
            } catch (IOException e) {
                log.error("Ошибка отправки кнопок: {}", e.getMessage());
            }
        } else if (!hasMessageTag) {
            sendText(chatId, "Диалог завершен. Напишите что-нибудь в чат, чтобы начать заново.");
            userStates.remove(chatId);
        }
    }
}
