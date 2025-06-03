package BasesDeDatos;
import controllers.Producto;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
        String sql = "SELECT * FROM TablaClientes c";

        try (Connection conn = obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("Clientes registrados:");
            boolean hayResultados = false;
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

    public static List<Producto> obtenerProductos() {
        List<Producto> productos = new ArrayList<>();

        String query = "SELECT p.ROWID AS id, p.nombre, p.precio, p.cantidad, p.descripcion, " +
                "c.nombre AS categoria, pr.nombre AS proveedor, p.disponible " +
                "FROM TablaProductos p " +
                "JOIN TablaCategorias c ON p.categoria_ref = REF(c) " +
                "JOIN TablaProveedores pr ON p.proveedor_ref = REF(pr)";

        try (Connection conn = obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet resultSet = stmt.executeQuery(query)) {

            while (resultSet.next()) {
                Producto producto = new Producto(
                        resultSet.getString("id"),
                        resultSet.getString("nombre"),
                        resultSet.getDouble("precio"),
                        resultSet.getInt("cantidad"),
                        resultSet.getString("descripcion"),
                        resultSet.getString("categoria"),
                        resultSet.getString("proveedor")
                );
                productos.add(producto);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
        }

        return productos;
    }

    public static void actualizarStock(String productoId, int nuevoStock) {
        String sql = "UPDATE TablaProductos SET cantidad = ? WHERE ROWID = ?";

        try (Connection conn = obtenerConexion();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nuevoStock);
            pstmt.setString(2, productoId);

            int filasActualizadas = pstmt.executeUpdate();
            if (filasActualizadas > 0) {
                System.out.println("Stock actualizado para el producto con ID " + productoId);
            } else {
                System.out.println("No se encontró el producto con ID " + productoId);
            }

        } catch (SQLException e) {
            System.err.println("Error al actualizar stock: " + e.getMessage());
        }
    }

    public Producto buscarProductoPorNombre(String nombreProducto) {
        String query = "SELECT p.ROWID AS id, p.nombre, p.precio, p.cantidad, p.descripcion, " +
                "c.nombre AS categoria, pr.nombre AS proveedor, p.disponible " +
                "FROM TablaProductos p " +
                "JOIN TablaCategorias c ON p.categoria_ref = REF(c) " +
                "JOIN TablaProveedores pr ON p.proveedor_ref = REF(pr) " +
                "WHERE LOWER(p.nombre) = LOWER(?)";

        try (Connection conn = obtenerConexion();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nombreProducto);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    return new Producto(
                            resultSet.getString("id"),
                            resultSet.getString("nombre"),
                            resultSet.getDouble("precio"),
                            resultSet.getInt("cantidad"),
                            resultSet.getString("descripcion"),
                            resultSet.getString("categoria"),
                            resultSet.getString("proveedor")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar producto por nombre: " + e.getMessage());
        }

        return null; // Si no se encuentra el producto
    }
}