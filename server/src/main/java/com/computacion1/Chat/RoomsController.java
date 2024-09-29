package com.computacion1.Chat;

import java.util.HashMap;
import java.util.UUID;

import com.computacion1.ClientHandler;
import com.computacion1.Main;

import models.ChatRoom;

public class RoomsController {

    private HashMap<UUID, ChatRoom> chatRooms;

    public RoomsController() {
        chatRooms = new HashMap<>();
    }

    public void addMultipleUsersToRoom(ChatRoom room, String[] membersToAdd) {
        for (String user : membersToAdd) {
            ClientHandler currentUser = Main.chaters.getMemberByName(user);
            if (currentUser != null) {
                Boolean wasSub = currentUser.getClient().subToChatRoom(room.getID(), room.getName());
                if (wasSub) {
                    room.addUser(currentUser);
                }
            }
        }
    }

    public ChatRoom createNewChatRoom(String name) {
        ChatRoom room = new ChatRoom(name);
        chatRooms.put(room.getID(), room);
        return room;
    }

    public ChatRoom sendMessageToRoom(UUID chatRoomId, byte[] message, String sender) {
        String messageString = new String(message, 0, message.length);
        ChatRoom room = getRoomByID(chatRoomId);
        room.sendMessage(sender, messageString);
        return room;
    }

    public void sendVoiceMessageToRoom(UUID chatRoomId, byte[] payload, String sender) {
        ChatRoom room = getRoomByID(chatRoomId);
        room.sendVoiceNote(payload, sender);
    }

    public ChatRoom getRoomByID(UUID roomID) {
        return chatRooms.get(roomID);
    }
}