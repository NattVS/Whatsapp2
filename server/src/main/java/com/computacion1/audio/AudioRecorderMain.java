package com.computacion1.audio;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;

public class AudioRecorderMain {

    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = true;

    public static byte[] recordAudio(int durationInSeconds) {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        VoiceMessageRecorder recorder = new VoiceMessageRecorder(format, durationInSeconds, out);
        Thread t = new Thread(recorder);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }
}
