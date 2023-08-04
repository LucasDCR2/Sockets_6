import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Base64;
import java.io.IOException;

public class ChatServer {
    public static final int PORT = 4000;
    private ServerSocket serverSocket;
    private final ArrayList<ClientSocket> clients = new ArrayList<>();

    public void start () throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciado na porta: " + PORT);
        clientConectionLoop();
    }
    
    private void clientConectionLoop() throws IOException{
        while (true) {
            ClientSocket clientSocket = new ClientSocket(serverSocket.accept()); // esse méteodo no parentes retorna um socket ent eu pego ele
            clients.add(clientSocket);
            new Thread(() -> clientMessageLoop(clientSocket)).start();
        }
        
    }

    private void clientMessageLoop(ClientSocket clientSocket) {
        try {
            while (true) {
                String message = clientSocket.getMessage();
                if (message != null) {
                    String[] parts = message.split(":", 2);
                    if (parts.length == 2) {
                        String type = parts[0];
                        String content = parts[1].trim();
    
                        // Process the message based on its type
                        if ("TEXT".equals(type)) {
                            // Process text message
                            System.out.println("Received TEXT message: " + content);
                            sendMessageToAll(clientSocket, message); // Broadcast the message to all clients
                        } else if ("IMAGE".equals(type)) {
                            // Process image message
                            byte[] imageData = Base64.getDecoder().decode(content);
                            // Process the image data as needed
                            System.out.println("Received IMAGE message");
                            sendImageToAll(clientSocket, imageData); // Broadcast the image to all clients
                        } else {
                            // Unknown type
                            System.out.println("Received message with unknown type: " + message);
                        }
                    } else {
                        // Message with invalid or unknown format
                        System.out.println("Received message with invalid format: " + message);
                    }
                } else {
                    // Handle client disconnection
                    System.out.println("Client disconnected: " + clientSocket.getRemoteSocketAddress());
                    clients.remove(clientSocket);
                    clientSocket.close();
                    break; // Exit the loop to terminate the thread dedicated to this client
                }
            }
        } finally {
            // Handle client removal and close resources here if necessary
        }
    }
    
    private void sendMessageToAll(ClientSocket sender, String message) {
        for (ClientSocket clientSocket : clients) {
            if (!sender.equals(clientSocket)) {
                clientSocket.sendMessage(message);
            }
        }
    }
    
    private void sendImageToAll(ClientSocket sender, byte[] imageData) {
        for (ClientSocket clientSocket : clients) {
            if (!sender.equals(clientSocket)) {
                clientSocket.sendImage(imageData);
            }
        }
    }
    

    public static void main(String[] args) {
        try {
            ChatServer server = new ChatServer();
        server.start();
        } catch (Exception e) {
            System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
        System.out.println("Servidor finalizado");
    }



}