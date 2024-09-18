package com.computacion1;

import java.util.HashMap;
import java.util.UUID;

import models.ChatRoom;

public class RoomsController {

    private HashMap<UUID, ChatRoom> chatRooms;

    public RoomsController() {
        chatRooms = new HashMap<>();
    }

    public void addMultipleUsersToRoom(ChatRoom room, String[] membersToAdd) {
        for (int i = 0; i < membersToAdd.length; i++) {
            ClientHandler currentUser = Main.chaters.getMemberByName(membersToAdd[i]);
            Boolean wasSub = currentUser.getClient().subToChatRoom(room.getID(), room.getName());
            if (wasSub) {
                room.addUser(currentUser);
            }
        }
    }

    public ChatRoom createNewChatRoom(String name) {
        ChatRoom room = new ChatRoom(name);
        chatRooms.put(room.getID(), room);
        return room;
    }

    public void sendMessageToRoom(ChatRoom room, String message) {
        chatRooms.get(room.getID()).sendMessage(message);
    }

    public ChatRoom getRoomByID(UUID roomID) {
        return chatRooms.get(roomID);
    }
}
