package org.example.DecomposedCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.im.botapi.BotApiClient;
import ru.mail.im.botapi.BotApiClientController;

public class TagBotApplication {
    private static final String TOKEN = "001.1031916963.1477955322:1000000106";
    private static final String HOST = "https://api.vkteams.ext.lukoil.com/";
    private static final Logger log = LoggerFactory.getLogger(TagBotApplication.class);

    public static void main(String[] args) {
        log.info("Бот запускается...");
        BotApiClient client = new BotApiClient(HOST, TOKEN, 0, 60);
        BotApiClientController controller = BotApiClientController.startBot(client);
        UserStateManager stateManager = new UserStateManager(client, controller);
        EventHandler handler = new EventHandler(client, controller, stateManager);

        UserStateManager userStateManager = new UserStateManager(client, controller);
        HandlerProcessor processor = new HandlerProcessor(userStateManager);

        client.addOnEventFetchListener(events -> events.forEach(handler::handleEvent));
        client.start();
        log.info("Бот запустился.");
    }
}