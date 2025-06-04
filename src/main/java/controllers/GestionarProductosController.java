package controllers;

import BasesDeDatos.MongoConection;
import com.mongodb.client.MongoCollection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;
import org.bson.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.nio.file.Path;


public class GestionarProductosController {

    @FXML
    private TextField id, nombreProducto, precioProducto, cantidadProducto, descripcionProducto;

    @FXML
    private ComboBox<String> categorias, proveedores;

    @FXML
    private Button buttonAgregarProducto, buttonEliminarProducto, buttonModificarProducto;

    @FXML
    private TableView<Producto> tableProductos;

    @FXML
    private TableColumn<Producto, String> colidProducto, colNombreProducto, colDescripcionProducto, colCategoriaProducto, colProveedorProducto;

    @FXML
    private TableColumn<Producto, Double> colTelefonoProducto;

    @FXML
    private TableColumn<Producto, Integer> colCorreoProducto;

    @FXML
    private ImageView SubirImagen;

    private String rutaImagenActual;

    private OracleConnection connection;

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    private final MongoConection mongoConection = new MongoConection();

    public void initialize() {
        colidProducto.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreProducto.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefonoProducto.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colCorreoProducto.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colDescripcionProducto.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colCategoriaProducto.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colProveedorProducto.setCellValueFactory(new PropertyValueFactory<>("proveedor"));

        tableProductos.setItems(productos);
        tableProductos.setRowFactory(tv -> new TableRow<Producto>() {
            @Override
            protected void updateItem(Producto producto, boolean empty) {
                super.updateItem(producto, empty);

                if (producto == null || empty) {
                    setStyle("");
                } else {
                    int stock = producto.getCantidad();

                    if (stock == 0) {
                        // Rojo con texto blanco
                        setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white;");
                        setTextFill(Color.WHITE);
                    } else if (stock > 0 && stock <= 5) {
                        // Amarillo con texto negro
                        setStyle("-fx-background-color: #fff176; -fx-text-fill: black;");
                        setTextFill(Color.BLACK);
                    } else {
                        // Default (blanco)
                        setStyle("");
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });
        connectToDatabase();
        loadCategorias();
        loadProveedores();
        loadProductos();
    }

    private void connectToDatabase() {
        try {
            OracleDataSource dataSource = new OracleDataSource();
            dataSource.setURL("jdbc:oracle:thin:@localhost:1521/FREEPDB1");
            dataSource.setUser("administrador");
            dataSource.setPassword("ContraseñaSegura2025");
            connection = (OracleConnection) dataSource.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCategorias() {
        try (PreparedStatement statement = connection.prepareStatement("SELECT nombre FROM TablaCategorias");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categorias.getItems().add(resultSet.getString("nombre"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProveedores() {
        try (PreparedStatement statement = connection.prepareStatement("SELECT nombre FROM TablaProveedores");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                proveedores.getItems().add(resultSet.getString("nombre"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProductos() {
        productos.clear();
        String query = """
                SELECT p.ROWID AS id, p.nombre, p.precio, p.cantidad, p.descripcion, 
                       c.nombre AS categoria, pr.nombre AS proveedor 
                FROM TablaProductos p 
                JOIN TablaCategorias c ON p.categoria_ref = REF(c) 
                JOIN TablaProveedores pr ON p.proveedor_ref = REF(pr)""";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
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
        String nombre = nombreProducto.getText().trim();
        String precioTexto = precioProducto.getText().trim();
        String cantidadTexto = cantidadProducto.getText().trim();
        String descripcion = descripcionProducto.getText().trim();
        String categoria = categorias.getValue();
        String proveedor = proveedores.getValue();

        if (nombre.isEmpty() || precioTexto.isEmpty() || cantidadTexto.isEmpty() ||
                descripcion.isEmpty() || categoria == null || proveedor == null) {
            mostrarMensaje("Por favor, completa todos los campos.");
            return;
        }

        double precio;
        int cantidad;

        try {
            precio = Double.parseDouble(precioTexto);
            cantidad = Integer.parseInt(cantidadTexto);

            if (precio < 0 || cantidad < 0) {
                mostrarMensaje("El precio y la cantidad deben ser valores mayores o iguales a 0.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarMensaje("El precio o la cantidad tienen un formato inválido.");
            return;
        }

        try {
            Object refCategoria = obtenerReferencia("TablaCategorias", categoria);
            Object refProveedor = obtenerReferencia("TablaProveedores", proveedor);

            String insertQuery = """
                INSERT INTO TablaProductos (nombre, precio, cantidad, descripcion, categoria_ref, proveedor_ref, disponible)
                VALUES (?, ?, ?, ?, ?, ?, ?)""";
            try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                statement.setString(1, nombre);
                statement.setDouble(2, precio);
                statement.setInt(3, cantidad);
                statement.setString(4, descripcion);
                statement.setObject(5, refCategoria);
                statement.setObject(6, refProveedor);
                statement.setInt(7, 1);
                statement.executeUpdate();
            }

            loadProductos();
            limpiarCampos();
            mostrarImagenPorDefecto();

            String queryRowId = "SELECT ROWID FROM TablaProductos WHERE nombre = ? AND descripcion = ?";
            try (PreparedStatement stmtRowId = connection.prepareStatement(queryRowId)) {
                stmtRowId.setString(1, nombre);
                stmtRowId.setString(2, descripcion);
                ResultSet rsRowId = stmtRowId.executeQuery();
                if (rsRowId.next()) {
                    String rowId = rsRowId.getString("ROWID");
                    MongoConection.insertarProducto(nombre, rowId, rutaImagenActual);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensaje("Error al agregar producto.");
        }
    }


    private Object obtenerReferencia(String tabla, String nombre) throws Exception {
        String query = "SELECT REF(t) FROM " + tabla + " t WHERE t.nombre = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nombre);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getObject(1);
        }
    }

    @FXML
    private void seleccionarProducto() {
        Producto producto = tableProductos.getSelectionModel().getSelectedItem();
        if (producto != null) {
            id.setText(producto.getId());
            nombreProducto.setText(producto.getNombre());
            precioProducto.setText(String.valueOf(producto.getPrecio()));
            cantidadProducto.setText(String.valueOf(producto.getCantidad()));
            descripcionProducto.setText(producto.getDescripcion());
            categorias.setValue(producto.getCategoria());
            proveedores.setValue(producto.getProveedor());

            // 🔍 Cargar imagen desde MongoDB
            try {
                String ruta = MongoConection.recuperarImagen(producto.getId());
                if (ruta != null && !ruta.equals("Sin imagen")) {
                    File archivo = new File(ruta);
                    if (!archivo.exists()) {
                        Path proyectoPath = Paths.get(System.getProperty("user.dir"));
                        archivo = proyectoPath.resolve(ruta).normalize().toFile();
                    }
                    if (archivo.exists()) {
                        Image imagen = new Image(new FileInputStream(archivo));
                        SubirImagen.setImage(imagen);
                    } else {
                        mostrarImagenPorDefecto();
                    }
                } else {
                    mostrarImagenPorDefecto();
                }
            } catch (Exception e) {
                e.printStackTrace();
                mostrarImagenPorDefecto();
            }
        }
    }

    @FXML
    private void modificarProducto(ActionEvent event) {
        Producto producto = tableProductos.getSelectionModel().getSelectedItem();
        if (producto == null) {
            mostrarMensaje("Por favor, seleccione un producto para modificar.");
            return;
        }

        String nombre = nombreProducto.getText().trim();
        String precioTexto = precioProducto.getText().trim();
        String cantidadTexto = cantidadProducto.getText().trim();
        String descripcion = descripcionProducto.getText().trim();
        String categoria = categorias.getValue();
        String proveedor = proveedores.getValue();

        if (nombre.isEmpty() || precioTexto.isEmpty() || cantidadTexto.isEmpty() ||
                descripcion.isEmpty() || categoria == null || proveedor == null) {
            mostrarMensaje("Por favor, completa todos los campos.");
            return;
        }

        double precio;
        int cantidad;

        try {
            precio = Double.parseDouble(precioTexto);
            cantidad = Integer.parseInt(cantidadTexto);

            if (precio < 0 || cantidad < 0) {
                mostrarMensaje("El precio y la cantidad deben ser mayores o iguales a 0.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarMensaje("Formato inválido para precio o cantidad.");
            return;
        }

        try {
            Object refCategoria = obtenerReferencia("TablaCategorias", categoria);
            Object refProveedor = obtenerReferencia("TablaProveedores", proveedor);

            String query = """
                UPDATE TablaProductos 
                SET nombre = ?, precio = ?, cantidad = ?, descripcion = ?, categoria_ref = ?, proveedor_ref = ? 
                WHERE ROWID = ?""";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, nombre);
                statement.setDouble(2, precio);
                statement.setInt(3, cantidad);
                statement.setString(4, descripcion);
                statement.setObject(5, refCategoria);
                statement.setObject(6, refProveedor);
                statement.setString(7, producto.getId());
                statement.executeUpdate();
            }

            loadProductos();
            limpiarCampos();
            mostrarImagenPorDefecto();
            mostrarMensaje("Producto modificado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensaje("Error al modificar el producto.");
        }
    }


    @FXML
    private void eliminarProducto(ActionEvent event) {
        Producto producto = tableProductos.getSelectionModel().getSelectedItem();
        if (producto == null) {
            mostrarMensaje("Por favor, seleccione un producto para eliminar.");
            return;
        }

        String rowId = producto.getId();

        try {
            // Primero eliminar imagen y documento de MongoDB
            eliminarProductoDeMongoYImagen(rowId);

            // Luego eliminar producto en Oracle
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM TablaProductos WHERE ROWID = ?")) {
                statement.setString(1, rowId);
                statement.executeUpdate();
            }

            loadProductos();
            limpiarCampos();
            mostrarImagenPorDefecto();
            mostrarMensaje("Producto eliminado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensaje("Error al eliminar el producto.");
        }
    }

    private void eliminarProductoDeMongoYImagen(String rowId) {
        try {
            // Obtener ruta de imagen en MongoDB
            String rutaImagen = MongoConection.recuperarImagen(rowId);

            if (rutaImagen != null && !rutaImagen.equals("Sin imagen")) {
                Path rutaArchivo;
                File posibleArchivo = new File(rutaImagen);

                if (posibleArchivo.isAbsolute()) {
                    rutaArchivo = Paths.get(rutaImagen);
                } else {
                    Path proyectoPath = Paths.get(System.getProperty("user.dir"));
                    rutaArchivo = proyectoPath.resolve(rutaImagen).normalize();
                }

                // 🧼 Liberar la imagen del ImageView para que no esté en uso
                SubirImagen.setImage(null);
                System.gc(); // Llama al recolector de basura para liberar FileInputStream

                // Verificar y eliminar archivo si existe
                if (Files.exists(rutaArchivo) && Files.isRegularFile(rutaArchivo)) {
                    try {
                        Files.delete(rutaArchivo);
                        System.out.println("Imagen eliminada: " + rutaArchivo.toString());
                    } catch (IOException ioEx) {
                        System.err.println("No se pudo eliminar la imagen: " + ioEx.getMessage());
                    }
                } else {
                    System.out.println("No se encontró la imagen para eliminar: " + rutaArchivo.toString());
                }
            }

            // Eliminar documento del producto en MongoDB
            if (MongoConection.database == null) {
                MongoConection.connect();
            }
            MongoCollection<Document> coleccionProductos = MongoConection.database.getCollection("productos");
            Document filtro = new Document("oracle_rowid", rowId);
            coleccionProductos.deleteOne(filtro);

            Image imagenPorDefecto = new Image(getClass().getResource("/imagenes/10608872.png").toExternalForm());
            SubirImagen.setImage(imagenPorDefecto);
            System.out.println("Producto eliminado de MongoDB con ROWID: " + rowId);

        } catch (Exception e) {
            System.err.println("Error al eliminar producto o imagen en MongoDB: " + e.getMessage());
        }
    }

    private void limpiarCampos() {
        id.clear();
        nombreProducto.clear();
        precioProducto.clear();
        cantidadProducto.clear();
        descripcionProducto.clear();
        categorias.getSelectionModel().clearSelection();
        proveedores.getSelectionModel().clearSelection();
    }

    private void mostrarMensaje(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void subirImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos de imagen", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File archivoSeleccionado = fileChooser.showOpenDialog(SubirImagen.getScene().getWindow());
        if (archivoSeleccionado != null) {
            try {
                String nombre = nombreProducto.getText().trim();
                if (nombre.isEmpty()) {
                    mostrarMensaje("Primero debes ingresar el nombre del producto.");
                    return;
                }

                File carpetaDestino = new File("src/main/resources/imagenes");
                if (!carpetaDestino.exists()) carpetaDestino.mkdirs();

                String extension = archivoSeleccionado.getName().substring(archivoSeleccionado.getName().lastIndexOf("."));
                String nombreArchivo = nombre.replaceAll("[^a-zA-Z0-9]", "_") + extension;
                File destino = new File(carpetaDestino, nombreArchivo);
                rutaImagenActual = destino.getPath();

                Files.copy(archivoSeleccionado.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);

                Image imagen = new Image(new FileInputStream(destino));
                SubirImagen.setImage(imagen);

                // Si estamos en modo edición (hay un ID cargado), actualizar ruta en Mongo
                String rowIdActual = id.getText();
                if (rowIdActual != null && !rowIdActual.isEmpty()) {
                    MongoConection.actualizarRutaImagen(rowIdActual, rutaImagenActual);
                }

                mostrarMensaje("Imagen subida con éxito: " + nombreArchivo);
            } catch (Exception e) {
                e.printStackTrace();
                mostrarMensaje("Error al subir la imagen.");
            } finally {
                mostrarImagenPorDefecto();
            }
        }
    }

    private void mostrarImagenPorDefecto() {
        Image imagenPorDefecto = new Image(getClass().getResource("/imagenes/10608872.png").toExternalForm());
        SubirImagen.setImage(imagenPorDefecto);
    }

}
