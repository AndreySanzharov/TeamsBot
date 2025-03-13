package org.example.Audio;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;
import net.sourceforge.jaad.util.wav.WaveFileWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class AacToWav {
//    public static void main(String[] args) {
//        convert("C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\aacFile.aac", "C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\output.wav");
//    }

    private final String input = "C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\input.aac";
    private final String output ="C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\output.wav";
    public void convert() {
        WaveFileWriter wav = null;
        try {
            ADTSDemultiplexer adts = new ADTSDemultiplexer(new FileInputStream(input));
            Decoder dec = new Decoder(adts.getDecoderSpecificInfo());
            SampleBuffer buf = new SampleBuffer();

            while(true) {
                byte[] b = adts.readNextFrame();
                dec.decodeFrame(b, buf);
                if (wav == null) {
                    wav = new WaveFileWriter(new File(output), buf.getSampleRate(), buf.getChannels(), buf.getBitsPerSample());
                }
                wav.write(buf.getData());
            }
        }
        catch (Exception e) {
            System.out.println("Приехали в конец файла: " + e.getMessage());
        }
        finally {
            if (wav != null) {
                try {
                    wav.close();
                } catch (IOException e) {
                    System.out.println("Ошибка закрытия wav: " + e.getMessage());
                }
            }
        }

    }
}
