package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GestionarProductosController {

    @FXML
    private TextField id, nombreProducto, precioProducto, cantidadProducto, descripcionProducto;

    @FXML
    private ComboBox<String> categorias, proveedores;

    @FXML
    private Button buttonAgregarProducto;

    @FXML
    private Button buttonEliminarProducto;

    @FXML
    private TableView<Producto> tableProductos;

    @FXML
    private TableColumn<Producto, String> colidProducto, colNombreProducto, colDescripcionProducto, colCategoriaProducto, colProveedorProducto;

    @FXML
    private TableColumn<Producto, Double> colTelefonoProducto;

    @FXML
    private TableColumn<Producto, Integer> colCorreoProducto;

    @FXML
    private Button buttonModificarProducto;

    private Connection connection;

    private ObservableList<Producto> productos;

    public void initialize() {
        // Configurar columnas de la tabla
        colidProducto.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreProducto.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefonoProducto.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colCorreoProducto.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colDescripcionProducto.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colCategoriaProducto.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colProveedorProducto.setCellValueFactory(new PropertyValueFactory<>("proveedor"));

        productos = FXCollections.observableArrayList();
        tableProductos.setItems(productos);

        // Conectar a la base de datos
        connectToDatabase();

        // Cargar categorías y proveedores
        loadCategorias();
        loadProveedores();

        // Cargar productos en la tabla
        loadProductos();
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/FREEPDB1", "administrador", "ContraseñaSegura2025");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCategorias() {
        try {
            String query = "SELECT nombre FROM TablaCategorias";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                categorias.getItems().add(resultSet.getString("nombre"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProveedores() {
        try {
            String query = "SELECT nombre FROM TablaProveedores";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                proveedores.getItems().add(resultSet.getString("nombre"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProductos() {
        productos.clear();
        try {
            String query = "SELECT p.ROWID AS id, p.nombre, p.precio, p.cantidad, p.descripcion, c.nombre AS categoria, pr.nombre AS proveedor " +
                           "FROM TablaProductos p " +
                           "JOIN TablaCategorias c ON p.categoria_ref = REF(c) " +
                           "JOIN TablaProveedores pr ON p.proveedor_ref = REF(pr)";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                productos.add(new Producto(
                        resultSet.getString("id"),
                        resultSet.getString("nombre"),
                        resultSet.getDouble("precio"),
                        resultSet.getInt("cantidad"),
                        resultSet.getString("descripcion"),
                        resultSet.getString("categoria"),
                        resultSet.getString("proveedor")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void agregarProducto(ActionEvent event) {
        String nombre = nombreProducto.getText();
        double precio = Double.parseDouble(precioProducto.getText());
        int cantidad = Integer.parseInt(cantidadProducto.getText());
        String descripcion = descripcionProducto.getText();
        String categoria = categorias.getValue();
        String proveedor = proveedores.getValue();

        try {
            // Obtener referencias de categoría y proveedor
            String queryCategoria = "SELECT REF(c) FROM TablaCategorias c WHERE c.nombre = ?";
            PreparedStatement stmtCategoria = connection.prepareStatement(queryCategoria);
            stmtCategoria.setString(1, categoria);
            ResultSet rsCategoria = stmtCategoria.executeQuery();
            rsCategoria.next();
            Object refCategoria = rsCategoria.getObject(1);

            String queryProveedor = "SELECT REF(p) FROM TablaProveedores p WHERE p.nombre = ?";
            PreparedStatement stmtProveedor = connection.prepareStatement(queryProveedor);
            stmtProveedor.setString(1, proveedor);
            ResultSet rsProveedor = stmtProveedor.executeQuery();
            rsProveedor.next();
            Object refProveedor = rsProveedor.getObject(1);

            // Insertar producto
            String query = "INSERT INTO TablaProductos (nombre, precio, cantidad, descripcion, categoria_ref, proveedor_ref) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, nombre);
            statement.setDouble(2, precio);
            statement.setInt(3, cantidad);
            statement.setString(4, descripcion);
            statement.setObject(5, refCategoria);
            statement.setObject(6, refProveedor);
            statement.executeUpdate();

            // Recargar productos
            loadProductos();

            // Limpiar campos
            id.clear();
            nombreProducto.clear();
            precioProducto.clear();
            cantidadProducto.clear();
            descripcionProducto.clear();
            categorias.getSelectionModel().clearSelection();
            proveedores.getSelectionModel().clearSelection();

            // Mostrar mensaje de éxito
            mostrarMensaje("Producto agregado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void seleccionarProducto() {
        Producto productoSeleccionado = tableProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            id.setText(productoSeleccionado.getId());
            nombreProducto.setText(productoSeleccionado.getNombre());
            precioProducto.setText(String.valueOf(productoSeleccionado.getPrecio()));
            cantidadProducto.setText(String.valueOf(productoSeleccionado.getCantidad()));
            descripcionProducto.setText(productoSeleccionado.getDescripcion());
            categorias.setValue(productoSeleccionado.getCategoria());
            proveedores.setValue(productoSeleccionado.getProveedor());
        }
    }

    @FXML
    private void modificarProducto(ActionEvent event) {
        Producto productoSeleccionado = tableProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado == null) {
            mostrarMensaje("Por favor, seleccione un producto para modificar.");
            return;
        }

        String nuevoNombre = nombreProducto.getText();
        double nuevoPrecio = Double.parseDouble(precioProducto.getText());
        int nuevaCantidad = Integer.parseInt(cantidadProducto.getText());
        String nuevaDescripcion = descripcionProducto.getText();
        String nuevaCategoria = categorias.getValue();
        String nuevoProveedor = proveedores.getValue();

        try {
            // Obtener referencias de categoría y proveedor
            String queryCategoria = "SELECT REF(c) FROM TablaCategorias c WHERE c.nombre = ?";
            PreparedStatement stmtCategoria = connection.prepareStatement(queryCategoria);
            stmtCategoria.setString(1, nuevaCategoria);
            ResultSet rsCategoria = stmtCategoria.executeQuery();
            rsCategoria.next();
            Object refCategoria = rsCategoria.getObject(1);

            String queryProveedor = "SELECT REF(p) FROM TablaProveedores p WHERE p.nombre = ?";
            PreparedStatement stmtProveedor = connection.prepareStatement(queryProveedor);
            stmtProveedor.setString(1, nuevoProveedor);
            ResultSet rsProveedor = stmtProveedor.executeQuery();
            rsProveedor.next();
            Object refProveedor = rsProveedor.getObject(1);

            // Actualizar producto
            String query = "UPDATE TablaProductos SET nombre = ?, precio = ?, cantidad = ?, descripcion = ?, categoria_ref = ?, proveedor_ref = ? WHERE ROWID = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, nuevoNombre);
            statement.setDouble(2, nuevoPrecio);
            statement.setInt(3, nuevaCantidad);
            statement.setString(4, nuevaDescripcion);
            statement.setObject(5, refCategoria);
            statement.setObject(6, refProveedor);
            statement.setString(7, productoSeleccionado.getId());
            statement.executeUpdate();

            // Recargar productos
            loadProductos();

            // Limpiar campos
            id.clear();
            nombreProducto.clear();
            precioProducto.clear();
            cantidadProducto.clear();
            descripcionProducto.clear();
            categorias.getSelectionModel().clearSelection();
            proveedores.getSelectionModel().clearSelection();

            // Mostrar mensaje de éxito
            mostrarMensaje("Producto modificado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensaje("Error al modificar el producto.");
        }
    }

    @FXML
    private void eliminarProducto(ActionEvent event) {
        Producto productoSeleccionado = tableProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado == null) {
            mostrarMensaje("Por favor, seleccione un producto para eliminar.");
            return;
        }

        try {
            String query = "DELETE FROM TablaProductos WHERE ROWID = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, productoSeleccionado.getId());
            statement.executeUpdate();

            // Recargar productos
            loadProductos();

            // Limpiar campos
            id.clear();
            nombreProducto.clear();
            precioProducto.clear();
            cantidadProducto.clear();
            descripcionProducto.clear();
            categorias.getSelectionModel().clearSelection();
            proveedores.getSelectionModel().clearSelection();

            // Mostrar mensaje de éxito
            mostrarMensaje("Producto eliminado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensaje("Error al eliminar el producto.");
        }
    }

    private void mostrarMensaje(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Operación exitosa");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}