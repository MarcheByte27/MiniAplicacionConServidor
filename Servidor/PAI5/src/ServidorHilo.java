import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ServidorHilo implements Runnable {
    Socket socket;
    BufferedReader input;
    PrintWriter output;

    public ServidorHilo(Socket socket) throws IOException {
        this.socket = socket;
        this.input = input = new BufferedReader(new
                InputStreamReader(socket.getInputStream()));
        this.output = new PrintWriter(new
                OutputStreamWriter(socket.getOutputStream()));
    }

    public void run() {
        try {

            String mensaje = input.readLine();
            String clavePub = input.readLine();
            String firma = input.readLine();


            byte[] decodePub = Base64.getDecoder().decode(clavePub);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodePub);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(keySpec);

            String datosSeparado[] = mensaje.split(";");
            String idCliente = datosSeparado[0];
            String pedido = datosSeparado[1];

            if (datosSeparado.length > 2)
                for (int i = 2; i < datosSeparado.length; i++)
                    pedido = pedido + "," + datosSeparado[i];

            Signature verifica = Signature.getInstance("SHA256withRSA");
            verifica.initVerify(pubKey);
            verifica.update(mensaje.getBytes());
            byte[] decodeFirma = Base64.getDecoder().decode(firma);

            boolean check = verifica.verify(decodeFirma);
            if (check) {
                output.println("Firma correcta");
                metodos.insertarPedido(pedido, clavePub, idCliente, 0);
                output.flush();
            } else {
                output.println("Firma incorrecta");
                metodos.insertarPedido(pedido, clavePub, idCliente, 1);
                output.flush();
            }

            output.close();
            input.close();
            socket.close();

        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException | InvalidKeyException | SignatureException | SQLException el) {
            el.printStackTrace();
        }
    }

}
