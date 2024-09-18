import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;

import com.computacion1.Chatters;
import com.computacion1.ClientHandler;
import com.computacion1.RoomsController;

import models.ChatRoom;
import models.Client;

import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;
import java.io.IOException;
import java.io.OutputStream;

public class SocketTests {

    @Test
    public void testClientConnection() throws IOException {
        // Mock the socket connection and output stream
        Socket mockSocket = mock(Socket.class);
        OutputStream mockOutputStream = mock(OutputStream.class);
        InetAddress mockInetAddress = mock(InetAddress.class);

        // Mock socket behavior
        when(mockSocket.getOutputStream()).thenReturn(mockOutputStream);
        when(mockSocket.getInetAddress()).thenReturn(mockInetAddress);
        when(mockInetAddress.getHostAddress()).thenReturn("127.0.0.1");
        when(mockSocket.getPort()).thenReturn(12345);

        // Create the ClientHandler and run it
        ClientHandler clientHandler = new ClientHandler(mockSocket);
        clientHandler.connect("CONNECT~~user1");

        // Verify that the connection is handled correctly
        verify(mockSocket).getOutputStream();
        verify(mockOutputStream, never()).write("ERROR".getBytes());
    }

    @Test
    public void testBroadcastMessage() throws IOException {
        // Mock the Chatters and socket
        Chatters mockChatters = mock(Chatters.class);
        Socket mockSocket = mock(Socket.class);
        OutputStream mockOutputStream = mock(OutputStream.class);
        InetAddress mockInetAddress = mock(InetAddress.class);

        // Mock socket behavior
        when(mockSocket.getOutputStream()).thenReturn(mockOutputStream);
        when(mockSocket.getInetAddress()).thenReturn(mockInetAddress);
        when(mockInetAddress.getHostAddress()).thenReturn("127.0.0.1");
        when(mockSocket.getPort()).thenReturn(12345);

        // Create ClientHandler and simulate connection
        ClientHandler clientHandler = new ClientHandler(mockSocket);
        clientHandler.connect("CONNECT~~user1");

        // Simulate sending a broadcast message
        String broadcastMessage = "BROADCAST~~Hello everyone!";
        clientHandler.handleMessage(broadcastMessage.getBytes());

        // Verify that the message is broadcasted
        verify(mockChatters, times(1)).broadCastMessage("Hello everyone!");
    }

    @Test
    public void testPrivateMessage() throws IOException {
        // Mock the Chatters, socket, and output stream
        Chatters mockChatters = mock(Chatters.class);
        Socket mockSocket = mock(Socket.class);
        OutputStream mockOutputStream = mock(OutputStream.class);
        InetAddress mockInetAddress = mock(InetAddress.class);

        // Mock socket behavior
        when(mockSocket.getOutputStream()).thenReturn(mockOutputStream);
        when(mockSocket.getInetAddress()).thenReturn(mockInetAddress);
        when(mockInetAddress.getHostAddress()).thenReturn("127.0.0.1");
        when(mockSocket.getPort()).thenReturn(12345);

        // Create ClientHandler and simulate connection
        ClientHandler clientHandler = new ClientHandler(mockSocket);
        clientHandler.connect("CONNECT~~user1");

        // Mock receiving a private message
        String privateMessage = "MESSAGE~~user2~~Hello!";
        clientHandler.handleMessage(privateMessage.getBytes());

        // Verify that the private message is sent correctly
        verify(mockChatters, times(1)).sendPrivateMessage("user1", "user2", "Hello!");
    }

