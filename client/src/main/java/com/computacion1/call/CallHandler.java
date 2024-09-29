package com.computacion1.call;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.io.IOException;
import javax.sound.sampled.*;
import java.net.*;
import java.io.ByteArrayOutputStream;

import com.computacion1.UserInput.UserInputHandler;

public class CallHandler {
    private DatagramSocket connection;
    private int UDP_PORT;
    private String IP_SERVER = "localhost";
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = true;
    private volatile boolean callEnded = false;

    private static final int MAX_PACKET_SIZE = 8192; // Tamaño máximo del paquete acumulado

    public CallHandler() throws SocketException {
        this.connection = new DatagramSocket(0); // Inicializa con un puerto libre
        this.UDP_PORT = connection.getLocalPort();
    }

    public int getUDP_PORT() {
        return UDP_PORT;
    }

    private AudioFormat getAudioFormat() {
        return new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
    }

    public void startCall(UserInputHandler reader, int SERVER_UDP_PORT) {
        System.out.println("Press X to exit the call");
        // Hilo que escucha si el usuario quiere colgar la llamada
        new Thread(() -> monitorUserInput(reader)).start();

        try {
            InetAddress serverAddress = InetAddress.getByName(IP_SERVER); // Cambiar por la IP del servidor
            DatagramSocket socket = this.connection;
            // Iniciar el hilo para recibir y reproducir audio
            Thread audioReceiverThread = new Thread(() -> receiveAndPlayAudio(socket, getAudioFormat()));
            audioReceiverThread.start();

            // Capturar y enviar audio
            captureAndSendAudio(socket, serverAddress, SERVER_UDP_PORT, getAudioFormat());

            // Espera a que la llamada termine para finalizar el hilo de recepción
            audioReceiverThread.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            closeCall();
        }
    }

    private void monitorUserInput(UserInputHandler reader) {
        String userInput;
        try {
            while (!callEnded) {
                userInput = reader.getUserInput("").trim();
                if (userInput.equalsIgnoreCase("X")) {
                    callEnded = true;
                    System.out.println("Call ended.");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    private void captureAndSendAudio(DatagramSocket socket, InetAddress serverAddress, int serverPort,
            AudioFormat format) {
        try (TargetDataLine line = openAudioCaptureLine(format)) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[SAMPLE_RATE];
            System.out.println("Capturing audio and sending to server...");

            while (!callEnded) {
                int bytesRead = line.read(buffer, 0, buffer.length); // Leer el audio
                if (bytesRead > 0) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);

                    if (byteArrayOutputStream.size() >= MAX_PACKET_SIZE || bytesRead < buffer.length) {
                        sendAudioPacket(socket, byteArrayOutputStream, serverAddress, serverPort);
                    }
                }
            }
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }

    private void sendAudioPacket(DatagramSocket socket, ByteArrayOutputStream byteArrayOutputStream,
            InetAddress serverAddress, int serverPort) throws IOException {
        byte[] audioData = byteArrayOutputStream.toByteArray();
        DatagramPacket packet = new DatagramPacket(audioData, audioData.length, serverAddress, serverPort);
        socket.send(packet); // Enviar los datos de audio como paquete UDP
        byteArrayOutputStream.reset(); // Reiniciar el ByteArrayOutputStream
    }

    private TargetDataLine openAudioCaptureLine(AudioFormat format) throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
        return line;
    }

    private void receiveAndPlayAudio(DatagramSocket socket, AudioFormat format) {
        try (SourceDataLine lineOut = openAudioPlaybackLine(format)) {
            byte[] buffer = new byte[SAMPLE_RATE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            System.out.println("Listening for audio...");

            while (!callEnded) {
                socket.receive(packet);
                lineOut.write(packet.getData(), 0, packet.getLength());
            }
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private SourceDataLine openAudioPlaybackLine(AudioFormat format) throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine lineOut = (SourceDataLine) AudioSystem.getLine(info);
        lineOut.open(format);
        lineOut.start();
        return lineOut;
    }

    private void closeCall() {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        System.out.println("Call resources released.");
    }
}
