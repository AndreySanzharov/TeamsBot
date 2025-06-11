package org.example.TagBot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.im.botapi.BotApiClient;
import ru.mail.im.botapi.BotApiClientController;
import ru.mail.im.botapi.fetcher.event.Event;
import java.io.IOException;
import java.sql.SQLException;

public class TagBotApplication {
//    private static final String TOKEN = "001.1031916963.1477955322:1000000106";
//    private static final String HOST = "https://api.vkteams.ext.lukoil.com/";
    private static final Logger log = LoggerFactory.getLogger(TagBotApplication.class);
    private static final DbManager dbManager = new DbManager();

    public static void main(String[] args) throws SQLException, IOException {
        log.info("Бот запускается...");
        dbManager.getConfigParams();
        BotApiClient client = new BotApiClient(dbManager.getToken(), dbManager.getHost(), 0, 60);
        BotApiClientController controller = BotApiClientController.startBot(client);
        UserStateManager stateManager = new UserStateManager(client, controller);
        EventHandler handler = new EventHandler(client, controller, stateManager);
        client.addOnEventFetchListener(events -> {
            for (Event event : events) {
                try {
                    handler.handleEvent(event);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        client.start();
        log.info("Бот запустился.");
    }
}

DROP TABLE IF EXISTS users;

CREATE TABLE users(
  id INT PRIMARY KEY AUTO_INCREMENT,
  chatId VARCHAR(100) NOT NULL,
  answer VARCHAR(100) NOT NULL
);

CREATE TABLE config (
    id INT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(255) NOT NULL,
    host VARCHAR(255) NOT NULL,
);

INSERT INTO config (token, host)
     VALUES ("001.1031916963.1477955322:1000000106", "https://api.vkteams.ext.lukoil.com/");