    @Test
    public void testMultipleClientsInChatRoom() throws IOException {
        // Mock Chatters, RoomsController, sockets, and output streams
        Chatters mockChatters = mock(Chatters.class);
        RoomsController mockRoomsController = mock(RoomsController.class);
        Socket mockSocket1 = mock(Socket.class);
        Socket mockSocket2 = mock(Socket.class);
        OutputStream mockOutputStream1 = mock(OutputStream.class);
        OutputStream mockOutputStream2 = mock(OutputStream.class);
        InetAddress mockInetAddress1 = mock(InetAddress.class);
        InetAddress mockInetAddress2 = mock(InetAddress.class);

        // Mock socket behavior
        when(mockSocket1.getOutputStream()).thenReturn(mockOutputStream1);
        when(mockSocket2.getOutputStream()).thenReturn(mockOutputStream2);
        when(mockSocket1.getInetAddress()).thenReturn(mockInetAddress1);
        when(mockSocket2.getInetAddress()).thenReturn(mockInetAddress2);
        when(mockInetAddress1.getHostAddress()).thenReturn("127.0.0.1");
        when(mockInetAddress2.getHostAddress()).thenReturn("127.0.0.2");
        when(mockSocket1.getPort()).thenReturn(12345);
        when(mockSocket2.getPort()).thenReturn(12346);

        // Create two client handlers
        ClientHandler clientHandler1 = new ClientHandler(mockSocket1);
        ClientHandler clientHandler2 = new ClientHandler(mockSocket2);

        // Simulate both clients connecting
        clientHandler1.connect("CONNECT~~user1");
        clientHandler2.connect("CONNECT~~user2");

        // Simulate creating and joining a room
        String createRoomCommand = "CREATE~~chatRoom~~user2";
        clientHandler1.handleMessage(createRoomCommand.getBytes());

        // Verify that a new chat room is created and users are added
        verify(mockRoomsController, times(1)).createNewChatRoom("chatRoom");
        verify(mockChatters, times(1)).addClientToMainRoom(clientHandler1);
        verify(mockChatters, times(1)).addClientToMainRoom(clientHandler2);
    }

    @Test
    public void testCreateGroupChatRoom() throws IOException {
        // Mock Chatters, RoomsController, socket, and output stream
        Chatters mockChatters = mock(Chatters.class);
        RoomsController mockRoomsController = mock(RoomsController.class);
        Socket mockSocket = mock(Socket.class);
        OutputStream mockOutputStream = mock(OutputStream.class);
        InetAddress mockInetAddress = mock(InetAddress.class);

        // Mock socket behavior
        when(mockSocket.getOutputStream()).thenReturn(mockOutputStream);
        when(mockSocket.getInetAddress()).thenReturn(mockInetAddress);
        when(mockInetAddress.getHostAddress()).thenReturn("127.0.0.1");
        when(mockSocket.getPort()).thenReturn(12345);

        // Create ClientHandler and simulate connection
        ClientHandler clientHandler = new ClientHandler(mockSocket);
        clientHandler.connect("CONNECT~~user1");

        // Simulate the creation of a new chat room
        String createRoomCommand = "CREATE~~chatRoom~~user2";
        clientHandler.handleMessage(createRoomCommand.getBytes());

        // Verify that the new chat room is created
        verify(mockRoomsController, times(1)).createNewChatRoom("chatRoom");

        // Verify that users are added to the room
        verify(mockChatters.getRooms(), times(1)).addMultipleUsersToRoom(any(ChatRoom.class), eq(new String[]{"user2"}));
    }

    @Test
    public void testSendMessageToGroupChat() throws IOException {
        // Mock RoomsController, ChatRoom, socket, client, and output stream
        RoomsController mockRoomsController = mock(RoomsController.class);
        ChatRoom mockChatRoom = mock(ChatRoom.class);
        Socket mockSocket = mock(Socket.class);
        Client mockClient = mock(Client.class);
        OutputStream mockOutputStream = mock(OutputStream.class);
        InetAddress mockInetAddress = mock(InetAddress.class);

        // Mock socket and client behavior
        when(mockSocket.getOutputStream()).thenReturn(mockOutputStream);
        when(mockSocket.getInetAddress()).thenReturn(mockInetAddress);
        when(mockInetAddress.getHostAddress()).thenReturn("127.0.0.1");
        when(mockSocket.getPort()).thenReturn(12345);
        when(mockClient.getRoomByName("chatRoom")).thenReturn(UUID.randomUUID());
        when(mockRoomsController.getRoomByID(any(UUID.class))).thenReturn(mockChatRoom);

        // Create ClientHandler for two users
        ClientHandler clientHandler1 = new ClientHandler(mockSocket);
        ClientHandler clientHandler2 = new ClientHandler(mockSocket);

        // Connect the clients
        clientHandler1.connect("CONNECT~~user1");
        clientHandler2.connect("CONNECT~~user2");

        // Simulate both clients joining the room
        String createRoomCommand = "CREATE~~chatRoom~~user2";
        clientHandler1.handleMessage(createRoomCommand.getBytes());

        // Simulate sending a message to the group
        String groupMessage = "GROUP_MESSAGE~~chatRoom~~Hello Group!";
        clientHandler1.handleMessage(groupMessage.getBytes());

        // Verify that the message was sent to all users in the room
        verify(mockChatRoom, times(1)).sendMessage("Hello Group!");
    }
}

