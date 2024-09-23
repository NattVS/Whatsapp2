package com.computacion1;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

import com.google.gson.JsonSyntaxException;
import models.ChatRoom;
import models.Client;
import models.headers.HeaderServerInformation;
import models.headers.HeaderServerMessage;
import models.headers.HeaderUserMessage;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private Client client;
    private PrintWriter out;
    private Gson gson;

    private HeaderUserMessage currentHeader;

    public ClientHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.client = new Client("UNASSIGNED", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort()); // TODO:
        // IMPLEMENT
        // THIS
        // CREATION
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.gson = new Gson();
        currentHeader = null;
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

    public void handleMessage(HeaderUserMessage headerUserMessage, String messageContent) throws IOException {
        String request = headerUserMessage.getMethod();
        if (!request.equals("CONNECT") && client.isAuthenticated()) {
            executeChatCommand(headerUserMessage, messageContent);
        } else {
            connect(headerUserMessage);
        }
    }

    public void executeChatCommand(HeaderUserMessage data, String message) {

        switch (data.getMethod()) {
            case "BROADCAST" -> Main.chaters.broadCastMessage(data.getRecipient());
            case "CREATE" -> createNewRoomChat(data, message);
            case "MESSAGE" -> {
                try {
                    Main.chaters.sendPrivateMessage(client.getUsername(), data.getRecipient(), message);
                    prepareMessage(200,"","Sent.");
                } catch (IllegalArgumentException e) {
                    prepareMessage(501,"USER_NOT_FOUND","The entered username was not found. The message was not sent." );
                }
            }
            case "GROUP_MESSAGE" -> sendMessageToGroupChat(data, message);
            case "VOICE" -> {
                try {
                    Main.chaters.sendPrivateVoiceMessage(client.getUsername(), data.getRecipient(), message);
                    prepareMessage(200,"","Sent.");
                } catch (IllegalArgumentException e) {
                    prepareMessage(501,"USER_NOT_FOUND","The entered username was not found. The message was not sent.");
                }
            }
            case "GROUP_VOICE" -> sendVoiceMessageToGroup(data, message);
            default -> {
            }
        }
    }

    public void prepareMessage(Integer status, String error, String message){
        HeaderServerInformation header = new HeaderServerInformation(status,error);
        String jsonMessage = gson.toJson(header);
        sendMessage(jsonMessage);
        sendMessage(message);
    }

    public void prepareMessage(String type, String sender, ChatRoom chat, String content){
        HeaderServerMessage header;
        if (chat != null){
            header = new HeaderServerMessage(type, sender, chat.getName());
        } else {
            header = new HeaderServerMessage(type, sender, null);
        }
        String jsonMessage = gson.toJson(header);
        if (chat != null){
            chat.sendMessage(jsonMessage);
            chat.sendMessage(content);
        } else {
            sendMessage(jsonMessage);
            sendMessage(content);
        }
    }

    public void createNewRoomChat(HeaderUserMessage data, String message) {
        UUID chatRoomID = client.getRoomByName(data.getRecipient());
        if (chatRoomID != null) {
            prepareMessage(501,"NAME_ALREADY_IN_USE", "The name you have entered for the new group is already in use. The group was not created.");
            return;
        }

        ChatRoom newRoom = Main.chaters.getRooms().createNewChatRoom(data.getRecipient());
        client.subToChatRoom(newRoom.getID(), newRoom.getName());
        newRoom.addUser(this);

        String[] memebersInRoom = message.split(",");
        Main.chaters.getRooms().addMultipleUsersToRoom(newRoom, memebersInRoom);
        prepareMessage(200,"", "The chat group was successfully created!");
    }

    public void sendMessageToGroupChat(HeaderUserMessage data, String message) {
        UUID chatRoomID = client.getRoomByName(data.getRecipient());
        if (chatRoomID == null) {
            prepareMessage(501,"GROUP_NOT_FOUND", "The entered group was not found. The message was not sent.");
            return;
        }
        ChatRoom userRoom = Main.chaters.getRooms().getRoomByID(chatRoomID);
        prepareMessage("TEXT", client.getUsername(), userRoom, message);
    }

    public void sendVoiceMessageToGroup(HeaderUserMessage data, String message){
        UUID chatRoomID = client.getRoomByName(data.getRecipient());
        if (chatRoomID == null) {
            prepareMessage(501,"GROUP_NOT_FOUND", "The entered group was not found. The message was not sent.");
            return;
        }
        ChatRoom userRoom = Main.chaters.getRooms().getRoomByID(chatRoomID);
        prepareMessage("VOICE", client.getUsername(), userRoom, message);
    }

    public void connect(HeaderUserMessage data) {
        client.setUsername(data.getRecipient());
        try {
            Main.chaters.addClientToMainRoom(this);
            System.out.println("CLIENT CONNECTED : " + data.getRecipient() + " | IP:" + client.getIP());
            prepareMessage(200,"","Successfully connected. Start typing your commands...");
        } catch (IllegalArgumentException e) {
            prepareMessage(501, "USERNAME_IN_USE", "The username you have entered is already in use. Connection not established");
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
                    String receivedMessage = new String(buffer, 0, bytesRead).trim();
                    try {
                        currentHeader = gson.fromJson(receivedMessage, HeaderUserMessage.class);
                        if (currentHeader.getMethod().equals("CONNECT")){
                            handleMessage(currentHeader, "");
                        }
                    } catch (JsonSyntaxException e) {
                        handleMessage(currentHeader, receivedMessage);
                    }
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
