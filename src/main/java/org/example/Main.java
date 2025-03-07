package org.example;

import javazoom.jl.decoder.JavaLayerException;
import org.example.Audio.DecoderDemo;
import org.example.Audio.MP3Formatter;
import org.example.Audio.MP3ToWavConverterJlayer;
import org.example.Downloaders.URLFileDownloader;
import ru.mail.im.botapi.BotApiClient;
import ru.mail.im.botapi.BotApiClientController;
import ru.mail.im.botapi.api.entity.InlineKeyboardButton;
import ru.mail.im.botapi.api.entity.SendTextRequest;
import ru.mail.im.botapi.fetcher.event.CallbackQueryEvent;
import ru.mail.im.botapi.fetcher.event.Event;
import ru.mail.im.botapi.fetcher.event.NewMessageEvent;
import ru.mail.im.botapi.fetcher.event.parts.File;
import ru.mail.im.botapi.fetcher.event.parts.Voice;
import ru.mail.im.botapi.response.ApiResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String token = "001.3509189690.2901436216:1000000011";
    private static final BotApiClient client = new BotApiClient("https://api.vkteams-test.ext.lukoil.com", token, 0, 60);
    private static final BotApiClientController controller = BotApiClientController.startBot(client);
    private static final String SAVE_PATH = "C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\";
    private static final URLFileDownloader urlFileDownloader = new URLFileDownloader();
    private static final MP3Formatter mp3Formatter = new MP3Formatter();
    private static final MP3ToWavConverterJlayer mp3ToWavConverterJlayer = new MP3ToWavConverterJlayer();
    private static final DecoderDemo decoderDemo = new DecoderDemo();
    private static String chatId = "";
    private static Long messageId;

    public static void main(String[] args) throws IOException {
        client.addOnEventFetchListener(events -> {
            for (Event event : events) {
                System.out.println("event type: " + event.getType());

                switch (event.getType()) {
                    case "newMessage": {
                        NewMessageEvent newMessage = (NewMessageEvent) event;
                        chatId = newMessage.getChat().getChatId();
                        messageId = newMessage.getMessageId();
                        getMessageLogs(newMessage);
                        if (newMessage.getParts() != null) {
                            handleFile(newMessage);
                        }
                        try {
                            sendMenu(newMessage);
                        } catch (IOException e) {
                            e.getStackTrace();
                        }
                        break;
                    }
                    case "callbackQuery": {
                        System.out.println("callbackQuery");
                        CallbackQueryEvent callbackQueryEvent = (CallbackQueryEvent) event;
                        try {
                            ApiResponse responce = client.messages().answerCallbackQuery(callbackQueryEvent.getQueryId(), "Accept", false, "");
                            if (callbackQueryEvent.getCallbackData().equals("sendFileToUser")) {
//                                sendFileToUser();
                            }
                            if (callbackQueryEvent.getCallbackData().equals("sendFileToBot")) {
                                controller.sendTextMessage(new SendTextRequest().setChatId(chatId).setText("Можете прислать мне файл в чат")).getMsgId();
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    }
                    case "deletedMessage": {
                        System.out.println("deleted");
                        break;
                    }
                    default: {
                        System.out.println("default reaction");
                        break;
                    }
                }

            }
            client.start();
        });
    }

    static void getMessageLogs(NewMessageEvent newMessage) {
        System.out.println("=========================");
        System.out.println("chat Id: " + newMessage.getChat().getChatId());
        System.out.println("message id: " + newMessage.getMessageId());
        System.out.println("text: " + newMessage.getText());
        System.out.println("chat: " + newMessage.getChat());
        System.out.println("from: " + newMessage.getFrom());
        System.out.println("parts: " + newMessage.getParts());
        System.out.println("------------------------");
    }


    static void sendMenu(NewMessageEvent newMessage) throws IOException {
        if (newMessage.getText().equals("Меню")) {
            List<List<InlineKeyboardButton>> buttonList = new ArrayList<>();
            List<InlineKeyboardButton> buttonSubList1 = new ArrayList<>();
            buttonSubList1.add(InlineKeyboardButton.callbackButton("Отправить файл", "sendFileToBot", "primary"));
            buttonSubList1.add(InlineKeyboardButton.callbackButton("Получить файл", "sendFileToUser", "primary"));
            buttonList.add(buttonSubList1);
            client.messages().sendText(chatId, "Выберите операцию", null, null, null, null, null, buttonList);
        }
    }


    static void handleFile(NewMessageEvent newMessageEvent) {
        final String baseUrl = "https://api.vkteams-test.ext.lukoil.com/bot/v1/files/getInfo/?token=001.3509189690.2901436216:1000000011&fileId=";
        File filePart = null;
        Voice voicePart = null;
        String tokenFile = "";
        try {
            filePart = (File) newMessageEvent.getParts().getFirst();
            tokenFile = filePart.getFileId();
            System.out.println("token: " + tokenFile);
            String fileUrl = baseUrl + tokenFile;
            System.out.println("full url: " + fileUrl);
            urlFileDownloader.setFilename("inputFile.txt");
            urlFileDownloader.downloadFile(urlFileDownloader.getGson(fileUrl), SAVE_PATH);

        } catch (Exception exception) {
            voicePart = (Voice) newMessageEvent.getParts().getFirst();
            tokenFile = voicePart.getFileId();
            System.out.println("token: " + tokenFile);
            String fileUrl = baseUrl + tokenFile;
            System.out.println("full url: " + fileUrl);
            urlFileDownloader.setFilename("input.mp3");
            urlFileDownloader.downloadFile(urlFileDownloader.getGson(fileUrl), SAVE_PATH);
            try {
                MP3Formatter.formatMp3("C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\input.mp3");
                mp3ToWavConverterJlayer.convertMp3ToWav();
                decoderDemo.decodeWav();
            } catch (JavaLayerException javaLayerException) {
                exception.getStackTrace();
            }

        }


    }
}