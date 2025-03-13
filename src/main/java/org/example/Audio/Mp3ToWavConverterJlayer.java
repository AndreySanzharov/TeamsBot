package org.example.Audio;

import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;

public class Mp3ToWavConverterJlayer {
    private final String sourceName = "C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\formatted.mp3";
    private final String destinationName = "C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\output.wav";

    public void convertMp3ToWav() throws JavaLayerException {
        Converter converter = new Converter();
        converter.convert(sourceName, destinationName);
    }

//    public static void main(String[] args) throws IOException, JavaLayerException {
//        String sourceName = "C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\levitan.mp3";
//        String destinationName = "C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\output.wav";
//        Converter converter = new Converter();
//        converter.convert(sourceName, destinationName);
//    }


}