package models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    public void sendMessage(String sender, String message) {
        new Thread(() -> {
            for (ClientHandler clientHandler : members) {
                System.out.println("SENDING MESSAGE TO: " + clientHandler.getClient().getUsername());
                clientHandler.sendServerMessage("TEXT", sender, name, message.getBytes().length);
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                clientHandler.sendMessage(message);
            }
        }).start();
    }

    public void sendVoiceNote(byte[] payload, String sender) {
        new Thread(() -> {
            for (ClientHandler clientHandler : members) {
                clientHandler.sendServerMessage("AUDIO", sender, name, payload.length);
                clientHandler.sendVoiceMessage(payload);
            }
        }).start();
    }

    public void sendCallRequestToUsers(String sender, String method, int uDP_PORT) {
        new Thread(() -> {
            for (ClientHandler clientHandler : members) {
                if (!clientHandler.getClient().getUsername().equalsIgnoreCase(sender)) {

                    clientHandler.sendCallHeader(method, uDP_PORT, sender, clientHandler.getClient().getUsername());
                }
            }
        }).start();
    }
}
