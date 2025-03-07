package org.example.Audio;

import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class VoskFileRecognizer {
    static String modelPath = "C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\resources\\vosk-model-small-ru-0.22";

    public static void main(String[] args) throws IOException {
        wavToText("C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\output.wav");
    }

    public static String wavToText(String audioFilePath) throws IOException {
        String text = null;
        Model model = new Model(modelPath);
        Recognizer recognizer = new Recognizer(model, 16000);

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(audioFilePath));
             byte[] buffer = new byte[100000];
             int bytesRead;

             while ((bytesRead = audioInputStream.read(buffer)) != -1){
               recognizer.acceptWaveForm(buffer, bytesRead);
             }

            System.out.println(recognizer.getResult());
             text = recognizer.getResult();
             recognizer.close();
             model.close();
        } catch (UnsupportedAudioFileException | IOException e) {
            e.getStackTrace();
        }
        return text;
    }
}
