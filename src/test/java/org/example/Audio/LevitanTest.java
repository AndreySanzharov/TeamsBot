package org.example.Audio;

import javazoom.jl.decoder.JavaLayerException;
import org.junit.jupiter.api.Test;


class LevitanTest {
    static MP3Formatter mp3Formatter = new MP3Formatter();
    static MP3ToWavConverterJlayer mp3ToWavConverterJlayer = new MP3ToWavConverterJlayer();
    static DecoderDemo decoderDemo = new DecoderDemo();
    @Test
    void mustRerurnTextOfSpeech() throws JavaLayerException {
        String LevitanMP3 = "C:\\Users\\sanzharovaa\\IdeaProjects\\bot\\src\\main\\java\\org\\example\\saveDir\\levitan.mp3";
        mp3Formatter.formatMp3(LevitanMP3);
        mp3ToWavConverterJlayer.convertMp3ToWav();
        decoderDemo.decodeWav();
    }

}