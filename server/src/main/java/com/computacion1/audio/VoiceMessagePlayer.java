package com.computacion1.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;

public class VoiceMessagePlayer {

    private AudioFormat format;
    private SourceDataLine lineOut;
    private AudioInputStream in;

    public VoiceMessagePlayer (AudioFormat format) {
        this.format = format;
    }

    public void initAudio(byte[] audio) {
        try {

            long frames = audio.length / format.getFrameSize();
            in = new AudioInputStream(new ByteArrayInputStream(audio), format, frames);

            lineOut = AudioSystem.getSourceDataLine(format);
            lineOut.open(format);
            lineOut.start();

            playAudio();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playAudio() {
        byte[] buffer = new byte[1024];
        int bytesRead;
        try {

            System.out.println("Playing audio...");

            while ((bytesRead = in.read(buffer, 0, buffer.length)) != -1) {
                lineOut.write(buffer, 0, bytesRead);
            }

            lineOut.drain();
            lineOut.stop();
            lineOut.close();
            System.out.println("Audio finished.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}