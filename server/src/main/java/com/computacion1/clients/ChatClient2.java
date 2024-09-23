package com.computacion1.clients;

import com.computacion1.audio.AudioRecorderMain;
import com.computacion1.audio.VoiceMessagePlayer;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import models.headers.HeaderServer;
import models.headers.HeaderServerInformation;
import models.headers.HeaderServerMessage;
import models.headers.HeaderUserMessage;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;

public class ChatClient2 {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9876;
    private static Gson gson = new Gson();
    private static HeaderServer currentHeader;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.print("Enter your username: ");
            String username = userInput.readLine();
            sendConnectMessage(out, username, "CONNECT");

            new Thread(() -> {
                String response;
                try {
                    while ((response = in.readLine()) != null) {
                        try {
                            if (response.contains("status")){
                                currentHeader = gson.fromJson(response, HeaderServerInformation.class);
                            } else {
                                currentHeader = gson.fromJson(response, HeaderServerMessage.class);
                            }
                        } catch (JsonSyntaxException e) {
                            handleServerResponse(currentHeader, response);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            String message;
            while (true) {
                message = userInput.readLine();
                if (message.equalsIgnoreCase("exit")) {
                    sendConnectMessage(out,null,"DISCONNECT");
                    break;
                }
                sendMessage(out, message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleServerResponse(HeaderServer header, String response){
        if (header instanceof HeaderServerInformation headerInfo){
            System.out.print("Server: ");
            if (headerInfo.getStatus().equals(200)){
                System.out.println(response);
            } else {
                System.out.println("ERROR " + headerInfo.getError() + "/" + response);
            }
        } else if (header instanceof HeaderServerMessage headerMsg){
            if (headerMsg.getType().equals("TEXT")){
                String msg = "From ";
                if (headerMsg.getGroup() != null){
                    msg = headerMsg.getGroup() + "/";
                }
                System.out.println(msg + headerMsg.getSender() + ": " + response);
            } else if (headerMsg.getType().equals("VOICE")){
                System.out.println("Voice message from " + headerMsg.getSender());
                //playVoiceMessage(response);
            }
        }
    }

    private static void sendConnectMessage(PrintWriter out, String username, String method) {
        HeaderUserMessage headerUserMessage = new HeaderUserMessage(null, method,username);
        String connectMessage = gson.toJson(headerUserMessage);
        out.println(connectMessage);
    }

    // Send messages to server
    private static void sendMessage(PrintWriter out, String message) {

        //SENDING HEADER
        String[] parts = message.split("~~");
        String method = parts[0];

        String type = "TEXT";
        HeaderUserMessage headerUserMessage;
        String jsonMessage;

        if (method.equals("VOICE") | method.equals("GROUP_VOICE")){
            parts [2] = recordVoiceMessage();
            type = "VOICE";
        } else if (method.equals("VIEW_HISTORY")){
            headerUserMessage = new HeaderUserMessage(type,method,null);
            jsonMessage = gson.toJson(headerUserMessage);
            out.println(jsonMessage);
        }

        if (!method.equals("VIEW_HISTORY")){
            headerUserMessage = new HeaderUserMessage(type, method,parts[1]);
            jsonMessage = gson.toJson(headerUserMessage);
            out.println(jsonMessage);

            //SENDING MESSAGE
            out.println(parts[2]);
        }
    }

    //Not working
    private void x(){
        try {
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String userResponse = userInput.readLine().trim().toUpperCase();
            if (userResponse.equals("Y")) {
                //playVoiceMessage(response);
            } else if (userResponse.equals("N")) {
                System.out.println("Voice message discarded.");
            } else {
                System.out.println("Invalid input. Please type Y or N.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String recordVoiceMessage() {
        byte[] audioData = AudioRecorderMain.recordAudio(5);
        return java.util.Base64.getEncoder().encodeToString(audioData);
    }

    private static void playVoiceMessage(String response){
        byte[] decodedAudio = Base64.getDecoder().decode(response);
        VoiceMessagePlayer player = new VoiceMessagePlayer(new AudioFormat(16000, 16, 1, true, true));
        player.initAudio(decodedAudio);
    }
}
