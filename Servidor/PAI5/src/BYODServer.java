import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;


public class BYODServer {

    /**
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, SQLException {
        //Comprobación de la instalación del driver
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            System.out.println("Error, no se ha podido cargar MySQL JDBC Driver" + ex);
        }

        // Conexión base datos
        metodos.connectDatabase();

        ServerSocket serverSocket = new ServerSocket(6060);

        while (true) {

            // wait for client connection and check login information
            try {
                System.err.println("Waiting for connection...");
                Socket socket = serverSocket.accept();
                System.out.println("Connection accepted");
                //ServidorHilo hilo = new ServidorHilo(socket);
                new Thread(new ServidorHilo(socket)).start();

            } // end try

            // handle exception communicating with client
            catch (IOException ioException) {
                ioException.printStackTrace();
            }

        } // end while

    }
}
