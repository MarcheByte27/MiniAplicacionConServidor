package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;


public class MainActivity extends AppCompatActivity {

    protected static String server = "192.168.31.215";
    protected static int port = 6060;
    PrintWriter output;
    BufferedReader input;
    KeyPair keys = generarClaves();
    Socket socket;

    public MainActivity() throws NoSuchAlgorithmException {
    }

    private KeyPair generarClaves() throws NoSuchAlgorithmException {
        KeyPairGenerator kgen = KeyPairGenerator.getInstance("RSA");
        kgen.initialize(2048);
        return kgen.generateKeyPair();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Capturamos el boton de Enviar
        View button = findViewById(R.id.button_send);

        // Llama al listener del boton Enviar
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

    }


    // Creación de un cuadro de dialogo para confirmar pedido
    private void showDialog() {
        final CheckBox sabanas = (CheckBox) findViewById(R.id.checkBox_sabanas);
        final CheckBox colchon = (CheckBox) findViewById(R.id.checkBox_colchon);
        final CheckBox lampara = (CheckBox) findViewById(R.id.checkBox_lampara);
        final CheckBox almohada = (CheckBox) findViewById(R.id.checkBox_almohada);
        final CheckBox cabecero = (CheckBox) findViewById(R.id.checkBox_cabecero);

        final int id = Integer.parseInt(String.valueOf(((EditText) findViewById(R.id.textinput_numcli)).getText()));

        final int sab = Integer.parseInt(String.valueOf(((EditText) findViewById(R.id.numSab)).getText()));
        final int col = Integer.parseInt(String.valueOf(((EditText) findViewById(R.id.numCol)).getText()));
        final int alm = Integer.parseInt(String.valueOf(((EditText) findViewById(R.id.numAlm)).getText()));
        final int lamp = Integer.parseInt(String.valueOf(((EditText) findViewById(R.id.numLam)).getText()));
        final int cab = Integer.parseInt(String.valueOf(((EditText) findViewById(R.id.numCab)).getText()));


        if (!sabanas.isChecked() && !colchon.isChecked() && !lampara.isChecked() &&
                !almohada.isChecked() && !cabecero.isChecked()) {
            // Mostramos un mensaje emergente;
            Toast.makeText(getApplicationContext(), "Selecciona al menos un elemento", Toast.LENGTH_SHORT).show();

        } else if (0 > sab || sab > 300
                || 0 > col || col > 300
                || 0 > alm || alm > 300
                || 0 > lamp || lamp > 300
                || 0 > cab || cab > 300) {
            Toast.makeText(getApplicationContext(), "Los valores tienen que estar entre 0 y 300", Toast.LENGTH_LONG).show();

        } else if (sabanas.isChecked() && sab == 0 ||
                colchon.isChecked() && col == 0 ||
                almohada.isChecked() && alm == 0 ||
                lampara.isChecked() && lamp == 0 ||
                cabecero.isChecked() && cab == 0) {

            Toast.makeText(getApplicationContext(), "Debes comprar al menos 1 unidad de los productos marcados", Toast.LENGTH_LONG).show();

        } else if (0 >= id) {

            Toast.makeText(getApplicationContext(), "El número de cliente tiene que ser mayor a 0", Toast.LENGTH_LONG).show();

        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Enviar")
                    .setMessage("Se va a proceder al envio")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                // Catch ok button and send information
                                public void onClick(DialogInterface dialog, int whichButton) {


                                    Thread hilo = new Thread(new Runnable() {
                                        @RequiresApi(api = Build.VERSION_CODES.O)
                                        @Override
                                        public void run() {
                                            try {
                                                socket = new Socket(server, 6060);
                                                output = new PrintWriter(new OutputStreamWriter(
                                                        socket.getOutputStream()));
                                                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                                                // 1. Extraer los datos de la vista
                                                String mensaje = "" + id;
                                                if (sabanas.isChecked()) mensaje += ";Sabana=" + sab;
                                                if (colchon.isChecked()) mensaje += ";Colchon=" + col;
                                                if (almohada.isChecked()) mensaje += ";Almohada=" + alm;
                                                if (lampara.isChecked()) mensaje += ";Lampara=" + lamp;
                                                if (cabecero.isChecked()) mensaje += ";Cabecero=" + cab;


                                                // 2. Firmar los datos

                                                Signature sg = Signature.getInstance("SHA256withRSA");
                                                sg.initSign(keys.getPrivate());
                                                sg.update(mensaje.getBytes());
                                                byte[] firma = sg.sign();


                                                // 3. Enviar los datos
                                                String publicKey = Base64.getEncoder().encodeToString(keys.getPublic().getEncoded());
                                                String ff = Base64.getEncoder().encodeToString(firma);
                                                output.println(mensaje);
                                                output.println(publicKey);
                                                output.println(ff);
                                                output.flush();

                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(MainActivity.this, "Petición enviada correctamente", Toast.LENGTH_SHORT).show();
                                                    }
                                                });


                                                String response = input.readLine();

                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                                                    }
                                                });


                                                output.close();
                                                input.close();
                                                socket.close();

                                            } catch (IOException | InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    hilo.start();

                                }
                            }

                    ).setNegativeButton(android.R.string.no, null).show();
        }
    }


}
