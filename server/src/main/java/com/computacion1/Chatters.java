package com.computacion1;

import java.util.ArrayList;

public class Chatters {

    private ArrayList<ClientHandler> chatters;

    public Chatters() {
        this.chatters = new ArrayList<>();
    }

    public ArrayList<ClientHandler> getChattersRoom() {
        return this.chatters;
    }

    public void addClientToRoom(ClientHandler client) throws IllegalArgumentException {
        boolean sameName = false;
        for (ClientHandler clientHandler : chatters) {
            if (clientHandler.getClient().getUsername().equalsIgnoreCase(client.getClient().getUsername())) {
                sameName = true;
            }
        }
        if (!sameName) {
            chatters.add(client);
        } else {
            throw new IllegalArgumentException("this name is alredy in use in this chat room");
        }

    }

    public void removeClientFromRoom(ClientHandler client) {
        chatters.remove(client);
    }

    public void broadCastMessage(String payload) {
        System.out.println(chatters.size());
        for (int i = 0; i < this.chatters.size(); i++) {
            this.chatters.get(i).sendMessage(payload);
        }
    }
}
