import java.io.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class metodos {
    public static Connection connection = null;
    public static void connectDatabase() {
        try {
            // We register the MySQL (MariaDB) driver
            // Registramos el driver de MySQL (MariaDB)
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                System.out.println("Error al registrar el driver de MySQL: " + ex);
            }

            // Database connect
            // Conectamos con la base de datos
            connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/servidor",
                    "root", "root");
            boolean valid = connection.isValid(10000);

            System.out.println(valid ? "TEST OK" : "TEST FAIL");

        } catch (SQLException ex) {
            System.out.println(ex);
        }

    }

    public static void insertarPedido(String mensaje , String clavePublica , String idCliente, int fallo) throws SQLException {

        PreparedStatement s = connection.prepareStatement("Insert into pedidos (mensaje, idCliente,clavePublica,verificacion) " +
                "values(?,?,?,?)");
        s.setString(1, mensaje);
        s.setInt(   2, Integer.parseInt(idCliente));
        s.setString(3, clavePublica);
        s.setInt(       4, fallo);

        s.executeUpdate();

    }

    //ejecutar 1 vez al dia para que no se repitan los dias
    public static void ratio() throws SQLException, IOException {

        LocalDateTime f = LocalDateTime.now();
        List<Double> fechas = new ArrayList<>();


        for (int i = 0; i < 3; i++) {
            DecimalFormat formatter = new DecimalFormat("00");

            LocalDateTime f2 = f.minusDays(1);
            PreparedStatement s = connection.prepareStatement("SELECT verificacion FROM pedidos WHERE fechaHora between '" + f2.getYear() + "-" + formatter.format(f2.getMonthValue()) + "-" + formatter.format(f2.getDayOfMonth()) + " 00:00:00'" +
                    " and '" + f.getYear() +"-"+formatter.format(f.getMonthValue())+"-"+formatter.format(f.getDayOfMonth())+" 00:00:00'");
            f = f2;
            ResultSet resultado = s.executeQuery();
            double suma = 0.;
            int tam = 0;
            while(resultado.next()){
                suma+= resultado.getInt(1);
                tam++;
            }
            suma = (tam - suma) / tam;
            fechas.add(suma);
        }

        f =  LocalDateTime.now().minusDays(1);

        if(fechas.get(0) > fechas.get(1) && fechas.get(0) > fechas.get(2))
            escribirLogs("MES: " + f.getMonth() + ", AÑO: " + f.getYear() + ", DIA: "+ f.getDayOfMonth() +", RATIO: "+ fechas.get(0) + ", TENDENCIA: +");
        else if(fechas.get(0) < fechas.get(1) && fechas.get(0) < fechas.get(2))
            escribirLogs("MES: " + f.getMonth() + ", AÑO: " + f.getYear() + ", DIA: "+ f.getDayOfMonth() +", RATIO: "+ fechas.get(0) + ", TENDENCIA: -");
        else if(fechas.get(0) == fechas.get(1) && fechas.get(0) == fechas.get(2))
            escribirLogs("MES: " + f.getMonth() + ", AÑO: " + f.getYear() + ", DIA: "+ f.getDayOfMonth() +", RATIO: "+ fechas.get(0) + ", TENDENCIA: 0");
        else
            escribirLogs("MES: " + f.getMonth() + ", AÑO: " + f.getYear() + ", DIA: "+ f.getDayOfMonth() +", RATIO: "+ fechas.get(0) + ", TENDENCIA: indeterminada");

    }

    public static void escribirLogs (String data){
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            File file = new File("logs.txt");
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(data + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //Cierra instancias de FileWriter y BufferedWriter
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }



}
