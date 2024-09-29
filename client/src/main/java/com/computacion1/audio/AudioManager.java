package com.computacion1.audio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

public class AudioManager {
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = true;

    // Method to record audio and return it as a byte array
    public byte[] recordAudio(int duration) {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Record the audio using a separate thread
        RecordAudio recorder = new RecordAudio(format, duration, out);
        Thread t = new Thread(recorder);
        t.start();
        try {
            t.join(); // Wait for the thread to finish
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return out.toByteArray(); // Return the recorded audio as a byte array
    }

    public void playVoiceMessage(byte[] audioData) {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
            AudioInputStream audioStream = new AudioInputStream(byteArrayInputStream, format, audioData.length / format.getFrameSize());

            Clip clip = AudioSystem.getClip();

            clip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close(); // Close the clip when the audio is done
                        System.out.println("Audio played.");
                        System.out.println("Press enter to continue.");
                    }
                }
            });

            clip.open(audioStream);
            clip.start();

            System.out.println("Playing audio...");

        } catch (Exception e) {
            System.out.println("Error playing voice message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

