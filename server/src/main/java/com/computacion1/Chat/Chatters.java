package com.computacion1.Chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.computacion1.ClientHandler;
import com.computacion1.Call.CallController;

import models.ChatRoom;
import models.headers.HeaderUserMessage;

public class Chatters {

    private Collection<ClientHandler> chatters;
    private HashMap<String, ClientHandler> memebers; // I decided to use a hashmap to make user lookup very fast
    private RoomsController rooms = new RoomsController();
    private CallController callController = new CallController();

    public Chatters() {
        this.chatters = Collections.synchronizedCollection(new ArrayList<>());
        memebers = new HashMap<>();
    }

    public CallController getCallController() {
        return callController;
    }

    public RoomsController getRooms() {
        return rooms;
    }

    public ClientHandler getMemberByName(String username) {
        return memebers.get(username);
    }

    public void addClientToMainRoom(ClientHandler client) throws IllegalArgumentException {
        ClientHandler exists = memebers.get(client.getClient().getUsername());
        if (exists == null) {
            chatters.add(client);
            memebers.put(client.getClient().getUsername(), client);
            client.getClient().authenticate();
        } else {
            throw new IllegalArgumentException("This name is already in use in this chat room.");
        }
    }

    public void sendPrivateMessage(String sender, String destination, String message) throws IllegalArgumentException {
        new Thread(() -> {

            ClientHandler client = memebers.get(destination);
            if (client == null) {
                throw new IllegalArgumentException();
            }
            client.sendServerMessage("TEXT", sender, "", message.getBytes().length);
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            client.sendMessage(message);
        }).start();
    }

    public void callRequestHeader(String sender, String destination, String method, int UDP_PORT) {
        ClientHandler client = memebers.get(destination);
        if (client == null) {
            throw new IllegalArgumentException();
        }
        client.sendCallHeader(method, UDP_PORT, sender, destination);
    }

    public void callRequestHeaderToGroup(String sender, UUID destination, String method, int UDP_PORT){
       ChatRoom room = rooms.getRoomByID(destination);
       room.sendCallRequestToUsers( sender,  method,  UDP_PORT);
    }

    public void sendServerHeader(String destination, Integer status, String error, String message) {

        ClientHandler client = memebers.get(destination);
        if (client == null) {
            throw new IllegalArgumentException();
        }
        client.prepareMessage(status, error, "");
    }


    public void sendPrivateVoiceMessage(String sender, String destination, byte[] payload)
            throws IllegalArgumentException {
        ClientHandler client = memebers.get(destination);
        if (client == null) {
            throw new IllegalArgumentException();
        }
        client.sendServerMessage("AUDIO", sender, "", payload.length);
        client.sendVoiceMessage(payload);
    }

    public void removeClientFromRoom(ClientHandler client) {
        memebers.remove(client.getClient().getUsername());
        chatters.remove(client);
    }

    public void shutdown() {
        for (ClientHandler client : chatters) {
            try {
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        memebers.clear();
        chatters.clear();
    }

    public void broadCastMessage(HeaderUserMessage data, byte[] payload, String sender) {
        if (data.getType().equals("TEXT")) {
            String message = new String(payload).trim();
            for (ClientHandler client : chatters) {
                client.sendServerMessage("TEXT", sender, "", payload.length);
                client.sendMessage(message);
            }
        } else {
            for (ClientHandler client : chatters) {
                client.sendServerMessage("AUDIO", sender, "", payload.length);
                client.sendVoiceMessage(payload);
            }
        }
    }
}
