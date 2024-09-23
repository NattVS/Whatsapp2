package com.computacion1;

import models.ChatRoom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class Chatters {

    private Collection<ClientHandler> chatters;
    private HashMap<String, ClientHandler> memebers; // I decided to use a hashmap to make user lookup very fast
    private RoomsController rooms = new RoomsController();

    public Chatters() {
        this.chatters = Collections.synchronizedCollection(new ArrayList<>());
        memebers = new HashMap<>();
    }

    public RoomsController getRooms() {
        return rooms;
    }

    public ClientHandler getMemberByName(String username){
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
        ClientHandler client = memebers.get(destination);
        if (client == null) {
            throw new IllegalArgumentException();
        }
        client.prepareMessage("TEXT", sender, null, message);
    }

    public void sendPrivateVoiceMessage(String sender, String destination, String message) throws IllegalArgumentException {
        ClientHandler client = memebers.get(destination);
        if (client == null) {
            throw new IllegalArgumentException();
        }
        client.prepareMessage("VOICE", sender, null, message);
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

    public void broadCastMessage(String payload) {
        for (ClientHandler client : chatters) {
            client.sendMessage(payload);
        }
    }
}
