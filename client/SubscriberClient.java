import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SubscriberClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 4321);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Conectado al servidor en el puerto 12345");

            // Hilo para recibir mensajes del servidor
            new Thread(() -> {
                String fromServer;
                try {
                    while ((fromServer = in.readLine()) != null) {
                        System.out.println(fromServer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Enviar mensajes al servidor
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);  // Enviar al servidor
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
