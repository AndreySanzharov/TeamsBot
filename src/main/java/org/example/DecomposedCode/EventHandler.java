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
