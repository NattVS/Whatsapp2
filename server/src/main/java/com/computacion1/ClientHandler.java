package com.computacion1;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

import models.Client;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private Client client;
    private PrintWriter out;

    public ClientHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.client = new Client("UNASSIGNED", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort()); // TODO:
        // IMPLEMENT
        // THIS
        // CREATION
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public Socket getClientScoket() {
        return clientSocket;
    }

    public void setClientScoket(Socket clientScoket) {
        this.clientSocket = clientScoket;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void sendMessage(String message) {
        new Thread(() -> {
            out.println(message);
        }).start();
    }

    public void handleMessage(byte[] payload) throws IOException {
        String message = new String(payload);
        // For now im just going to broadcast the message.
        if (!message.contains("CONNECT") && client.isAuthenticated()) {
            message = this.client.getUsername() + ": " + message;
            Main.chaters.broadCastMessage(message);
        } else {
            connect(message);
        }
    }

    public void connect(String message) {

        String[] username = message.split("~~"); // The message should be CONNECT~~USERNAME
        if (username.length != 2){
            return;
        }
        client.setUsername(username[1]);
        try {
            Main.chaters.addClientToRoom(this);
            System.out.println("CLIENT CONNECTED : " + username[1] + " | IP:" + client.getIP());
            sendMessage("ACK");
        } catch (IllegalArgumentException e) {
            sendMessage("ERROR");
        }
    }

    public void disconnect() throws IOException {
        this.clientSocket.close();
    }

    // This gets called for every socket since the server need a thread to listen to
    // every incoming request from the user
    @Override
    public void run() {
        InputStream is;
        try {
            is = this.clientSocket.getInputStream();
            byte[] buffer = new byte[1024];

            while (!clientSocket.isClosed()) {
                int bytesRead = is.read(buffer);

                // Detect client disconnection (read() returns -1)
                if (bytesRead == -1) {
                    System.out.println("Client " + client.getUsername() + " disconnected.");
                    Main.chaters.removeClientFromRoom(this); // Remove client from chat
                    break; // Exit the loop and terminate the thread
                }

                if (bytesRead > 0) {
                    System.out.println("message read: " + new String(buffer, 0, bytesRead));
                    handleMessage(Arrays.copyOf(buffer, bytesRead)); // Handle only the bytes read
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading from client " + client.getUsername() + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up resources when the client disconnects or an error occurs
            try {
                if (!clientSocket.isClosed()) {
                    disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
