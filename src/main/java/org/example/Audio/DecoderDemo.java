package org.example.Audio;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DecoderDemo {

    public void decodeWav() {
        LibVosk.setLogLevel(LogLevel.DEBUG);
        String uri = "C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\output.wav";

        try (Model model = new Model("C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\resources\\vosk-model-small-ru-0.22");
             InputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(
                     new FileInputStream(uri)));
             Recognizer recognizer = new Recognizer(model, 16000)) {

            int nbytes;
            byte[] b = new byte[4096];
            while ((nbytes = ais.read(b)) >= 0) {
                if (recognizer.acceptWaveForm(b, nbytes)) {
                    System.out.println(recognizer.getPartialResult());
                }
            }

            System.out.println(recognizer.getFinalResult());
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    //    public static void main(String[] argv) throws IOException, UnsupportedAudioFileException {
//        LibVosk.setLogLevel(LogLevel.DEBUG);
//        String uri = "C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\output.wav";
//
//        try (Model model = new Model("C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\resources\\vosk-model-small-ru-0.22");
//             InputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(
//                     new FileInputStream(uri)));
//             Recognizer recognizer = new Recognizer(model, 16000)) {
//
//            int nbytes;
//            byte[] b = new byte[4096];
//            while ((nbytes = ais.read(b)) >= 0) {
//                if (recognizer.acceptWaveForm(b, nbytes)) {
//                    System.out.println(recognizer.getPartialResult());
//                }
//            }
//
//            System.out.println(recognizer.getFinalResult());
//        }
//    }
}