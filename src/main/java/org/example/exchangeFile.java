// TagBotApplication.java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagBotApplication {
    private static final Logger log = LoggerFactory.getLogger(TagBotApplication.class);

    public static void main(String[] args) {
        log.info("Запуск бота...");
        BotService botService = new BotService();
        botService.start();
        log.info("Бот успешно запущен");
    }
}

// BotService.java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotService {
    private static final Logger log = LoggerFactory.getLogger(BotService.class);
    private static final String TOKEN = "001.1031916963.1477955322:1000000106";
    private static final String HOST = "https://api.vkteams.ext.lukoil.com/";

    private final BotApiClient client;
    private final BotApiClientController controller;
    private final EventHandler eventHandler;

    public BotService() {
        this.client = new BotApiClient(HOST, TOKEN, 0, 60);
        this.controller = BotApiClientController.startBot(client);
        MessageService messageService = new MessageService(controller);
        JsonFlowService jsonFlowService = new JsonFlowService(messageService);
        this.eventHandler = new EventHandler(client, jsonFlowService, messageService);
        log.info("BotService инициализирован");
    }

    public void start() {
        log.info("Бот начинает прослушивать события");
        client.addOnEventFetchListener(events -> events.forEach(eventHandler::handleEvent));
        client.start();
    }
}

// EventHandler.java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventHandler {
    private static final Logger log = LoggerFactory.getLogger(EventHandler.class);

    private final BotApiClient client;
    private final JsonFlowService jsonFlowService;
    private final MessageService messageService;

    public EventHandler(BotApiClient client, JsonFlowService jsonFlowService, MessageService messageService) {
        this.client = client;
        this.jsonFlowService = jsonFlowService;
        this.messageService = messageService;
    }

    public void handleEvent(Event event) {
        log.debug("Обработка события: {}", event);
        switch (event) {
            case NewMessageEvent message -> jsonFlowService.handleMessage(message);
            case CallbackQueryEvent callback -> jsonFlowService.handleCallback(callback);
            default -> {
                log.error("Неизвестное событие: {}", event);
                throw new IllegalStateException("Неизвестное событие: " + event);
            }
        }
    }
}

// JsonFlowService.java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonFlowService {
    private static final Logger log = LoggerFactory.getLogger(JsonFlowService.class);

    private final MessageService messageService;
    private final UserStateService stateService = new UserStateService();

    public JsonFlowService(MessageService messageService) {
        this.messageService = messageService;
    }

    public void handleMessage(NewMessageEvent message) {
        String chatId = message.getChat().getChatId();
        log.info("Получено сообщение от {}: {}", chatId, message.getText());

        if (stateService.isWaitingForInput(chatId)) {
            stateService.setWaitingForInput(chatId, false);
            messageService.sendText(chatId, "Вы ввели: " + message.getText());
            sendRootQuestion(chatId);
        } else if (!stateService.hasState(chatId)) {
            sendRootQuestion(chatId);
        }
    }

    public void handleCallback(CallbackQueryEvent callback) {
        String chatId = callback.getFrom().getUserId();
        String queryId = callback.getQueryId();
        String data = callback.getCallbackData();

        log.info("Обработка callback от {}: {}", chatId, data);

        JSONObject current = stateService.getState(chatId);
        JSONArray options = current.optJSONArray("options");
        if (options == null) return;

        for (int i = 0; i < options.length(); i++) {
            JSONObject option = options.getJSONObject(i);
            if (data.equals(option.optString("text"))) {
                if (option.has("next")) {
                    JSONObject next = option.getJSONObject("next");
                    stateService.setState(chatId, next);
                    sendQuestionWithButtons(chatId, next);
                } else {
                    messageService.sendText(chatId, "Вы выбрали: " + data);
                    stateService.clearState(chatId);
                }
                break;
            }
        }

        try {
            client.messages().answerCallbackQuery(queryId, "", false, "");
        } catch (IOException e) {
            log.error("Ошибка при ответе на callback: {}", e.getMessage(), e);
        }
    }

    private void sendRootQuestion(String chatId) {
        try {
            JSONObject root = JsonLoader.loadJson();
            stateService.setState(chatId, root);
            sendQuestionWithButtons(chatId, root);
        } catch (IOException e) {
            log.error("Ошибка загрузки сценария: {}", e.getMessage(), e);
            messageService.sendText(chatId, "Ошибка загрузки сценария: " + e.getMessage());
        }
    }

    private void sendQuestionWithButtons(String chatId, JSONObject node) {
        String description = node.optString("description", "Выберите действие:");
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        JSONArray options = node.optJSONArray("options");
        if (options != null) {
            for (int i = 0; i < options.length(); i++) {
                JSONObject option = options.getJSONObject(i);
                String tag = option.optString("tag");
                switch (tag) {
                    case "button" -> {
                        String text = option.optString("text");
                        buttons.add(Collections.singletonList(
                            InlineKeyboardButton.callbackButton(text, text, "primary")));
                    }
                    case "message" -> {
                        String text = option.optString("text");
                        messageService.sendText(chatId, text);
                        stateService.setWaitingForInput(chatId, true);
                    }
                    case "stop" -> {
                        messageService.sendText(chatId, "Составление заявки отменено");
                        sendRootQuestion(chatId);
                    }
                    default -> {
                        log.warn("Неизвестный тэг в JSON: {}", tag);
                        messageService.sendText(chatId, "Неизвестный тэг в JSON: " + tag);
                    }
                }
            }
        }

        messageService.sendButtons(chatId, description, buttons);
    }
}

