package com.computacion1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class Chatters {

    private Collection<ClientHandler> chatters;
    private HashMap<String, ClientHandler> memebers; // I decided to use a hasmap to make user lookup very fast

    public Chatters() {
        this.chatters = Collections.synchronizedCollection(new ArrayList<>());
        memebers = new HashMap<>();
    }

    public Collection<ClientHandler> getChattersRoom() {
        return this.chatters;
    }

    public void addClientToRoom(ClientHandler client) throws IllegalArgumentException {
        ClientHandler exists = memebers.get(client.getClient().getUsername());
        if (exists == null) {
            chatters.add(client);
            memebers.put(client.getClient().getUsername(), client);
        } else {
            throw new IllegalArgumentException("this name is alredy in use in this chat room");
        }

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
        System.out.println(chatters.size());
        for (ClientHandler client : chatters) {
            client.sendMessage(payload);
        }
    }
}
