package BasesDeDatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class OracleConection {
    private static final String URL = "jdbc:oracle:thin:@localhost:1521/FREEPDB1";
    private static final String USUARIO = "administrador";
    private static final String CONTRASEÑA = "ContraseñaSegura2025";

    // Método para obtener la conexión
    public static Connection obtenerConexion() throws SQLException {
        try {
            // Cargar el driver de Oracle
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se pudo cargar el driver JDBC de Oracle", e);
        }

        // Retornar la conexión
        return DriverManager.getConnection(URL, USUARIO, CONTRASEÑA);
    }

    public static void consultarClientes() {
    String sql = "SELECT c.nombre, c.email FROM TablaClientes c";

    try (Connection conn = obtenerConexion();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        System.out.println("Clientes registrados:");
        boolean hayResultados = false; // Bandera para verificar si hay resultados
        while (rs.next()) {
            hayResultados = true;
            String nombre = rs.getString("nombre");
            String email = rs.getString("email");
            System.out.println("Nombre: " + nombre + ", Email: " + email);
        }

        if (!hayResultados) {
            System.out.println("No se encontraron clientes en la base de datos.");
        }

    } catch (SQLException e) {
        System.err.println("Error al consultar clientes: " + e.getMessage());
    }
}
}
