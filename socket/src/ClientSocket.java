import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Base64;

public class ClientSocket {
    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;
    

    public ClientSocket(Socket socket) throws IOException{
        this.socket = socket;
        System.out.println("Cliente: " + socket.getRemoteSocketAddress() + " conectou ao servidor");
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
    }

    public SocketAddress getRemoteSocketAddress () {
        return socket.getRemoteSocketAddress();
    }

    public String getMessage() {
        try {
            return in.readLine();    
        } catch (IOException e) {
            return null;
        }
    }

    public boolean sendMessage(String message) {
        try {
            out.write(message);
            out.newLine();
            out.flush(); 
            return true;  
        } catch (Exception e) {
            return false;
        }
         
    }

    public void sendImage(byte[] imageData) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String encodedImage = Base64.getEncoder().encodeToString(imageData);
                    String fullMessage = "IMAGE:" + encodedImage;
                    out.write(fullMessage);
                    out.newLine();
                    out.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
    }

    public void close () {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            System.out.println("Erro ao realizar os close do socket: " + e.getMessage());
        }
        
    
    }
}