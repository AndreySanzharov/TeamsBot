package org.example.TagBot;

import com.bmc.arsys.api.ARException;
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
    private static final String JSON_PATH = "C:\\Users\\SanzharovAA\\IdeaProjects\\TeamsBot\\src\\main\\resources\\questions.json";
    private final Map<String, JSONObject> userStates = new HashMap<>();
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

    public void processTextMessage(String chatId, NewMessageEvent message) throws IOException, SQLException, ARException {
        if (waitingForInput.contains(chatId)) {
            waitingForInput.remove(chatId);
            String userInput = message.getText();
            log.info("Пользователь написал сообщение: {}", userInput);
            JSONObject current = userStates.get(chatId);
            dbManager.saveUserAnswer(chatId, userInput);
            sendText(chatId, "Вы ввели: " + userInput);

            if (current != null && current.has("handler")) {
                String handler = current.getString("handler");
                if (!HandlerProcessor.processHandler(handler, userInput, chatId)) {
                    waitingForInput.add(chatId);
                    return;
                }
            }

            if (current != null && current.has("next")) {
                JSONObject next = current.getJSONObject("next");
                userStates.put(chatId, next);
                sendQuestionWithButtons(chatId, next);
            } else {
                sendText(chatId, "Диалог завершен. Напишите что-нибудь в чат, чтобы начать заново.");
                userStates.remove(chatId);
            }
        }
    }

    public void processCallback(String chatId, String data) throws SQLException, IOException, ARException {
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
                if (option.has("next")) {
                    JSONObject next = option.getJSONObject("next");
                    log.debug("Переход к следующему узлу для пользователя: {}", chatId);
                    userStates.put(chatId, next);
                    sendQuestionWithButtons(chatId, next);
                } else {
                    sendText(chatId, "Вы выбрали: " + data);
                    sendText(chatId, "Диалог завершен. Напишите что-нибудь в чат, чтобы начать заново.");
                    log.info("Диалог завершен для пользователя: {}", chatId);
                    userStates.remove(chatId);
                }
                break;
            }
        }
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
package org.example.TagBot;

