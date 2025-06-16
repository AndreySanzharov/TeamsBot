package org.example.TagBot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;

public class DbManager {
    private final Logger log = LoggerFactory.getLogger(DbManager.class);
    private final String jdbcUrl = "jdbc:h2:file:./data/testdb;DB_CLOSE_DELAY=-1";
    private final String user = "sa";
    private final String password = "";

    private String host = "";
    private String token = "";


    public void saveUserAnswer(String chatId, String answer) throws SQLException, IOException {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             Statement stmt = conn.createStatement()) {
            String schemaSql = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("C:\\Users\\SanzharovAA\\TeamsBot\\src\\main\\resources\\schema.sql")));
            stmt.execute(schemaSql);
            try (PreparedStatement pstmt = conn.prepareStatement(Queries.saveAnswersQuery)) {
                pstmt.setString(1, chatId);
                pstmt.setString(2, answer);
                pstmt.executeUpdate();
            }

            // Чтение и вывод
            ResultSet rs = stmt.executeQuery(Queries.printDBQuery);
            while (rs.next()) {
                System.out.printf("%d, %s, %s%n",
                        rs.getInt("id"),
                        rs.getString("chatId"),
                        rs.getString("answer"));
            }
        }
    }

    private void initDatabase() throws SQLException, IOException {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             Statement stmt = conn.createStatement()) {
            String shemaSql = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/main/resources/schema.sql")));
            stmt.execute(shemaSql);
        }
    }

    public void getConfigParams() throws SQLException, IOException {
        initDatabase();
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(Queries.getConfigQuery)) {

            while (rs.next()) {
                String key = rs.getString("config_key").trim();
                String value = rs.getString("config_value").trim();
                switch (key) {
                    case "token" -> token = value;
                    case "host" -> host = value;
                    default -> log.warn("Неизвестный параметр конфигурации: {}", key);
                }
            }

            log.info("Конфигурация получена: token='{}', host='{}'", token, host);

        } catch (SQLException e) {
            log.error("Ошибка при получении конфигурации: {}", e.getMessage(), e);
        }
    }

    public String getHost() {
        return host;
    }

    public String getToken() {
        return token;
    }
}
package org.example.TagBot;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.im.botapi.BotApiClient;
import ru.mail.im.botapi.BotApiClientController;
import ru.mail.im.botapi.fetcher.event.CallbackQueryEvent;
import ru.mail.im.botapi.fetcher.event.Event;
import ru.mail.im.botapi.fetcher.event.NewMessageEvent;

import java.io.IOException;
import java.sql.SQLException;

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

    public void handleEvent(Event event) throws SQLException, IOException {
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

    private void handleCallbackEvent(CallbackQueryEvent callback) throws SQLException, IOException {
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
package org.example.TagBot;

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
            Set<String> usersList = Set.of("Иванов Иван Иванович", "Петров Петр Петрович"); // пример имен. Надо удалить
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
package org.example.TagBot;

public interface Queries {
    String saveAnswersQuery = "INSERT INTO users (chatId, answer) VALUES (?, ?)";
    String printDBQuery = "SELECT * FROM users";
    String getConfigQuery = "SELECT config_key, config_value FROM config";
}
package org.example.TagBot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.im.botapi.BotApiClient;
import ru.mail.im.botapi.BotApiClientController;
import ru.mail.im.botapi.fetcher.event.Event;
import java.io.IOException;
import java.sql.SQLException;

public class TagBotApplication {
//    private static final String TOKEN = "001.1031916963.1477955322:1000000106";
//    private static final String HOST = "https://api.vkteams.ext.lukoil.com/";
    private static final Logger log = LoggerFactory.getLogger(TagBotApplication.class);
    private static final DbManager dbManager = new DbManager();

    public static void main(String[] args) throws SQLException, IOException {
        log.info("Бот запускается...");

        dbManager.getConfigParams();
        log.info("DB HOST {}:", dbManager.getHost());
        log.info("DB TOKEN {}:", dbManager.getToken());

        BotApiClient client = new BotApiClient(dbManager.getHost(), dbManager.getToken(), 0, 60);
        BotApiClientController controller = BotApiClientController.startBot(client);
        UserStateManager stateManager = new UserStateManager(client, controller);
        EventHandler handler = new EventHandler(client, controller, stateManager);
        client.addOnEventFetchListener(events -> {
            for (Event event : events) {
                try {
                    handler.handleEvent(event);
                } catch (SQLException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        client.start();

         log.info("Бот запустился.");
    }
}
package org.example.TagBot;

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
import java.sql.SQLException;
import java.util.*;

class UserStateManager {
    private static final String JSON_PATH = "C:\\Users\\SanzharovAA\\TeamsBot\\src\\main\\resources\\questions.json";
    private final Map<String, JSONObject> userStates = new HashMap<>();
    //private static final Map<String, List<String>> userAnswers = new HashMap<>();
    private final Set<String> waitingForInput = new HashSet<>();
    private static final Logger log = LoggerFactory.getLogger(UserStateManager.class);
    private final BotApiClient client;
    private final BotApiClientController controller;
    private final DbManager dbManager = new DbManager();

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

    public void processTextMessage(String chatId, NewMessageEvent message) throws IOException, SQLException {
        if (waitingForInput.contains(chatId)) {
            waitingForInput.remove(chatId);
            String userInput = message.getText();
            log.info("Пользователь написал сообщение: {}", userInput);
            JSONObject current = userStates.get(chatId);

            //saveUserAnswer(chatId, userInput);
            dbManager.saveUserAnswer(chatId, userInput);
            sendText(chatId, "Вы ввели: " + userInput);

            if (current != null && current.has("handler")) {
                String handler = current.getString("handler");
                if (!HandlerProcessor.processHandler(handler, userInput, chatId)) { // удалить последний параметр
                    waitingForInput.add(chatId);
                    return;
                }
            }

            if (current != null && current.has("next")) {
                JSONObject next = current.getJSONObject("next");
                userStates.put(chatId, next);
                sendQuestionWithButtons(chatId, next);
            } else {
                //getUserAnswers(chatId);
                sendText(chatId, "Диалог завершен. Напишите что-нибудь в чат, чтобы начать заново.");
                //userAnswers.remove(chatId);
                userStates.remove(chatId);
            }
        }
    }

    public void processCallback(String chatId, String data) throws SQLException, IOException {
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
                dbManager.saveUserAnswer(chatId, data);
                //saveUserAnswer(chatId, data);
                if (option.has("next")) {
                    JSONObject next = option.getJSONObject("next");
                    log.debug("Переход к следующему узлу для пользователя: {}", chatId);
                    userStates.put(chatId, next);
                    sendQuestionWithButtons(chatId, next);
                } else {
                    sendText(chatId, "Вы выбрали: " + data);
                    //getUserAnswers(chatId);
                    sendText(chatId, "Диалог завершен. Напишите что-нибудь в чат, чтобы начать заново.");
                    log.info("Диалог завершен для пользователя: {}", chatId);
                    //userAnswers.remove(chatId);
                    userStates.remove(chatId);
                }
                break;
            }
        }
    }


//    public void saveUserAnswer(String chatId, String answer) {
//        userAnswers.computeIfAbsent(chatId, k -> new ArrayList<>()).add(answer);
//        log.debug("Ответ пользователя {}: {}", chatId, answer);
//    }

//    public  Map<String, List<String>> getUserAnswers(String chatId) {
//        sendText(chatId, "Ответы пользователя: " + chatId + "\n" + String.join("\n ", userAnswers.getOrDefault(chatId, Collections.emptyList())));
//        return userAnswers;
//    }

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
