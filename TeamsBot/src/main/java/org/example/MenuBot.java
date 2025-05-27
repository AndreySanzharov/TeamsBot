package org.example;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.im.botapi.BotApiClient;
import ru.mail.im.botapi.BotApiClientController;
import ru.mail.im.botapi.api.entity.SendTextRequest;
import ru.mail.im.botapi.fetcher.event.CallbackQueryEvent;
import ru.mail.im.botapi.fetcher.event.Event;
import ru.mail.im.botapi.fetcher.event.NewMessageEvent;
import ru.mail.im.botapi.api.entity.InlineKeyboardButton;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MenuBot {
    private static final String token = "001.1031916963.1477955322:1000000106";
    private static final BotApiClient client = new BotApiClient("https://api.vkteams.ext.lukoil.com/", token, 0, 60);
    private static final BotApiClientController controller = BotApiClientController.startBot(client);
    private static final String JSON_PATH = "src/main/java/org/example/JsonHandlers/questions.json";

    private static final Map<String, JSONObject> userStates = new ConcurrentHashMap<>(); // Состояние каждого пользователя
    private static final Map<String, Map<String, String>> userResponses = new ConcurrentHashMap<>(); // Ответы пользователей
    private static final Logger log = LoggerFactory.getLogger(MenuBot.class);

    public static void main(String[] args) throws IOException {
        client.addOnEventFetchListener(events -> {
            for (Event event : events) {
                System.out.println(event.getType());
                log.info(event.getType());
                if (event instanceof NewMessageEvent) {
                    NewMessageEvent newMessage = (NewMessageEvent) event;

                    log.info(String.valueOf(newMessage.getChat().getChatId()));
                    log.info(String.valueOf(newMessage.getChat().getType()));
                    log.info(String.valueOf(newMessage.getFrom()));
                    log.info(String.valueOf(newMessage.getParts()));

                    handleUserResponse(newMessage);
                } else if (event instanceof CallbackQueryEvent) {
                    try {
                        handleCallback((CallbackQueryEvent) event);
                        log.info(event.getType());
                        log.info(((CallbackQueryEvent) event).getQueryId());
                        log.info(((CallbackQueryEvent) event).getMessageText());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        });
        client.start();
    }

    private static void handleUserResponse(NewMessageEvent event) {
        String chatId = event.getChat().getChatId();
        if (!userStates.containsKey(chatId)) {
            try {
                JSONObject jsonRoot = loadJson(JSON_PATH);
                userResponses.put(chatId, new HashMap<>());
                userStates.put(chatId, jsonRoot);
                sendQuestionWithButtons(chatId, jsonRoot);
            } catch (FileNotFoundException e) {
                log.error("JSON не найден");
                sendMessage(chatId, "Ошибка: JSON-файл не найден!");
            }
        }
    }

    private static void handleCallback(CallbackQueryEvent event) throws IOException {
        String chatId = event.getFrom().getUserId();
        String queryID = event.getQueryId();
        String callbackData = event.getCallbackData();
        JSONObject currentNode = userStates.get(chatId);

        if (currentNode != null) {
            String currentQuestion = currentNode.keys().next();
            JSONObject possibleAnswers = currentNode.getJSONObject(currentQuestion);

            if (possibleAnswers.has(callbackData)) {
                userResponses.get(chatId).put(currentQuestion, callbackData);
                Object next = possibleAnswers.get(callbackData);

                if (next instanceof String) {
                    sendMessage(chatId, (String) next);
                    saveUserResponse(chatId);
                    userStates.remove(chatId);
                    userResponses.remove(chatId);
                } else {
                    userStates.put(chatId, (JSONObject) next);
                    sendQuestionWithButtons(chatId, (JSONObject) next);
                }
            }
        }
        client.messages().answerCallbackQuery(queryID, "", false, "");
    }

    private static JSONObject loadJson(String filePath) throws FileNotFoundException {
        return new JSONObject(new JSONTokener(new FileInputStream(filePath)));
    }

    private static void sendQuestionWithButtons(String chatId, JSONObject questionNode) {
        String question = questionNode.keys().next();
        JSONObject answers = questionNode.getJSONObject(question);
        List<List<InlineKeyboardButton>> buttonList = new ArrayList<>();
        for (String answer : answers.keySet()) {
            buttonList.add(Collections.singletonList(InlineKeyboardButton.callbackButton(answer, answer, "primary")));
        }

        try {
            client.messages().sendText(chatId, question, null, null, null, null, null, buttonList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessage(String chatId, String text) {
        try {
            controller.sendTextMessage(new SendTextRequest().setChatId(chatId).setText(text));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveUserResponse(String chatId) {
        System.out.println("Ответы пользователя (" + chatId + "):");

        Map<String, String> responses = userResponses.get(chatId);
        Map<String, Map<String, String>> savedAnswers = new LinkedHashMap<>();

        if (responses != null) {
            responses.forEach((key, value) -> System.out.println(key + " -> " + value));
            savedAnswers.put(chatId, responses);
        }
        System.out.println(userResponses.get(chatId));
        sendMessage(chatId, userResponses.get(chatId).toString());
    }
}