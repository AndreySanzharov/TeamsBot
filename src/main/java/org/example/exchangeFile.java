package org.example.NewBot;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.im.botapi.BotApiClient;
import ru.mail.im.botapi.BotApiClientController;
import ru.mail.im.botapi.api.entity.InlineKeyboardButton;
import ru.mail.im.botapi.api.entity.SendTextRequest;
import ru.mail.im.botapi.fetcher.event.CallbackQueryEvent;
import ru.mail.im.botapi.fetcher.event.Event;
import ru.mail.im.botapi.fetcher.event.NewMessageEvent;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class TagBot {
    private static final Logger log = LoggerFactory.getLogger(TagBot.class);
    private static final String TOKEN = "001.1031916963.1477955322:1000000106";
    private static final String HOST = "https://api.vkteams.ext.lukoil.com/";
    private static final BotApiClient client = new BotApiClient(HOST, TOKEN, 0, 60);
    private static final BotApiClientController controller = BotApiClientController.startBot(client);
    private static final String JSON_PATH = "C:\\Users\\SanzharovAA\\TeamsBot\\src\\main\\java\\org\\example\\JsonHandlers\\questions.json";

    private static final Map<String, JSONObject> userStates = new HashMap<>();
    private static final Set<String> waitingForInputUsers = new HashSet<>();

    public static void main(String[] args) {
        log.info("Бот запущен и готов к обработке событий.");
        client.addOnEventFetchListener(events -> events.forEach(TagBot::handleEvent));
        client.start();
    }

    public static void handleEvent(Event event) {
        switch (event) {
            case NewMessageEvent message -> {
                String chatId = message.getChat().getChatId();
                try {
                    handleMessageName(chatId, message);
                    if (!userStates.containsKey(chatId)) {
                        JSONObject root = loadJson();
                        userStates.put(chatId, root);
                        sendQuestionWithButtons(chatId, root);
                    }
                } catch (Exception e) {
                    log.error("Ошибка обработки нового сообщения", e);
                    sendText(chatId, "Ошибка загрузки меню: " + e.getMessage());
                }
            }
            case CallbackQueryEvent callback -> {
                String chatId = callback.getFrom().getUserId();
                String queryID = callback.getQueryId();
                String data = callback.getCallbackData();
                JSONObject current = userStates.get(chatId);
                if (current == null) return;

                JSONArray options = current.optJSONArray("options");
                if (options == null) return;

                for (int i = 0; i < options.length(); i++) {
                    JSONObject option = options.getJSONObject(i);
                    if (option.optString("text").equals(data)) {
                        if (option.has("next")) {
                            JSONObject next = option.getJSONObject("next");
                            userStates.put(chatId, next);
                            sendQuestionWithButtons(chatId, next);
                        } else {
                            sendText(chatId, "Вы выбрали: " + data);
                            userStates.remove(chatId);
                        }
                        break;
                    }
                }
                try {
                    client.messages().answerCallbackQuery(queryID, "", false, "");
                } catch (IOException e) {
                    log.error("Ошибка при подтверждении callback-запроса", e);
                    throw new RuntimeException(e);
                }
            }
            default -> {
                log.warn("Неизвестное событие: {}", event);
                throw new IllegalStateException("Неизвестное событие: " + event);
            }
        }
    }

    private static JSONObject loadJson() throws IOException {
        try (FileInputStream fis = new FileInputStream(JSON_PATH)) {
            return new JSONObject(new JSONTokener(fis));
        }
    }

    private static void sendText(String chatId, String text) {
        try {
            controller.sendTextMessage(new SendTextRequest().setChatId(chatId).setText(text));
        } catch (IOException e) {
            log.error("Ошибка при отправке текста в чат {}: {}", chatId, text, e);
        }
    }

    public static void handleMessageName(String chatId, Event message) throws IOException {
        if (waitingForInputUsers.contains(chatId)) {
            waitingForInputUsers.remove(chatId);
            String input = ((NewMessageEvent) message).getText();

            long spaceCount = input.chars().filter(ch -> ch == ' ').count();
            if (spaceCount != 2) {
                log.info("Пользователь {} ввел имя с неверным количеством пробелов: '{}'", chatId, input);
                sendText(chatId, "Ошибка: введите строку, содержащую ровно два пробела (пример: Имя Отчество Фамилия)");
                waitingForInputUsers.add(chatId);
                return;
            }

            log.info("Пользователь {} ввел корректное имя: '{}'", chatId, input);
            sendText(chatId, "Вы ввели: " + input);

            JSONObject root = loadJson();
            userStates.put(chatId, root);
            sendQuestionWithButtons(chatId, root);
        }
    }

    private static void sendQuestionWithButtons(String chatId, JSONObject node) {
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
                                InlineKeyboardButton.callbackButton(text, text, "primary")
                        ));
                    }
                    case "messageName" -> {
                        String text = option.optString("text");
                        sendText(chatId, text);
                        waitingForInputUsers.add(chatId);
                    }
                    case "message" -> {
                        String text = option.optString("text");
                        sendText(chatId, text);
                    }
                    case "stop" -> {
                        sendText(chatId, "Составление заявки отменено");
                        try {
                            JSONObject root = loadJson();
                            userStates.put(chatId, root);
                            sendQuestionWithButtons(chatId, root);
                        } catch (IOException e) {
                            log.error("Ошибка при возврате в главное меню", e);
                            sendText(chatId, "Ошибка при возврате в главное меню: " + e.getMessage());
                        }
                    }
                    default -> {
                        log.warn("Неизвестный тэг в файле json: {}", tag);
                        sendText(chatId, "Неизвестный тэг в файле json: " + tag);
                    }
                }
            }
        }

        try {
            client.messages().sendText(chatId, description, null, null, null, null, null, buttons);
        } catch (IOException e) {
            log.error("Ошибка при отправке вопроса с кнопками", e);
        }
    }
}