// MessageService.java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageService {
    private static final Logger log = LoggerFactory.getLogger(MessageService.class);
    private final BotApiClientController controller;

    public MessageService(BotApiClientController controller) {
        this.controller = controller;
    }

    public void sendText(String chatId, String text) {
        try {
            controller.sendTextMessage(new SendTextRequest().setChatId(chatId).setText(text));
            log.debug("Отправлено сообщение: {} -> {}", chatId, text);
        } catch (IOException e) {
            log.error("Ошибка при отправке сообщения: {}", e.getMessage(), e);
        }
    }

    public void sendButtons(String chatId, String text, List<List<InlineKeyboardButton>> buttons) {
        try {
            controller.getClient().messages().sendText(chatId, text, null, null, null, null, null, buttons);
            log.debug("Отправлены кнопки: {} -> {}", chatId, text);
        } catch (IOException e) {
            log.error("Ошибка при отправке кнопок: {}", e.getMessage(), e);
        }
    }
}

// JsonLoader.java
public class JsonLoader {
    private static final String JSON_PATH = "C:/Users/SanzharovAA/TeamsBot/src/main/java/org/example/JsonHandlers/questions.json";

    public static JSONObject loadJson() throws IOException {
        try (FileInputStream fis = new FileInputStream(JSON_PATH)) {
            return new JSONObject(new JSONTokener(fis));
        }
    }
}

// UserStateService.java
public class UserStateService {
    private final Map<String, JSONObject> userStates = new HashMap<>();
    private final Set<String> waitingForInputUsers = new HashSet<>();

    public boolean hasState(String chatId) {
        return userStates.containsKey(chatId);
    }

    public JSONObject getState(String chatId) {
        return userStates.get(chatId);
    }

    public void setState(String chatId, JSONObject state) {
        userStates.put(chatId, state);
    }

    public void clearState(String chatId) {
        userStates.remove(chatId);
    }

    public boolean isWaitingForInput(String chatId) {
        return waitingForInputUsers.contains(chatId);
    }

    public void setWaitingForInput(String chatId, boolean waiting) {
        if (waiting) {
            waitingForInputUsers.add(chatId);
        } else {
            waitingForInputUsers.remove(chatId);
        }
    }
}
