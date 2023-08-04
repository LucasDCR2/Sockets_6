package com.example.desafio_6_sockets.Controller;

import android.net.Uri;
import android.nfc.Tag;
import android.util.Base64;
import android.util.Log;

import com.example.desafio_6_sockets.View.MainActivity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

    //===============================================================================< Class >=================================================================================//

public class ChatClient extends Thread {
    private static final String SERVER_IP = "191.220.202.174"; // Endereço IP do servidor de casa:
    //private static final String SERVER_IP = "192.168.7.214"; // Endereço de IP do servidor da Imply:
    private static final int SERVER_PORT = 4000; // Porta do servidor
    private static final String TAG = "Gabes";
    private BufferedWriter out;
    private BufferedReader in;
    private Socket clientSocket;
    private MainActivity mainActivity;

    //================================================================================< Main >==================================================================================//

    public ChatClient (MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        boolean conected = false;
        while (!conected) {
            try {
                this.clientSocket = new Socket(SERVER_IP, SERVER_PORT);
                // Para enviar mensgagens para o servidor
                this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
                // Receber resposta do servidor
                this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                receiveMessageLoop(clientSocket);
                mainActivity.reloadServerStatus("Online");
                conected = true;
            } catch (IOException e) {
                mainActivity.reloadServerStatus("Offline");
                e.printStackTrace();
            } try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    //====================================================================< Receber Mensagem >=================================================================================//

    public void receiveMessageLoop(Socket clientSocket) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String serverResponse = in.readLine();
                        if (serverResponse != null) {
                            handleMessage(serverResponse);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        thread.start();
    }

    private void handleMessage(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            String type = parts[0];
            String content = parts[1].trim();

            if ("TEXT".equals(type)) {
                // Process text message
                mainActivity.showReceiveMessage(content);
            } else if ("IMAGE".equals(type)) {
                // Process image message
                byte[] imageData = Base64.decode(content, Base64.DEFAULT);
                mainActivity.showReceiveImage(imageData);
            } else {
                // Unknown type
                System.out.println("Received message with unknown type: " + message);
            }
        } else {
            // Message with invalid or unknown format
            System.out.println("Received message with invalid format: " + message);
        }
    }

    //=====================================================================< Enviar Mensagem >=================================================================================//

    public void sendMessageWithType(String type, String message) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run(){
                try {
                    String fullMessage = type + ":" + message;
                    Log.i(TAG, "Mensagem a ser enviada: " + fullMessage);
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

    public void sendImage(byte[] imageData) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String messageType = "IMAGE";
                    String message = messageType + ":";
                    out.write(message);
                    out.flush();

                    OutputStream outputStream = clientSocket.getOutputStream();
                    outputStream.write(imageData);
                    outputStream.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
    }





    //===============================================================================< Close >=================================================================================//

    public void close(){
        try {
            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException e){
            Log.e(TAG, e.getMessage());
        }
    }

    public Uri decodeBase64ToUri (String fileBase64) {

        Log.w(TAG, "Imagem em base 64: " + fileBase64);
        String type = "";
        //if (fileBase64.startsWith("image")) {
            type = "image";
            fileBase64 = fileBase64.replace("image ", "");
            fileBase64 = fileBase64.replace(" END_OF_IMAGE", "");
        //}

        //Log.i(TAG, "oi: " + fileBase64);

        String fullImage = "data:image/jpg;base64," + fileBase64;

        Uri imageUri = Uri.parse(fullImage);
        //Log.e(TAG, "return: " + imageUri);

        return imageUri;
    }
}

//=====================================================================================< Fim >=================================================================================//
