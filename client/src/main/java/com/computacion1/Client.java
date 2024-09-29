package com.computacion1;

import java.io.IOException;

import com.computacion1.UserInput.UserInputHandler;

public class Client {

    private static ConnectionHandler connectionHandler;
    private static UserInputHandler userInputHandler;
    private static boolean dealingWithAudio;

    public static void main(String[] args) throws InterruptedException {
        userInputHandler = new UserInputHandler();

        System.out.print("Enter your username: ");
        String username = userInputHandler.getUserInput("").trim();

        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }

        // Handle all the logic of the client
        new Thread(() -> {
            try {
                connectionHandler = new ConnectionHandler(userInputHandler);
                connectionHandler.sendConnectMessage(username);
                connectionHandler.run();
                // connection.waitForServerResponse();
            } catch (IOException e) {
                System.out.println("THERE WAS AN ERROR CREATING THE CONNECTION HERE IS WHAT I KNOW: ");
                e.printStackTrace();
            }
        }).start();

        // Menu loop
        String choice;
        while (true) {

            if (!dealingWithAudio){
                displayMenu();
            }

            choice = userInputHandler.getUserInput("Enter your choice: ").trim();

            switch (choice) {
                case "1" -> { // Create group
                    String groupName = userInputHandler.getUserInput("Enter group name: ").trim();
                    if (groupName.isEmpty()) {
                        System.out.println("Group name cannot be empty.");
                    } else {
                        String invited = userInputHandler.getUserInput(
                                "Please enter the name of the people you want to invite separated by commas. Ex: juan,ana,maria: ")
                                .trim();

                        if (invited.isEmpty()) {
                            System.out.println("You can't create an empty group");
                        } else {
                            connectionHandler.createNewGroup(groupName, invited);
                        }
                    }
                }
                case "2" -> { // Send a text message to a user
                    String userRecipient = userInputHandler.getUserInput("Enter recipient (user): ").trim();
                    if (userRecipient.isEmpty()) {
                        System.out.println("Recipient cannot be empty.");
                        break;
                    }
                    String message = userInputHandler.getUserInput("Enter message: ").trim();
                    if (message.isEmpty()) {
                        System.out.println("Message cannot be empty.");
                    } else {
                        connectionHandler.sendMessageToIndividualUser(userRecipient, message);
                    }

                }
                case "3" -> { // Send a text message to a group
                    System.out.print("Enter group name: ");
                    String groupRecipient = userInputHandler.getUserInput("Enter group name: ").trim();
                    if (groupRecipient.isEmpty()) {
                        System.out.println("Group name cannot be empty.");
                    } else {
                        String groupMessage = userInputHandler.getUserInput("Enter message: ").trim();
                        if (groupMessage.isEmpty()) {
                            System.out.println("Message cannot be empty.");
                        } else {
                            connectionHandler.sendMessageToGroup(groupRecipient, groupMessage);
                        }
                    }
                }
                case "4" -> { // Send voice message to user
                    String voiceRecipient = userInputHandler.getUserInput("Enter recipient (user): ").trim();
                    if (voiceRecipient.isEmpty()) {
                        System.out.println("Recipient cannot be empty.");
                    } else {
                        String time = userInputHandler.getUserInput("How long do you want to record?").trim();
                        connectionHandler.sendVoiceMessageToUser(voiceRecipient, time);
                    }
                }
                case "5" -> {
                    String callRecipient = userInputHandler.getUserInput("Enter recipient (group): ").trim();
                    if (callRecipient.isEmpty()) {
                        System.out.println("Recipient cannot be empty.");
                    } else {

                        String time = userInputHandler.getUserInput("How long do you want to record?").trim();
                        connectionHandler.sendVoiceMessageToGroup(callRecipient, time);
                    }
                } // voice message to group
                case "6" -> { // Make a call to a user
                    String callRecipient = userInputHandler.getUserInput("Who do you wanna call (user): ").trim();
                    if (callRecipient.isEmpty()) {
                        System.out.println("Recipient cannot be empty.");
                    } else {
                        connectionHandler.callUser(callRecipient);
                    }
                }
                case "7" -> {// Make a call to a group

                    String callRecipient = userInputHandler.getUserInput("Who do you wanna call (group): ").trim();
                    if (callRecipient.isEmpty()) {
                        System.out.println("Recipient cannot be empty.");
                    } else {
                        connectionHandler.callGroup(callRecipient);
                    }
                }
                case "8" -> { // View message history
                    connectionHandler.viewHistory();

                }
                case "9" -> { // Exit
                    System.out.println("Disconnected from server.");
                    connectionHandler.disconect();
                    return;
                }
                case "Y", "N" -> {
                    System.out.println("Again:");
                    dealingWithAudio = true;
                }
                case "" -> {
                    System.out.println();
                }
                default -> {
                    System.out.println("Invalid choice. Please enter a number between 1 and 7.");
                }
            }
        }

    }

    public static void setDealingWithAudio(boolean b){
        dealingWithAudio = b;
    }

    private static void displayMenu() {
        System.out.println("Choose an action:");
        System.out.println("1. Create a chat group");
        System.out.println("2. Send a text message to a user");
        System.out.println("3. Send a text message to a group");
        System.out.println("4. Send a voice message to a user ");
        System.out.println("5. Send a voice message to a group");
        System.out.println("6. Make a call to a user");
        System.out.println("7. Make a call to a group");
        System.out.println("8. View message history");
        System.out.println("9. Exit");
        System.out.print("Enter your choice: ");
    }
}
