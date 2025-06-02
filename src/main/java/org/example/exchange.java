package org.example;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class TagBot {
    private static final String TOKEN = "001.1031916963.1477955322:1000000106";
    private static final String HOST = "https://api.vkteams.ext.lukoil.com/";
    private static final BotApiClient client = new BotApiClient(HOST, TOKEN, 0, 60);
    private static final BotApiClientController controller = BotApiClientController.startBot(client);
    private static final String JSON_PATH = "C:\\Users\\SanzharovAA\\TeamsBot\\src\\main\\resources\\questions.json";
    private static final String ANSWERS_PATH = "answers.txt";

    private static final Map<String, JSONObject> userStates = new HashMap<>();
    private static final Set<String> waitingForInput = new HashSet<>();
    private static final Logger log = LoggerFactory.getLogger(TagBot.class);

    public static void main(String[] args) {
        log.info("Бот запускается...");
        client.addOnEventFetchListener(events -> events.forEach(TagBot::handleEvent));
        client.start();
    }

    public static void handleEvent(Event event) {
        switch (event) {
            case NewMessageEvent message -> {
                String chatId = message.getChat().getChatId();
                log.info("Новое сообщение от пользователя: " + chatId + " = {" + message.getText() + "}");
                try {
                    handleMessage(chatId, message);
                    if (!userStates.containsKey(chatId)) {
                        JSONObject root = loadJson();
                        userStates.put(chatId, root);
                        sendQuestionWithButtons(chatId, root);
                    }
                } catch (Exception e) {
                    log.error("Ошибка при обработке сообщения: " + e.getMessage());
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
                    log.error("Ошибка подтверждения callback: " + e.getMessage());
                }
            }
            default -> log.warn("Неизвестный тип события: " + event.getType());
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
            log.error("Ошибка отправки текста: " + e.getMessage());
        }
    }

    private static void handleMessage(String chatId, Event message) throws IOException {
        if (waitingForInput.contains(chatId)) {
            waitingForInput.remove(chatId);
            String userInput = ((NewMessageEvent) message).getText();
            saveAnswer(chatId, userInput);
            sendText(chatId, "Вы ввели: " + userInput);

            JSONObject current = userStates.get(chatId);
            if (current != null && current.has("next")) {
                JSONObject next = current.getJSONObject("next");
                userStates.put(chatId, next);
                sendQuestionWithButtons(chatId, next);
            } else {
                sendText(chatId, "Спасибо! Диалог завершён.");
                userStates.remove(chatId);
            }
        }
    }

    private static void saveAnswer(String chatId, String answer) {
        String line = String.format("Пользователь %s ввел: %s%n", chatId, answer);
        try {
            Files.writeString(Path.of(ANSWERS_PATH), line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            log.info("Ответ сохранен: " + line);
        } catch (IOException e) {
            log.error("Ошибка при записи: " + e.getMessage());
        }
    }

    private static void sendQuestionWithButtons(String chatId, JSONObject node) {
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
                            JSONObject root = loadJson();
                            userStates.put(chatId, root);
                            sendQuestionWithButtons(chatId, root);
                        } catch (IOException e) {
                            log.error("Ошибка при возврате в меню: " + e.getMessage());
                        }
                    }
                }
            }
        }

        if (!buttons.isEmpty()) {
            try {
                client.messages().sendText(chatId, description, null, null, null, null, null, buttons);
            } catch (IOException e) {
                log.error("Ошибка отправки кнопок: " + e.getMessage());
            }
        } else if (!hasMessageTag) {
            sendText(chatId, "Спасибо! Диалог завершён.");
            userStates.remove(chatId);
        }
    }
}
