package com.computacion1;

import com.computacion1.Chat.HistoryManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

import models.ChatMessage;
import models.ChatRoom;
import models.Client;
import models.headers.CallHeader;
import models.headers.HeaderServerInformation;
import models.headers.HeaderServerMessage;
import models.headers.HeaderUser;
import models.headers.HeaderUserMessage;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private Client client;
    private PrintWriter out;
    private Gson gson;
    private Integer UDP_PORT;

    public ClientHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.client = new Client("UNASSIGNED", clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.gson = new Gson();
    }

    public Integer getUDP_PORT() {
        return UDP_PORT;
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
        out.println(message);
    }

    public void sendVoiceMessage(byte[] payload) {
        new Thread(() -> {
            try {
                this.clientSocket.getOutputStream().write(payload);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void handleMessage(HeaderUser header, byte[] payload) throws IOException {
        if (header != null) {
            if (header instanceof HeaderUserMessage) {
                HeaderUserMessage headerUserMessage = ((HeaderUserMessage) header);

                String request = headerUserMessage.getMethod();

                if (!request.equals("CONNECT") && client.isAuthenticated()) {
                    executeChatCommand(headerUserMessage, payload);
                } else {
                    connect(headerUserMessage);
                }

                if (request.equals("DISCONNECT")) {
                    disconnect();
                }
            } else if (header instanceof CallHeader) {
                handleCallHeader((CallHeader) header);
            }
        }
    }

    public void executeChatCommand(HeaderUserMessage data, byte[] payload) throws IOException {
        String message = null;
        if (data.getType().equalsIgnoreCase("TEXT")) {
            message = new String(payload, 0, payload.length).trim();
        }

        switch (data.getMethod()) {
            case "BROADCAST" -> {
                Main.chaters.broadCastMessage(data, payload, client.getUsername());
            }
            case "CREATE" -> {
                try {
                    createNewRoomChat(data, message);
                    prepareMessage(200, "", "Group Created");
                } catch (Exception e) {
                    e.printStackTrace();
                    prepareMessage(501, "ERROR_CREATING_GROUP",
                            "There was an error creating the group");
                }
            }
            case "MESSAGE" -> {
                try {
                    Main.chaters.sendPrivateMessage(client.getUsername(), data.getRecipient(), message);
                    saveMessage("TEXT", message, data.getRecipient());
                    prepareMessage(200, "", "Sent.");
                } catch (JsonSyntaxException e) {

                } catch (Exception e) {
                    prepareMessage(501, "USER_NOT_FOUND",
                            "The entered username was not found. The message was not sent.");
                }
            }
            case "GROUP_MESSAGE" -> {
                try {

                    UUID chatRoomID = client.getRoomByName(data.getRecipient());
                    if (chatRoomID == null) {
                        prepareMessage(501, "GROUP_NOT_FOUND",
                                "The entered group was not found. The message was not sent.");
                        return;
                    }
                    Main.chaters.getRooms().sendMessageToRoom(chatRoomID, payload, client.getUsername());
                    prepareMessage(200, "", "Group Message Sent!");
                    saveMessage("TEXT", message, data.getRecipient());
                } catch (JsonSyntaxException e) {
                }
            }
            case "VOICE" -> {
                try {
                    Main.chaters.sendPrivateVoiceMessage(client.getUsername(), data.getRecipient(), payload);
                    prepareMessage(200, "", "Sent.");
                    saveMessage("AUDIO", "MENSAJE DE VOZ", data.getRecipient());
                } catch (Exception e) {
                    prepareMessage(501, "USER_NOT_FOUND",
                            "The entered username was not found. The message was not sent.");
                    e.printStackTrace();
                }
            }
            case "GROUP_VOICE" -> {
                try {

                    UUID chatRoomID = client.getRoomByName(data.getRecipient());
                    if (chatRoomID == null) {
                        throw new IllegalArgumentException();
                    }
                    Main.chaters.getRooms().sendVoiceMessageToRoom(chatRoomID, payload, client.getUsername());
                    prepareMessage(200, "", "Sent.");
                    saveMessage("AUDIO", "MENSAJE DE VOZ", data.getRecipient());

                } catch (Exception e) {

                    prepareMessage(501, "GROUP_NOT_FOUND",
                            "The entered group was not found. The message was not sent.");
                    e.printStackTrace();
                }
            }
            case "VIEW_HISTORY" -> {
                viewHistory();
            }

            default -> {
            }
        }
    }

    public void handleCallHeader(CallHeader data) {
        switch (data.getMethod()) {
            // THIS CASES ARE USED TO START THE HANDSHAKE OF THE CALL OVER TCP
            case "CALL_USER" -> {
                try {
                    int callPort = Main.chaters.getCallController().startCall(this);
                    this.UDP_PORT = data.getUDP_PORT();
                    Main.chaters.callRequestHeader(client.getUsername(), data.getPersonBeingCalled(), "NEW_CALL",
                            callPort);
                } catch (Exception e) {
                    prepareMessage(501, "USER_NOT_FOUND",
                            "The entered user was not found");
                    e.printStackTrace();
                }
            }
            case "CALL_GROUP" -> {
                try {
                    UUID chatRoom = client.getRoomByName(data.getPersonBeingCalled());
                    int callPort = Main.chaters.getCallController().startCall(this);
                    this.UDP_PORT = data.getUDP_PORT();
                    Main.chaters.callRequestHeaderToGroup(client.getUsername(), chatRoom, "NEW_CALL",
                            callPort);
                } catch (Exception e) {
                    prepareMessage(501, "GROUP_NOT_FOUND", "The group was not found");
                    e.printStackTrace();
                }
            }
            case "ACCEPT_CALL" -> {
                int callPort = Main.chaters.getCallController().joinCall(this, data.getCaller());
                this.UDP_PORT = data.getUDP_PORT();
                Main.chaters.callRequestHeader(client.getUsername(), data.getCaller(), "CALL_ACCEPTED", callPort);
                prepareMessage(200, "", "CALL WAS ACCEPTED");
            }
            case "NEGATE_CALL" -> {
                Main.chaters.callRequestHeader(client.getUsername(), data.getCaller(), "CALLED_DENIED", 0);
                prepareMessage(200, "", "CALL WAS CANCELED");
            }
            case "LEAVE_CALL" -> {

            }

            default -> {
            }
        }
    }

    public void saveMessage(String type, String message, String receiver) throws IOException {
        HistoryManager.saveMessage(client.getUsername(), client.getIP(), receiver, message, type);
    }

    public void viewHistory() {
        ArrayList<ChatMessage> history = HistoryManager.getHistory(client.getUsername(), client.getIP());
        StringBuilder msg = new StringBuilder("Your history of messages:");

        for (ChatMessage message : history) {
            msg.append("\n").append(message.getDate()).append(" | To ").append(message.getReceiver()).append(": ");
            msg.append(message.getMessage());
        }
        prepareMessage(200, "", msg.toString());
    }

    public void prepareMessage(Integer status, String error, String message) {
        HeaderServerInformation header = new HeaderServerInformation(status, error, message);
        String jsonMessage = gson.toJson(header);
        System.out.println("SENDING MESSAGE" + jsonMessage);
        sendMessage(jsonMessage);
    }

    public void sendServerMessage(String type, String sender, String chatName, int dataLenght) {
        HeaderServerMessage header = new HeaderServerMessage(type, sender, chatName, dataLenght);
        String jsonMessage = gson.toJson(header);
        sendMessage(jsonMessage);
    }

    public void sendCallHeader(String method, int UDP_PORT, String sender, String destination) {
        CallHeader callHeader = new CallHeader(method, UDP_PORT, sender, destination);
        String jsonMessage = gson.toJson(callHeader, CallHeader.class);
        sendMessage(jsonMessage);
    }

    public void createNewRoomChat(HeaderUserMessage data, String payload) {
        UUID chatRoomID = client.getRoomByName(data.getRecipient());

        if (chatRoomID != null) {
            prepareMessage(501, "NAME_ALREADY_IN_USE",
                    "The name you have entered for the new group is already in use. The group was not created.");
            return;
        }

        ChatRoom newRoom = Main.chaters.getRooms().createNewChatRoom(data.getRecipient());
        client.subToChatRoom(newRoom.getID(), newRoom.getName());
        newRoom.addUser(this);
        String[] memebersInRoom = payload.split(",");
        Main.chaters.getRooms().addMultipleUsersToRoom(newRoom, memebersInRoom);
    }

    public void connect(HeaderUserMessage data) {
        client.setUsername(data.getRecipient());
        try {
            Main.chaters.addClientToMainRoom(this);
            System.out.println("CLIENT CONNECTED : " + data.getRecipient() + " | IP:" + client.getIP());
            // Create the history file of the user
            HistoryManager.createUserHistoryFile(client.getUsername(), client.getIP());
            prepareMessage(200, "", "Successfully connected. Start typing your commands...");
        } catch (IllegalArgumentException e) {
            prepareMessage(501, "USERNAME_IN_USE",
                    "The username you have entered is already in use. Connection not established. PLEASE RESTART CLIENT");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect() throws IOException {
        System.out.println("CLIENT DISCONECTED: " + client.getUsername());
        Main.chaters.removeClientFromRoom(this);
        this.clientSocket.close();
    }

    private HeaderUser readHeader(BufferedReader is) throws IOException {
        String header;
        while ((header = is.readLine()) != null) {
            if (!header.equalsIgnoreCase("")) {
                try {
                    if (header.contains("CALL_HEADER")) {
                        return gson.fromJson(header, CallHeader.class);
                    } else {
                        return gson.fromJson(header, HeaderUserMessage.class);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private byte[] readPayload(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int byteRead;

        while ((byteRead = is.read()) != -1) { // Read byte by byte
            buffer.write(byteRead); // Write byte to buffer
            if (byteRead == '\n') { // Check if newline character is found
                break;
            }
        }

        return buffer.toByteArray(); // Return the byte array
    }

    private byte[] readAudioPayload(InputStream is, int fileSize) throws IOException {
        long bytesReceived = 0;
        byte[] buffer = new byte[1024]; // Buffer size can be adjusted based on needs
        ByteArrayOutputStream wavDataBuffer = new ByteArrayOutputStream();

        // Continuously read from InputStream until all expected bytes are received
        while (bytesReceived < fileSize) {
            int bytesRead = is.read(buffer);
            if (bytesRead == -1) {
                break; // End of stream reached
            }
            wavDataBuffer.write(buffer, 0, bytesRead);
            bytesReceived += bytesRead;
        }

        return wavDataBuffer.toByteArray();
    }

    @Override
    public void run() {
        try (InputStream is = this.clientSocket.getInputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            byte[] payload = new byte[0];
            while (true) { // Continue until the connection is closed
                // Read the header
                HeaderUser header = readHeader(reader);
                System.out.println("HEADER RECEIVED: " + header);
                if (header != null) {
                    if (header.getDataLenght() > 0) {
                        // Read the payload
                        if (header.getType().equals("AUDIO")) {
                            payload = readAudioPayload(is, header.getDataLenght());
                        } else {
                            payload = readPayload(is);
                        }

                    }

                    // Handle the message
                    handleMessage(header, payload);
                } else {
                    // Exit the loop if header is null (indicating end of stream)
                    disconnect();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading from client " + client.getUsername() + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
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
