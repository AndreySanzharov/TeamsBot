import ru.mail.im.botapi.fetcher.event.NewMessageEvent;
import ru.mail.im.botapi.fetcher.event.parts.File;
import ru.mail.im.botapi.fetcher.event.parts.Voice;


//скопировано из мейна
public class FileHandler {
//    static void handleFile(NewMessageEvent newMessageEvent) {
//        final String baseUrl = "https://api.vkteams.ext.lukoil.com/bot/v1/files/getInfo/?token=" + token + "&fileId=";
//        File filePart = null;
//        Voice voicePart = null;
//        String tokenFile = "";
//        try {
//            filePart = (File) newMessageEvent.getParts().getFirst();
//            tokenFile = filePart.getFileId();
//            System.out.println("token: " + tokenFile);
//            String fileUrl = baseUrl + tokenFile;
//            System.out.println("full url: " + fileUrl);
//            urlFileDownloader.setFilename("inputFile.txt");
//            urlFileDownloader.downloadFile(urlFileDownloader.getGson(fileUrl), SAVE_PATH);
//
//        } catch (Exception exception) {
//            voicePart = (Voice) newMessageEvent.getParts().getFirst();
//            tokenFile = voicePart.getFileId();
//            System.out.println("token: " + tokenFile);
//            String fileUrl = baseUrl + tokenFile;
//            System.out.println("full url: " + fileUrl);
//            //urlFileDownloader.setFilename("input.mp3");
//            urlFileDownloader.setFilename("input.mp3");
//            urlFileDownloader.downloadFile(urlFileDownloader.getGson(fileUrl), SAVE_PATH);
//            aacToWav.convert();
//            decoderDemo.decodeWav();
//        }
//    }
}
