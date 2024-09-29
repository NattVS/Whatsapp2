package com.computacion1.Call;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

import com.computacion1.ClientHandler;

public class Call implements Runnable {
    private ArrayList<ClientHandler> members; // List of clients in the call

    private DatagramSocket socketServer; // UDP socket for communication

    public Call() throws SocketException {
        this.members = new ArrayList<>();
        socketServer = new DatagramSocket(0); // Bind to an available port
    }

    public int getCallPort() {
        return socketServer.getLocalPort();
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    @Override
    public void run() {
        new Thread(() -> {
            byte[] receiveData = new byte[16000]; // Buffer for incoming data
            DatagramPacket receivePacket;

            while (true) {
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    Arrays.fill(receiveData, (byte) 0);
                    // Server waiting for clients' messages
                    socketServer.receive(receivePacket);

                    // Get the client's IP address and port
                    InetAddress IPAddress = receivePacket.getAddress();
                    int port = receivePacket.getPort();

                    // Broadcast the message to all other members
                    broadcastMessage(receiveData, IPAddress, port);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Method to add members to the call
    public void addMember(ClientHandler clientHandler) {
        members.add(clientHandler);
    }

    public void removeMember(ClientHandler clientHandler) {
        members.remove(clientHandler);
    }

    // Method to broadcast received data to all members except the sender
    private void broadcastMessage(byte[] message, InetAddress senderIP, int senderPort) {
        final int MAX_PACKET_SIZE = 16000; 
        int totalLength = message.length;
        int offset = 0;

        while (offset < totalLength) {
            // Calcular el tamaño del próximo paquete
            int packetSize = Math.min(MAX_PACKET_SIZE, totalLength - offset);
            byte[] packetData = Arrays.copyOfRange(message, offset, offset + packetSize);

            for (ClientHandler member : members) {
                if (!member.getClientScoket().getInetAddress().equals(senderIP)
                        || member.getClientScoket().getPort() != senderPort) {
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(packetData, packetData.length,
                                member.getClientScoket().getInetAddress(), member.getUDP_PORT());
                        socketServer.send(sendPacket); // Enviar el mensaje al miembro
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            offset += packetSize; // Avanzar al siguiente segmento
        }
    }

    public void shutDown() {
        members.clear();
    }
}
