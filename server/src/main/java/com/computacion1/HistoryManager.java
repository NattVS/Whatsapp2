package com.computacion1;

import com.google.gson.Gson;
import models.ChatMessage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class HistoryManager {
    private static final String HISTORY_FOLDER = "chat_history/";
    private static Gson gson = new Gson();

    public static void createUserHistoryFile(String username, String userIP) throws IOException {
        String filename = username + "_" + userIP.replace(".", "_") + "_history.json";
        File userHistoryFile = new File(HISTORY_FOLDER + filename);

        if (!userHistoryFile.exists()) {
            userHistoryFile.getParentFile().mkdirs();
            userHistoryFile.createNewFile();

            try (FileWriter writer = new FileWriter(userHistoryFile)) {
                writer.write("[]");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void saveMessage(String username, String userIP, String receiver, String content, String type) throws IOException {
        ChatMessage message = new ChatMessage(username, receiver, content, type);
        String filename = HISTORY_FOLDER + username + "_" + userIP.replace(".", "_") + "_history.json";
        ArrayList<ChatMessage> history = getHistory(username, userIP);
        history.add(message);
        String json = gson.toJson(history);
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            fos.write(json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<ChatMessage> getHistory(String username, String userIP) {
        String filename = HISTORY_FOLDER + username + "_" + userIP.replace(".", "_") + "_history.json";
        ArrayList<ChatMessage> chatMessages = new ArrayList<>();

        try (FileInputStream fs = new FileInputStream(filename);
             BufferedReader br = new BufferedReader(new InputStreamReader(fs))) {

            String json = br.readLine();

            if (json != null && !json.trim().isEmpty()) {
                ChatMessage[] array = gson.fromJson(json, ChatMessage[].class);
                chatMessages = new ArrayList<>(Arrays.asList(array));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return chatMessages;
    }



}
