package models;

import java.util.HashMap;
import java.util.UUID;

public class Client {

    private String username;
    private String IP;
    private int PORT;
    private boolean isAuthenticated;
    private HashMap<String, UUID> chatRoomsSubcribed;

    public Client(String username, String IP, int PORT) {
        this.username = username;
        this.IP = IP;
        this.PORT = PORT;
        this.isAuthenticated = false;
        this.chatRoomsSubcribed = new HashMap<>();
    }

    public boolean subToChatRoom(UUID ID, String name) {
        if (!chatRoomsSubcribed.containsKey(name)) {
            chatRoomsSubcribed.put(name, ID);
            return true;
        }
        return false;
    }

    public UUID getRoomByName(String name) {
        return chatRoomsSubcribed.get(name);
    }

    public void authenticate() {
        this.isAuthenticated = true;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String newName) {
        this.username = newName;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String iP) {
        IP = iP;
    }

    public int getPORT() {
        return PORT;
    }

    public void setPORT(int pORT) {
        PORT = pORT;
    }

}
