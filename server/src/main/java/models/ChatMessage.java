package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private String sender;
    private String receiver;
    private String message;
    private String type;
    private String date;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ChatMessage(String sender, String receiver, String message, String type) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.date = LocalDateTime.now().format(formatter);
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public String getType() {
        return type;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}