import com.bmc.arsys.api.ARException;
import org.example.ARS.ARSManager;
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
    ARSManager arsManager = new ARSManager();

    public EventHandler(BotApiClient client, BotApiClientController controller, UserStateManager stateManager) {
        this.client = client;
        this.controller = controller;
        this.stateManager = stateManager;
    }

    public void handleEvent(Event event) throws SQLException, IOException, ARException {
        switch (event) {
            case NewMessageEvent message -> handleMessageEvent(message);
            case CallbackQueryEvent callback -> handleCallbackEvent(callback);
            default -> log.warn("Неизвестный тип события: {}", event.getType());
        }
    }

    private void handleMessageEvent(NewMessageEvent message) throws ARException {
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

    private void handleCallbackEvent(CallbackQueryEvent callback) throws SQLException, IOException, ARException {
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
package org.example.ARS;

import com.bmc.arsys.api.*;

import java.util.ArrayList;
import java.util.List;

public class ARSManager {

    private ARServerUser server;

    public ARSManager() {
        server = new ARServerUser();
    }

    public void connect(String serverName, String userName, String userPassword) throws ARException {
        server.setPort(35000);
        server.setServer(serverName);
        server.setUser(userName);
        server.setPassword(userPassword);
        server.verifyUser();
    }

    public boolean userIsValid(String chatId) throws ARException {
        return getUser(chatId) != null;
    }

    public String createIncident(String submitter, String status, String description, String information, String firstName, String lastName, String middleName, String castCompany, String category) throws ARException {
        String formName = "HPD:IncidentInterface_Create";
        Entry entry = new Entry();

        entry.put(Constants.AR_CORE_SUBMITTER, new Value(submitter));
        entry.put(Constants.AR_CORE_STATUS, new Value(status, DataType.ENUM));
        entry.put(1000000076, new Value("CREATE")); // action
        entry.put(1000000019, new Value(firstName)); // first name
        entry.put(1000000018, new Value(lastName)); // last name
        entry.put(1000000020, new Value(middleName)); // middle name
        entry.put(1000000082, new Value(castCompany)); // company
        entry.put(1000000163, new Value("3000", DataType.ENUM)); // impact
        entry.put(1000000162, new Value("3000", DataType.ENUM)); // urgency
        entry.put(1000000000, new Value(description)); // description
        entry.put(1000000151, new Value(information)); // detailed description
        entry.put(1000000215, new Value("10000", DataType.ENUM)); // reported source

        int choice = switch (category) {
            case "Инцидент" -> 0;
            case "Инцидент мониторинга" -> 2;
            case "Событие мониторинга" -> 3;
            case "Жалоба" -> 4;
            default -> 1;
        };

        entry.put(1000000099, new Value(choice, DataType.ENUM)); // service type

        String entryIdOut = server.createEntry(formName, entry);
        Entry incEntry = server.getEntry(formName, entryIdOut, null);
        Value incVal = incEntry.get(1000000161); // Incident Number
        return incVal.toString();
    }

    public void cleanup() {
        server.logout();
    }


    private ARSUser getUser(String qualStr) throws ARException {
        ARSUser user = new ARSUser();

        List<Field> fields = server.getListFieldObjects("CTM:People");
// Create the search qualifier.
        QualifierInfo qual = server.parseQualification(qualStr, fields, null, Constants.AR_QUALCONTEXT_DEFAULT);

        int[] fieldIds = {1000000048, 1, 4, 179, 1000000054, 1000000018, 1000000019, 1000000020, 1000000023, 1000000001, 1000000010, 200000006, 1000000056, 1000000035};
        OutputInteger nMatches = new OutputInteger();
        List<SortInfo> sortOrder = new ArrayList<>();
        sortOrder.add(new SortInfo(1000000048, Constants.AR_SORT_DESCENDING));
// Retrieve entries from the form using the given qualification.
        List<Entry> entryList = server.getListEntryObjects("CTM:People", qual, 0, Constants.AR_NO_MAX_LIST_RETRIEVE, sortOrder, fieldIds, true, nMatches);

        System.out.println("Query returned " + nMatches + " matches.");
        System.out.println("nMatches.intValue " + nMatches.intValue());
        if (nMatches.intValue() == 1) {
//requestNumder = entryList.get(0).get(301572100).toString();
//            user.setPersonID(entryList.get(0).get(1).toString());
//            user.setCorporateID(entryList.get(0).get(1000000054).toString());
//            user.setRemedyLoginID(entryList.get(0).get(4).toString());
//            user.setInstanceID(entryList.get(0).get(179).toString());
            user.setLastName(entryList.get(0).get(1000000018).toString());
            user.setFirstName(entryList.get(0).get(1000000019).toString());
            user.setMiddleName(entryList.get(0).get(1000000020).toString());
           // user.setOrganization(entryList.get(0).get(1000000010).toString());

//            user.setJobTitle(entryList.get(0).get(1000000023).toString());
//            user.setEmail(entryList.get(0).get(1000000048).toString());
//
            user.setCompany(entryList.get(0).get(1000000001).toString());

//            user.setDepartment(entryList.get(0).get(200000006).toString());
//
//            user.setPhone(entryList.get(0).get(1000000056).toString());
//            user.setPlace(entryList.get(0).get(1000000035).toString());
//
//            user.setValid(true);

            System.out.println("first name " + user.getFirstName());
            System.out.println("last name " + user.getLastName());
            System.out.println("middle name " + user.getMiddleName());
            System.out.println("organization " + user.getCompany());
            return user;
        } else {
            System.out.println("Пользователь не найден");
            return null;
        }
    }


    public ARSUser getUserInfo(String email) throws ARException {
        String qualStr = "'Internet E-mail'=\"" + email + "\"AND 'Profile Status' =\"Enabled\"";
        System.out.println("Qual: " + qualStr);
        return getUser(qualStr);
    }


    public ARSUser getUserInfo(String company, String lastName, String firstName, String middleName) throws ARException {

        String qualStr = "'Last Name' =\"" + lastName + "\"AND 'First Name' =\"" + firstName + "\"AND 'Middle Initial' =\"" + middleName + "\"AND 'Company' =\"" + company.replaceAll("\"", "\"\"") + "\"";

        System.out.println("Qual: " + qualStr);

        return getUser(qualStr);
    }


}

