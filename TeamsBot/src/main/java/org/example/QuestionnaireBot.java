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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QuestionnaireBot {
    private static final String token = "001.1031916963.1477955322:1000000106";
    private static final BotApiClient client = new BotApiClient("https://api.vkteams.ext.lukoil.com/", token, 0, 60);
    private static final BotApiClientController controller = BotApiClientController.startBot(client);
    private static final String JSON_PATH = "src/main/java/org/example/JsonHandlers/questions.json";

    private static final Map<String, JSONObject> userStates =  new ConcurrentHashMap<>();; // Состояние каждого пользователя
    private static final Map<String, Map<String, String>> userResponses =  new ConcurrentHashMap<>();; // Состояние каждого пользователя

    public static void main(String[] args) throws IOException {
        client.addOnEventFetchListener(events -> {
            for (Event event : events) {
                if (event instanceof NewMessageEvent) {
                    NewMessageEvent newMessage = (NewMessageEvent) event;
                    getMessageLogs((NewMessageEvent) event);
                    handleUserResponse(newMessage);
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
            // Если пользователь новый — начинаем опрос
            try {
                JSONObject jsonRoot = loadJson(JSON_PATH);
                userResponses.put(chatId, new HashMap<>()); // новая мапа для пользователя
                userStates.put(chatId, jsonRoot);
                sendQuestion(chatId, jsonRoot);
            } catch (FileNotFoundException e) {
                sendMessage(chatId, "Ошибка: JSON-файл не найден!");
            }
            return;
        }

        // Получаем текущий узел JSON
        JSONObject currentNode = userStates.get(chatId);
        String currentQuestion = currentNode.keys().next();
        JSONObject possibleAnswers = currentNode.getJSONObject(currentQuestion);

        if (!possibleAnswers.has(userMessage)) {
            sendMessage(chatId, "Ответ не распознан. Попробуйте еще раз.");
            return;
        }

        userResponses.get(chatId).put(currentQuestion, userMessage);

        Object next = possibleAnswers.get(userMessage);

        if (next instanceof String) {
            sendMessage(chatId, (String) next);
            printUserResponse(chatId);
            userStates.remove(chatId); // Завершаем опрос
            userResponses.remove(chatId);
        } else {
            userStates.put(chatId, (JSONObject) next);
            sendQuestion(chatId, (JSONObject) next);
        }
    }

    /**
     * Загружает JSON-файл
     */
    private static JSONObject loadJson(String filePath) throws FileNotFoundException {
        return new JSONObject(new JSONTokener(new FileInputStream(filePath)));
    }

    /**
     * Отправляет пользователю вопрос
     */
    private static void sendQuestion(String chatId, JSONObject questionNode) {
        String question = questionNode.keys().next();
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

    private static void printUserResponse(String chatId){
        System.out.println("*************************************");
        System.out.println("Ответы пользователя (" + chatId + "):");

        Map<String, String> responses = userResponses.get(chatId);

        if(responses != null){
            for (Map.Entry<String, String> entry : responses.entrySet()){
                System.out.println(entry.getKey() + " -> " + entry.getValue());
            }
            sendMessage(chatId, userResponses.toString());
            userResponses.remove(chatId);
            System.out.println("*************************************");
        }
    }

    private static void getMessageLogs(NewMessageEvent newMessage) {
        System.out.println("=========================");
        System.out.println("chat Id: " + newMessage.getChat().getChatId());
        System.out.println("message id: " + newMessage.getMessageId());
        System.out.println("text: " + newMessage.getText());
        System.out.println("chat: " + newMessage.getChat());
        System.out.println("from: " + newMessage.getFrom());
        System.out.println("------------------------");
    }

}
