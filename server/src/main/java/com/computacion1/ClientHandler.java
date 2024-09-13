package com.computacion1;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;

import models.Client;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private Client client;
    private PrintWriter out;

    public ClientHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.client = new Client(" ", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort()); // TODO:
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
        });
    }

    public void handleMessage(byte[] payload) throws IOException {
        String message = new String(payload);
        message = this.client.getUsername() + ": " + message;
        // TODO: HANDLE THE LOGIC TO ADD A CLIENT WHEN THEY SEND THE USERNAME
        System.out.println(message);
        // For now im just going to broadcast the message.
        if (!message.contains("CONNECT")) {
            Main.chaters.broadCastMessage(message);
        } else {
            String[] username = message.split("~~"); // The message should be CONNECT///USERNAME
            client.setUsername(username[1]);
            try {
                Main.chaters.addClientToRoom(this);
            } catch (IllegalArgumentException e) {
                sendMessage("NAME ALREDY IN USE");
                this.clientSocket.close();
            }
        }

    }

    // This gets called for every socket since the server need a thread to listen to
    // every incoming request from the users
    @Override
    public void run() {
        while (!clientSocket.isClosed()) {
            InputStream is;
            try {
                is = this.clientSocket.getInputStream();
                byte[] buffer = new byte[1024];
                is.read(buffer); // this is the action that gets blocked, since needing a new thread for each
                                 // socket
                System.out.println("message read");
                handleMessage(buffer);
            } catch (IOException e) {
                e.printStackTrace(); // TODO: HANDLE WHAT TO DO IF IM NOT HABLE TO HANDLE A MESSAGE
            }
        }
    }

}
