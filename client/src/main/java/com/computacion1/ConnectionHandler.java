package com.computacion1;

import com.computacion1.UserInput.UserInputHandler;
import com.computacion1.audio.AudioManager;
import com.computacion1.call.CallHandler;
import com.google.gson.Gson;

import models.headers.CallHeader;
import models.headers.Header;
import models.headers.HeaderUserMessage;
import models.headers.HeaderServerInformation;
import models.headers.HeaderServerMessage;
import models.headers.HeaderUser;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class ConnectionHandler implements Runnable {
    private Gson gson;
    private Socket connection;
    private PrintWriter out;
    private AudioManager audioManager;
    private UserInputHandler userInput;
    private CallHandler callHanlder;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9876;
    private boolean isConnected = true;

    public ConnectionHandler(UserInputHandler userInput) throws IOException {
        this.connection = new Socket(SERVER_ADDRESS, SERVER_PORT);
        this.out = new PrintWriter(connection.getOutputStream(), true);
        this.audioManager = new AudioManager();
        this.gson = new Gson();
        this.userInput = userInput;
        this.callHanlder = new CallHandler();
    }

    public void sendConnectMessage(String username) {
        new Thread(() -> {
            HeaderUserMessage headerUserMessage = new HeaderUserMessage("TEXT", "CONNECT", username, 0);
            String connectMessage = gson.toJson(headerUserMessage);
            out.println(connectMessage);
        }).start();
    }

    private void handleServerInformation(HeaderServerInformation header) {
        System.out.print("Server: ");
        if (header.getStatus().equals(200)) {
            System.out.println(header.getMessage());
        } else {

            System.out.println("ERROR " + header.getError() + "/" + header.getMessage());
        }
    }

    private void handleServerMessage(HeaderServerMessage header, byte[] payload) {
        if (header.getType().equals("TEXT")) {
            String msg = "From ";
            if (!header.getGroup().equalsIgnoreCase("")) {
                msg = header.getGroup() + "/";
            }
            String message = new String(payload);
            System.out.println(msg + header.getSender() + ": " + message);
        } else if (header.getType().equals("AUDIO")) {
            System.out.println("You have received a voice message from " + header.getSender() + ".");
            System.out.println("Do you want to play it? Y: Yes / N: No");

            // Esperar la respuesta del usuario
            try {
                String userChoice = userInput.getUserInput("").trim();

                if ("Y".equals(userChoice)) {
                    audioManager.playVoiceMessage(payload);
                } else {
                    System.out.println("Audio ignored. Press Enter to continue");
                }
                Client.setDealingWithAudio(false);
            } catch (InterruptedException e) {
                System.out.println("Error reading input: " + e.getMessage());
                e.printStackTrace();
            }
        }

    }

    private void handleServerCall(CallHeader header) {
        System.out.println("HEADER RECEIVED: " + header);
        if (header.getMethod().equalsIgnoreCase("NEW_CALL")) {
            System.out.println("You have a new call from " + header.getCaller());
            System.out.println("Do you want to answer? (yes/no)");
            try {
                String userChoice = userInput.getPriorityThreadSpecificInput("").trim();
                if (userChoice.equalsIgnoreCase("yes")) {
                    acceptCall(header.getCaller());
                    callHanlder.startCall(userInput, header.getUDP_PORT());
                    leaveCall(header.getCaller());
                } else {
                    denyCall(header.getCaller());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (header.getMethod().equalsIgnoreCase("CALL_ACCEPTED")) {
            System.out.println("The user accepted your call!");
            callHanlder.startCall(userInput, header.getUDP_PORT());
            leaveCall(header.getCaller());
        } else if (header.getMethod().equalsIgnoreCase("CALLED_DENIED")) {
            System.out.println("The user denied your call");
            leaveCall(header.getCaller());
        }
    }

    private void acceptCall(String caller) {
        CallHeader headerUserMessage = new CallHeader("ACCEPT_CALL", callHanlder.getUDP_PORT(), caller);
        headerUserMessage.setUDP_PORT(callHanlder.getUDP_PORT());
        String jsonMessage = gson.toJson(headerUserMessage, CallHeader.class);
        out.println(jsonMessage);
    }

    private void denyCall(String caller) {
        CallHeader headerUserMessage = new CallHeader("NEGATE_CALL", callHanlder.getUDP_PORT(), caller);
        headerUserMessage.setUDP_PORT(callHanlder.getUDP_PORT());
        String jsonMessage = gson.toJson(headerUserMessage, CallHeader.class);
        out.println(jsonMessage);
    }

    private void leaveCall(String caller) {
    }

    private Header readHeader(BufferedReader is) throws IOException {
        String header;
        while ((header = is.readLine()) != null) {
            if (!header.equalsIgnoreCase("")) {
                if (header.contains("CALL_HEADER")) {
                    return gson.fromJson(header, CallHeader.class);
                }
                if (header.contains("status")) {
                    return gson.fromJson(header, HeaderServerInformation.class);
                } else {
                    return gson.fromJson(header, HeaderServerMessage.class);
                }
            }
        }
        return null;

    }

    private byte[] readPayload(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int byteRead;

        while ((byteRead = is.read()) != -1) { // Read byte by byte
            buffer.write(byteRead); // Write byte to buffer
            if (byteRead == '\n') { // Check if newline character is found
                break;
            }
        }

        return buffer.toByteArray(); // Return the byte array
    }

    private byte[] readAudioPayload(InputStream is, int fileSize) throws IOException {
        long bytesReceived = 0;
        byte[] buffer = new byte[1024]; // Buffer size can be adjusted based on needs
        ByteArrayOutputStream wavDataBuffer = new ByteArrayOutputStream();

        // Continuously read from InputStream until all expected bytes are received
        while (bytesReceived < fileSize) {
            int bytesRead = is.read(buffer);
            if (bytesRead == -1) {
                break; // End of stream reached
            }
            wavDataBuffer.write(buffer, 0, bytesRead);
            bytesReceived += bytesRead;
        }

        // Log the amount of data read
        System.out.println("Data length read: " + wavDataBuffer.toByteArray().length + " bytes");
        return wavDataBuffer.toByteArray();
    }

    @Override
    public void run() {
        try (InputStream is = this.connection.getInputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            while (!connection.isClosed()) {
                // Read the header
                Header header = readHeader(reader);
                System.out.println("header: " + header);
                if (header == null || !isConnected) {
                    break;
                }
                if (header instanceof HeaderServerInformation) {
                    handleServerInformation((HeaderServerInformation) header);

                } else if (header instanceof HeaderServerMessage) {
                    byte[] payload;
                    if (((HeaderServerMessage) header).getType().equalsIgnoreCase("AUDIO")) {
                        payload = readAudioPayload(is, ((HeaderServerMessage) header).getDataLenght());
                    } else {

                        payload = readPayload(is);
                    }
                    handleServerMessage((HeaderServerMessage) header, payload);
                } else if (header instanceof HeaderUser) {
                    System.out.println("DEALING WITH CALL HEADER");
                    handleServerCall(((CallHeader) header));
                }

            }
        } catch (IOException e) {
            System.out.println("ERROR READING THE SERVER MESSAGE ");
            e.printStackTrace();
        }
    }

    public void sendMessageToIndividualUser(String userRecipient, String message) {
        new Thread(() -> {
            HeaderUserMessage headerUserMessage = new HeaderUserMessage("TEXT", "MESSAGE", userRecipient,
                    message.getBytes().length);
            String jsonMessage = gson.toJson(headerUserMessage, HeaderUserMessage.class);

            try {
                out.println(jsonMessage);
                TimeUnit.SECONDS.sleep(1);
                out.println(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();
    }

    public void createNewGroup(String groupName, String invited) {
        HeaderUserMessage headerUserMessage = new HeaderUserMessage("TEXT", "CREATE", groupName,
                invited.getBytes().length);
        String jsonMessage = gson.toJson(headerUserMessage, HeaderUserMessage.class);

        try {
            out.println(jsonMessage);
            TimeUnit.SECONDS.sleep(1);
            out.println(invited);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void sendMessageToGroup(String groupName, String message) {

        new Thread(() -> {
            HeaderUserMessage headerUserMessage = new HeaderUserMessage("TEXT", "GROUP_MESSAGE", groupName,
                    message.getBytes().length);
            String jsonMessage = gson.toJson(headerUserMessage, HeaderUserMessage.class);

            try {
                out.println(jsonMessage);
                TimeUnit.SECONDS.sleep(1);
                out.println(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();
    }

    public void sendVoiceMessageToUser(String voiceRecipient, String time) {
        byte[] audio = audioManager.recordAudio(Integer.valueOf(time));

        HeaderUserMessage headerUserMessage = new HeaderUserMessage("AUDIO", "VOICE", voiceRecipient,
                audio.length);
        String jsonMessage = gson.toJson(headerUserMessage, HeaderUserMessage.class);
        System.out.println("AUDIO LENGHT " + audio.length);
        try {
            out.println(jsonMessage);
            TimeUnit.SECONDS.sleep(1);
            try {
                connection.getOutputStream().write(audio);
            } catch (IOException e) {
                e.printStackTrace();
            }
            out.println(audio);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sendVoiceMessageToGroup(String voiceGroup, String time) {

        byte[] audio = audioManager.recordAudio(Integer.valueOf(time));

        HeaderUserMessage headerUserMessage = new HeaderUserMessage("AUDIO", "GROUP_VOICE", voiceGroup,
                audio.length);
        String jsonMessage = gson.toJson(headerUserMessage, HeaderUserMessage.class);
        System.out.println("AUDIO LENGHT " + audio.length);
        try {
            out.println(jsonMessage);
            TimeUnit.SECONDS.sleep(1);
            try {
                connection.getOutputStream().write(audio);
            } catch (IOException e) {
                e.printStackTrace();
            }
            out.println(audio);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void callUser(String callRecipient) {
        CallHeader headerUserMessage = new CallHeader("CALL_USER", callHanlder.getUDP_PORT(), "", callRecipient);
        headerUserMessage.setUDP_PORT(callHanlder.getUDP_PORT());
        String jsonMessage = gson.toJson(headerUserMessage, CallHeader.class);
        out.println(jsonMessage);
    }

    public void callGroup(String callRecipient) {

        CallHeader headerUserMessage = new CallHeader("CALL_GROUP", callHanlder.getUDP_PORT(), "", callRecipient);
        headerUserMessage.setUDP_PORT(callHanlder.getUDP_PORT());
        String jsonMessage = gson.toJson(headerUserMessage, CallHeader.class);
        out.println(jsonMessage);
    }

    public void viewHistory() {

        HeaderUserMessage headerUserMessage = new HeaderUserMessage("TEXT", "VIEW_HISTORY", "", 0);
        String jsonMessage = gson.toJson(headerUserMessage, HeaderUserMessage.class);
        out.println(jsonMessage);
    }

    public void disconect() {
        this.isConnected = true;
        System.exit(0);
    }
}