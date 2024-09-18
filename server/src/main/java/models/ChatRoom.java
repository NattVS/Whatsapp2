package models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.computacion1.ClientHandler;

public class ChatRoom {
    private UUID ID;
    private String name;
    private Set<ClientHandler> members;

    public ChatRoom(String name) {
        this.name = name;
        this.ID = UUID.randomUUID();
        members = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getID() {
        return ID;
    }

    public void setID(UUID iD) {
        ID = iD;
    }

    public void addUser(ClientHandler client) {
        members.add(client);
    }

    public void removeUser(ClientHandler client) {
        members.remove(client);
    }

    public boolean isUserInChatRoom(ClientHandler client) {
        return members.contains(client);
    }

    public void sendMessage(String message) {
        new Thread(() -> {
            for (ClientHandler clientHandler : members) {
                clientHandler.sendMessage(message);
            }
        }).start();
    }
}
