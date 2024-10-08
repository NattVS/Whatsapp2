package com.computacion1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.computacion1.Chat.Chatters;

import io.github.cdimascio.dotenv.Dotenv;

public class Main {

    public static Chatters chaters = new Chatters();
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) throws NumberFormatException, IOException {
        // Load .env file to get the enviroment variables
        Dotenv dotenv = Dotenv.load();
        // Get the PORT enviroment variable
        int PORT = Integer.valueOf(dotenv.get("PORT-TCP"));
        // Start the server
        ServerSocket server = new ServerSocket(PORT);

        logger.info("SERVER RUNNING IN PORT " + PORT);

        ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // THIS LOGIC HERE WILL JUST HANDLE THE SERVER SHUTDOWN BY ANY REASON
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    Thread.sleep(200);
                    logger.info("SHUTTING DOWN SERVER, CLOSING ALL CONECTIONS");
                    chaters.shutdown();
                    server.close();
                } catch (InterruptedException | IOException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        });

        while (true) {
            Socket socket = server.accept();
            try {
                ClientHandler clientHandler = new ClientHandler(socket);
                pool.execute(clientHandler);
            } catch (IOException | IllegalArgumentException e) {
                logger.log(Level.SEVERE, "ERROR CREATING THE CLIENT!", e);
            }
        }
    }

}
