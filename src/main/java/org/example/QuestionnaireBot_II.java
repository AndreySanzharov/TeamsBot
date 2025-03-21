package org.example;

import org.json.JSONObject;
import org.json.JSONTokener;
import ru.mail.im.botapi.BotApiClient;
import ru.mail.im.botapi.BotApiClientController;
import ru.mail.im.botapi.api.entity.SendTextRequest;
import ru.mail.im.botapi.fetcher.event.Event;
import ru.mail.im.botapi.fetcher.event.NewMessageEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QuestionnaireBot_II {
    private static final String token = "001.3509189690.2901436216:1000000011";
    private static final BotApiClient client = new BotApiClient("https://api.vkteams-test.ext.lukoil.com", token, 0, 60);
    private static final BotApiClientController controller = BotApiClientController.startBot(client);
    private static final String JSON_PATH = "src/main/java/org/example/JsonHandlers/questions.json";

    private static final Map<String, JSONObject> userStates = new ConcurrentHashMap<>(); // Храним состояние опроса для каждого пользователя
    private static final Map<String, Map<String, String>> userResponses = new ConcurrentHashMap<>(); // Храним ответы пользователей

    public static void main(String[] args) throws IOException {
        client.addOnEventFetchListener(events -> {
            for (Event event : events) {
                if (event instanceof NewMessageEvent) {
                    handleUserResponse((NewMessageEvent) event);
                }
            }
        });
        client.start();
    }

    /**
     * Обрабатывает ответ пользователя и отправляет следующий вопрос
     */
    private static void handleUserResponse(NewMessageEvent event) {
        String chatId = event.getChat().getChatId();
        String userMessage = event.getText().trim().toLowerCase();

        if (!userStates.containsKey(chatId)) {
            startNewSurvey(chatId);
            return;
        }

        JSONObject currentNode = userStates.get(chatId);
        String currentQuestion = getFirstKey(currentNode);
        JSONObject possibleAnswers = currentNode.getJSONObject(currentQuestion);

        if (!possibleAnswers.has(userMessage)) {
            sendMessage(chatId, "Ответ не распознан. Попробуйте еще раз.");
            return;
        }

        userResponses.get(chatId).put(currentQuestion, userMessage);
        Object next = possibleAnswers.get(userMessage);

        if (next instanceof String) {
            sendMessage(chatId, (String) next);
            printUserResponses(chatId);
            userResponses.remove(chatId); // Удаляем только ответы, но оставляем userStates
        } else {
            userStates.put(chatId, (JSONObject) next);
            sendQuestion(chatId, (JSONObject) next);
        }
    }

    /**
     * Запускает новый опрос для пользователя
     */
    private static void startNewSurvey(String chatId) {
        try {
            JSONObject jsonRoot = loadJson(JSON_PATH);
            userResponses.put(chatId, new ConcurrentHashMap<>()); // создаем мапу ответов для пользователя
            userStates.put(chatId, jsonRoot);
            sendQuestion(chatId, jsonRoot);
        } catch (FileNotFoundException e) {
            sendMessage(chatId, "Ошибка: JSON-файл не найден!");
        }
    }

    /**
     * Загружает JSON-файл
     */
    private static JSONObject loadJson(String filePath) throws FileNotFoundException {
        return new JSONObject(new JSONTokener(new FileInputStream(filePath)));
    }

    /**
     * Получает первый ключ JSON-объекта (название текущего вопроса)
     */
    private static String getFirstKey(JSONObject jsonObject) {
        return jsonObject.keys().next();
    }

    /**
     * Отправляет пользователю вопрос
     */
    private static void sendQuestion(String chatId, JSONObject questionNode) {
        String question = getFirstKey(questionNode);
        JSONObject answers = questionNode.getJSONObject(question);
        sendMessage(chatId, question + "\nОтветы: " + String.join(" / ", answers.keySet()));
    }

    /**
     * Отправляет сообщение пользователю
     */
    private static void sendMessage(String chatId, String text) {
        try {
            controller.sendTextMessage(new SendTextRequest().setChatId(chatId).setText(text));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Выводит в консоль ответы пользователя
     */
    private static void printUserResponses(String chatId) {
        System.out.println("Ответы пользователя (" + chatId + "):");

        Map<String, String> responses = userResponses.get(chatId);
        if (responses != null) {
            responses.forEach((question, answer) -> System.out.println(question + " -> " + answer));
        }
    }
}