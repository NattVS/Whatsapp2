//Esta clase es nueva, es la que usa el 
//profe para probar el recording la creé pues para usar esta forma en vez de la otra

package com.computacion1.audio;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

public class RecordAudio implements Runnable {

    private AudioFormat format;
    private int duration;
    private ByteArrayOutputStream out;

    public RecordAudio(AudioFormat format, int duration, ByteArrayOutputStream out) {
        this.format = format;
        this.duration = duration;
        this.out = out;
    }

    @Override
    public void run() {
        int bytesRead;
        try {
            // Abriir linea de captura de audio
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);

            line.open(format);
            line.start();  // Iniciar captura de audio
            System.out.println("Capturing audio..." + duration + " segundos");

            byte[] buffer = new byte[line.getBufferSize() / 5];
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < TimeUnit.SECONDS.toMillis(duration)) {
                bytesRead = line.read(buffer, 0, buffer.length);
                out.write(buffer, 0, bytesRead);
            }
            System.out.println("capturó audio");
            line.stop();
            line.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}