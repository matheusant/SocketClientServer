package com.example.salvadesktop;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    private static final int SELECT_PICTURE = 1;

    private String selectedImagePath;
    private ImageView img;
    String basePath = android.os.Environment.getExternalStorageDirectory().toString();
    Button btnClear;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnClear = findViewById(R.id.btnClear);

        System.out.println("34");
        img = (ImageView) findViewById(R.id.ivPic);
        System.out.println("36");
        ((Button) findViewById(R.id.bBrowse))
                .setOnClickListener(new OnClickListener() {
                    public void onClick(View arg0) {
                        System.out.println("40");
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(
                                Intent.createChooser(intent, "Select Picture"),
                                SELECT_PICTURE);
                        System.out.println("47");
                    }
                });
        ;
        System.out.println("51");
        Button send = (Button) findViewById(R.id.bSend);
        final TextView status = (TextView) findViewById(R.id.tvStatus);

        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                /*File myFile = new File (selectedImagePath);
                Tasks tasks = new Tasks();
                tasks.execute(myFile);*/

                new Connecting().execute();

                /*final Socket[] sock = new Socket[1];
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sock[0] = new Socket("192.168.0.70", 8000);
                            File myFile = new File(selectedImagePath);
                            byte[] mybytearray = new byte[(int) myFile.length()];
                            FileInputStream fis = new FileInputStream(myFile);
                            BufferedInputStream bis = new BufferedInputStream(fis);
                            bis.read(mybytearray, 0, mybytearray.length);
                            OutputStream os = sock[0].getOutputStream();
                            System.out.println("Sending...");
                            os.write(mybytearray, 0, mybytearray.length);
                            os.flush();
                            sock[0].close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Connecting...");

                    }
                });
                thread.start();*/

            }
        });

        btnClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String filePath = basePath + "/FRASES/";
                File sdFile = new File(filePath);
                for (String foto : sdFile.list()){
                    new File(filePath+foto).delete();
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                TextView path = (TextView) findViewById(R.id.tvPath);
                path.setText("Image Path : " + selectedImagePath);
                img.setImageURI(selectedImageUri);
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    class Tasks extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... voids) {
            Socket sock;
            try {
                String myFile = voids[0];
                sock = new Socket("192.168.0.70", 8000);
                System.out.println("Connecting...");


                // sendfile

                byte[] mybytearray = new byte[1024];
                FileInputStream fis = new FileInputStream(myFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(mybytearray, 0, mybytearray.length);
                OutputStream os = sock.getOutputStream();
                System.out.println("Sending...");
                os.write(mybytearray, 0, mybytearray.length);
                os.flush();

                sock.close();
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return voids[0];
        }
    }

    class Connecting extends AsyncTask<String, Integer, Boolean> {
        Socket client = null;
        Socket s = null;
        OutputStream outputStream = null;
        boolean sucesso = false;
        DataOutputStream dataOutputStream = null;

        @Override
        protected Boolean doInBackground(String... strings) {
            String filePath = basePath + "/IMI/upload/";
            File sdFile = new File(filePath);

            try {
                for (String fotos : sdFile.list()) {
                    s = new Socket("192.168.0.70", 7800);
                    client = new Socket("192.168.0.70", 8000);
                    outputStream = client.getOutputStream();
                    dataOutputStream = new DataOutputStream(s.getOutputStream());
                    dataOutputStream.writeUTF(fotos);
                    dataOutputStream.flush();
                    dataOutputStream.close();
                    //DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                    FileInputStream in = new FileInputStream(filePath + fotos);
                    byte[] buffer = new byte[1024];
                    BufferedInputStream bis = new BufferedInputStream(in);
                    int rBytes;
                    while ((rBytes = bis.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, rBytes);
                    }

                    in.close();

                    outputStream.flush();
                    outputStream.close();
                    client.close();


                    File file = new File(filePath + fotos);
                    if (file.exists()) file.delete();

                }
                sucesso = true;

            } catch (UnknownHostException e) {
                e.printStackTrace();
                sucesso = false;
            } catch (IOException e) {
                e.printStackTrace();
                sucesso = false;
            }

            return sucesso;

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
            if (aBoolean){
                dlg
                        .setTitle("Atenção")
                        .setMessage("Fotos enviadas")
                        .show();
            }else{

                dlg
                        .setTitle("Atenção")
                        .setMessage("Fotos não enviadas\nCheque sua conexão")
                        .show();
            }
        }
    }
}