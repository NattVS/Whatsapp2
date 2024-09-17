package com.computacion1;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import models.ChatRoom;
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
            // TODO: handle all the voice stuff realted
            executeChatCommand(message);
        } else {
            connect(message);
        }
    }

    public void executeChatCommand(String message) {
        String[] payload = message.split("~~"); // Depending of the message but it should be COMMAND~~MAIN
                                                // INFO~~ADITIONAL INFO
        if (payload.length >= 2) {
            switch (payload[0]) {
                case "BROADCAST":
                    Main.chaters.broadCastMessage(payload[1]);
                    break;
                case "CREATE":
                    createNewRoomChat(payload);
                    break;
                case "MESSAGE":
                    try {
                        Main.chaters.sendPrivateMessage(client.getUsername(), payload[1], payload[2]);
                    } catch (IllegalArgumentException e) {
                        sendMessage("NO USER WITH THIS NAME");
                    }
                    break;

                case "GROUP_MESSAGE":
                    sendMessageToGroupChat(payload);
                    break;
                default:
                    break;
            }
        } else {
            sendMessage("!INCORRECT COMMAND");
        }
    }

    public void createNewRoomChat(String[] payload) {
        UUID chatRoomID = client.getRoomByName(payload[1]);
        if (chatRoomID != null) {
            sendMessage("YOU ALREADY HAVE A ROOM WITH THIS NAME");
            return;
        }

        // Add this user first since he created it
        ChatRoom newRoom = Main.chaters.getRooms().createNewChatRoom(payload[1]);
        client.subToChatRoom(newRoom.getID(), newRoom.getName());
        newRoom.addUser(this);

        String[] memebersInRoom = payload[2].split(",");
        Main.chaters.getRooms().addMultipleUsersToRoom(newRoom, memebersInRoom);
    }

    public void sendMessageToGroupChat(String[] payload) {

        UUID chatRoomID = client.getRoomByName(payload[1]);
        if (chatRoomID == null) {
            sendMessage("ROOM DOES NOT EXIST");
            return;
        }
        ChatRoom userRoom = Main.chaters.getRooms().getRoomByID(chatRoomID);
        userRoom.sendMessage(payload[2]);
    }

    public void connect(String message) {

        String[] payload = message.split("~~"); // The message should be CONNECT~~USERNAME
        if (payload.length != 2) {
            return;
        }
        client.setUsername(payload[1]);
        try {
            Main.chaters.addClientToMainRoom(this);
            System.out.println("CLIENT CONNECTED : " + payload[1] + " | IP:" + client.getIP());
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
