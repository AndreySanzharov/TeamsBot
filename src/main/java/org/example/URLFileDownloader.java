package org.example;

import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.example.GsonFileObject;

public class URLFileDownloader {
    public void downloadFile(GsonFileObject gsonFileObject, String savePath) {
        try {
            URL url = new URL(gsonFileObject.getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            String filename = "input.mp3";
            //String path = savePath + gsonFileObject.getFilename();
            String path = savePath + filename;
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream(); FileOutputStream outputStream = new FileOutputStream(path)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    System.out.println("Файл успешно скачан: " + savePath);
                }

            } else {
                System.err.println("Ошибка HTTP " + responseCode);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public GsonFileObject getGson(String fileURL) {
        Gson gson = new Gson();
        String readyGson = "";
        try {
            URL url = new URL(fileURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        readyGson += new String(buffer, StandardCharsets.UTF_8);
                    }
                    readyGson = readyGson.trim();
                    System.out.println(readyGson);
                }
            } else {
                System.err.println("Ошибка HTTP " + responseCode);
            }
        } catch (IOException e) {
            System.err.println("Ошибка");
        }
        return gson.fromJson(readyGson, GsonFileObject.class);
    }
//        public static void main(String[] args) {
//        String savePath = "C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\";
//        String url = "https://api.vkteams-test.ext.lukoil.com/bot/v1/files/getInfo/?token=001.3509189690.2901436216:1000000011&fileId=YaKyM6XDWUbSux99wXuq3967c05fa01bg";
//        downloadFile(getGson(url), savePath);
//    }
}
