package org.example.Audio;

import javazoom.jl.decoder.JavaLayerException;

public class LevitanTEST {
    static MP3ToWavConverterJlayer mp3ToWavConverterJlayer = new MP3ToWavConverterJlayer();
    static MP3Formatter MP3Formatter = new MP3Formatter();
    static DecoderDemo decoderDemo = new DecoderDemo();

    public static void main(String[] args) throws JavaLayerException {
        String LevitanMP3 = "C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\levitan.mp3";
        //String LevitanWav = "C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\output.wav";

        MP3Formatter.formatMp3(LevitanMP3);
        mp3ToWavConverterJlayer.convertMp3ToWav();
        decoderDemo.decodeWav();
    }
}
