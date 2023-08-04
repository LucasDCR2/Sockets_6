package com.example.desafio_6_sockets.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.desafio_6_sockets.Controller.ChatClient;
import com.example.desafio_6_sockets.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

//==========================================================================< Main >=================================================================================//

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Gabes";
    private ChatClient chatClient;
    ImageButton imageButtonSend;
    EditText editTextMessage;
    ImageButton imageButtonAttach;
    androidx.appcompat.widget.Toolbar toolbarIndividualChat;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ChatClient socketClient;
    private AlertDialog alertDialog;

    //====================================================================< On Create >=================================================================================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbarIndividualChat = findViewById(R.id.toolbarIndividualChat);
        imageButtonSend = findViewById(R.id.imageButtonSend);
        editTextMessage = findViewById(R.id.editTextMessage);
        imageButtonAttach = findViewById(R.id.imageButtonAttach);

        // Inicia a conexão com o servidor Socket quando a Activity for criada
        socketClient = new ChatClient(this);
        socketClient.start();

        imageButtonAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAttachDialog();
            }
        });

        //==========================================================< Enviar Mensagem Layout >=================================================================================//

        imageButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = String.valueOf(editTextMessage.getText());
                editTextMessage.setText("");

                socketClient.sendMessageWithType("TEXT", message);

                LinearLayout layoutParent = findViewById(R.id.message_space);
                View layoutChild = getLayoutInflater().inflate(R.layout.inflate_own_msg, null);
                TextView textViewMessage = layoutChild.findViewById(R.id.txt_message);
                textViewMessage.setText(message);
                layoutParent.addView(layoutChild);

                ScrollView scrollView = findViewById(R.id.scrollViewMessages);
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

        //=====================================================================< Verificar Tipo >=================================================================================//
        ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (result.getData() != null) {
                            Uri selectedFileUri = result.getData().getData();

                            // Verifica se o arquivo selecionado é uma imagem
                            if (selectedFileUri != null && getContentResolver().getType(selectedFileUri).startsWith("image/")) {
                                // mostrar a imagem para quem está enviando
                                LinearLayout layoutParent = findViewById(R.id.message_space);
                                View layoutChild = getLayoutInflater().inflate(R.layout.inflate_img_own, null);
                                ImageView imageView = layoutChild.findViewById(R.id.img_inflate_own);
                                imageView.setImageURI(selectedFileUri);
                                layoutParent.addView(layoutChild);

                                // Send the image to the server
                                handleImage(selectedFileUri);
                            }
                        }
                    }
                }
        );
    }

        //================================================================< AnexarBTN OnClick >=================================================================================//

        private void showAttachDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View customLayout = LayoutInflater.from(MainActivity.this).inflate(R.layout.alert_dialog_custom, null);
            builder.setView(customLayout);

            LinearLayout buttonGaleria = customLayout.findViewById(R.id.button_galeria);
            buttonGaleria.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openGalleryIMG();
                    alertDialog.dismiss();
                }
            });

            LinearLayout buttonEnviarArquivo = customLayout.findViewById(R.id.button_enviar_arquivo);
            buttonEnviarArquivo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openGalleryFiles();
                    alertDialog.dismiss();
                }
            });

            LinearLayout buttonEnviarAudio = customLayout.findViewById(R.id.button_enviar_audio);
            buttonEnviarAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openGalleryAudio();
                    alertDialog.dismiss();
                }
            });

            alertDialog = builder.create();
            alertDialog.show();
        }


    //===============================================================< Abrir Galeria Imagens>=================================================================================//

    private void openGalleryIMG() {
        Log.d(TAG, "Botão de anexo clicado. Abrindo a galeria de imagens.");
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Inicie a atividade esperando um resultado usando o launcher para ActivityResult
        galleryLauncher.launch(intent);
    }

    //==============================================================< Abrir Galeria Arquivos >=================================================================================//

    // Método para abrir a galeria de arquivos
    private void openGalleryFiles() {
        Log.d(TAG, "Botão de anexo de arquivos clicado. Abrindo a galeria de arquivos.");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/*"); // Define o tipo de arquivo (todos)
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Inicie a atividade esperando um resultado usando o launcher para ActivityResult
        galleryLauncher.launch(intent);
    }

    //================================================================< Abrir Galeria Audios >=================================================================================//

    // Método para abrir a galeria de áudios
    private void openGalleryAudio() {
        Log.d(TAG, "Botão de anexo de áudio clicado. Abrindo a galeria de áudios.");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*"); // Define o tipo de arquivo como áudio
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Inicie a atividade esperando um resultado usando o launcher para ActivityResult
        galleryLauncher.launch(intent);
    }

    //===================================================================< Convert BASE64 >=================================================================================//

    private byte[] convertFileToByteArray(Uri fileUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(fileUri);
        byte[] fileBytes = IOUtils.toByteArray(inputStream);
        return fileBytes;
    }

    //========================================================================< OnStop >=================================================================================//

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "Finalizando o app");
        //socketClient.close();
    }

    //====================================================================< Mostrar Mensagem >=================================================================================//

    public void showReceiveMessage(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout layoutParent = findViewById(R.id.message_space);
                View layoutChild = getLayoutInflater().inflate(R.layout.inflate_other_msg, null);
                TextView textViewMessage = layoutChild.findViewById(R.id.txt_message);
                textViewMessage.setText(message);
                layoutParent.addView(layoutChild);

                ScrollView scrollView = findViewById(R.id.scrollViewMessages);
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public void showReceiveImage(byte[] imageData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout layoutParent = findViewById(R.id.message_space);
                View layoutChild = getLayoutInflater().inflate(R.layout.inflate_img_other, null);
                ImageView imageView = layoutChild.findViewById(R.id.img_inflate_other);

                // Convert byte array to Drawable or Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

                // Set the image to the ImageView
                imageView.setImageBitmap(bitmap); // Use setImageDrawable() if you have a Drawable

                layoutParent.addView(layoutChild);
            }
        });
    }


    public void reloadServerStatus(String status){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toolbarIndividualChat.setTitle(status);
                if (status.equals("Online")) {
                    toolbarIndividualChat.setTitleTextColor(0xFF1BF403);
                } else {
                    toolbarIndividualChat.setTitleTextColor(0xFFF4030B);
                }
            }
        });
    }

    private void handleImage(Uri selectedFileUri) {
        try {
            byte[] imageData = convertUriToByteArray(selectedFileUri);
            socketClient.sendImage(imageData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] convertUriToByteArray(Uri fileUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(fileUri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        return byteArrayOutputStream.toByteArray();
    }

}

//==============================================================================< Fim >=================================================================================//