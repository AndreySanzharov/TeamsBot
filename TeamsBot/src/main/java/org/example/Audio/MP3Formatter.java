package org.example.Audio;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;

public class MP3Formatter {
    private static final File target = new File(
            "C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\formatted.mp3"
    );

    public static void formatMp3(String source) {
        Logger root = Logger.getRootLogger();
        root.addAppender(new ConsoleAppender(new PatternLayout("%r [%t] %p %c %x - %m%n")));

        try {
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("libmp3lame");
            audio.setBitRate(64000);
            audio.setSamplingRate(16000);
            audio.setChannels(1);

            EncodingAttributes attributes = new EncodingAttributes();
            attributes.setInputFormat("mp3");
            attributes.setAudioAttributes(audio);

            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(new File(source)), target, attributes);
            System.out.println();
        } catch (EncoderException e) {
            e.getStackTrace();
        }
    }
//    public static void main(String[] args) {
//        Logger root = Logger.getRootLogger();
//        root.addAppender(new ConsoleAppender(new PatternLayout("%r [%t] %p %c %x - %m%n")));

//        File source = new File("C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\levitan.mp3");
    //     File target = new File("C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\formatted.mp3");
//        try {
//            AudioAttributes audio = new AudioAttributes();
//            audio.setCodec("libmp3lame");
//            audio.setBitRate(64000);
//            audio.setSamplingRate(16000);
//            audio.setChannels(1);
//
//            EncodingAttributes attributes = new EncodingAttributes();
//            attributes.setInputFormat("mp3");
//            attributes.setAudioAttributes(audio);
//
//            Encoder encoder = new Encoder();
//            encoder.encode(new MultimediaObject(source), target, attributes);
//            System.out.println();
//        } catch (EncoderException e) {
//           e.getStackTrace();
//        }
//    }
//}
}
