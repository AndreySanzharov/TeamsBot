package org.example.NewBot;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
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
    private static final String TOKEN = "001.1031916963.1477955322:1000000106";
    private static final String HOST = "https://api.vkteams.ext.lukoil.com/";
    private static final BotApiClient client = new BotApiClient(HOST, TOKEN, 0, 60);
    private static final BotApiClientController controller = BotApiClientController.startBot(client);
    private static final String JSON_PATH = "C:\\Users\\SanzharovAA\\TeamsBot\\src\\main\\java\\org\\example\\JsonHandlers\\questions.json";

    private static final Map<String, JSONObject> userStates = new HashMap<>();

    public static void main(String[] args) {
        client.addOnEventFetchListener(events -> events.forEach(TagBot::handleEvent));
        client.start();
    }

    public static void handleEvent(Event event) {
        switch (event) {
            case NewMessageEvent message -> {
                String chatId = message.getChat().getChatId();
                try {
                    JSONObject root = loadJson();
                    if (!userStates.containsKey(message.getChat().getChatId())){
                        sendQuestionWithButtons(chatId, root);
                        userStates.put(chatId, root);
                    }
                } catch (Exception e) {
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
                    throw new RuntimeException(e);
                }
            }

            default -> throw new IllegalStateException("Неизвестное событие: " + event);
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
            e.printStackTrace();
        }
    }

    private static void sendQuestionWithButtons(String chatId, JSONObject node) {
        String description = node.optString("description", "Выберите действие:");
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        JSONArray options = node.optJSONArray("options");
        if (options != null) {
            for (int i = 0; i < options.length(); i++) {
                JSONObject option = options.getJSONObject(i);
                if ("button".equals(option.optString("tag"))) {
                    String text = option.optString("text");
                    buttons.add(Collections.singletonList(
                            InlineKeyboardButton.callbackButton(text, text, "primary")
                    ));
                } else if ("message".equals(option.optString("tag"))) {
                    String text = option.getString("text");
                    sendText(chatId, text);
                }
            }
        }

        try {
            client.messages().sendText(chatId, description, null, null, null, null, null, buttons);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}