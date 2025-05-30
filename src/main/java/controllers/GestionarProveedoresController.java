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

public class GestionarProveedoresController {

    @FXML
    private TextField id;

    @FXML
    private TextField nombre;

    @FXML
    private TextField telefono;

    @FXML
    private TextField contacto;

    @FXML
    private TextField direccion;

    @FXML
    private Button buttonAgregarProveedor;

    @FXML
    private TableView<Proveedor> tableProveedores;

    @FXML
    private TableColumn<Proveedor, String> idProveedor;

    @FXML
    private TableColumn<Proveedor, String> nombreProveedor;

    @FXML
    private TableColumn<Proveedor, String> telefonoProveedor;

    @FXML
    private TableColumn<Proveedor, String> contactoProveedor;

    @FXML
    private TableColumn<Proveedor, String> direccionProveedor;

    private ObservableList<Proveedor> proveedores;

    private Connection connection;

    public void initialize() {
        // Configurar columnas de la tabla con las propiedades correctas
        idProveedor.setCellValueFactory(new PropertyValueFactory<>("id"));
        nombreProveedor.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        contactoProveedor.setCellValueFactory(new PropertyValueFactory<>("contacto"));
        direccionProveedor.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        telefonoProveedor.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        proveedores = FXCollections.observableArrayList();
        tableProveedores.setItems(proveedores);

        // Conectar a la base de datos
        connectToDatabase();

        // Cargar datos iniciales
        loadProveedores();

        // Detectar selección en la tabla
        tableProveedores.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                id.setText(newValue.getId());
                nombre.setText(newValue.getNombre());
                contacto.setText(newValue.getContacto());
                direccion.setText(newValue.getDireccion());
                telefono.setText(newValue.getTelefono());
            }
        });
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/FREEPDB1", "administrador", "ContraseñaSegura2025");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProveedores() {
        proveedores.clear();
        try {
            String query = "SELECT ROWID AS id, nombre, contacto, direccion, telefono FROM TablaProveedores ORDER BY nombre";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                proveedores.add(new Proveedor(
                        resultSet.getString("id"),
                        resultSet.getString("nombre"),
                        resultSet.getString("contacto"),
                        resultSet.getString("direccion"),
                        resultSet.getString("telefono")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void agregarProveedor(ActionEvent event) {
        String nombreText = nombre.getText();
        String contactoText = contacto.getText();
        String direccionText = direccion.getText();
        String telefonoText = telefono.getText();

        try {
            String query = "INSERT INTO TablaProveedores (nombre, contacto, direccion, telefono) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, nombreText);
            statement.setString(2, contactoText);
            statement.setString(3, direccionText);
            statement.setString(4, telefonoText);
            statement.executeUpdate();

            // Recargar datos en la tabla
            loadProveedores();

            // Limpiar campos
            id.clear();
            nombre.clear();
            telefono.clear();
            contacto.clear();
            direccion.clear();

            // Mostrar mensaje de éxito
            mostrarMensaje("Proveedor agregado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void modificarProveedor(ActionEvent event) {
        Proveedor selectedProveedor = tableProveedores.getSelectionModel().getSelectedItem();
        if (selectedProveedor == null) {
            mostrarMensaje("Por favor, selecciona un proveedor para modificar.");
            return;
        }

        String idText = id.getText();
        String nombreText = nombre.getText();
        String contactoText = contacto.getText();
        String direccionText = direccion.getText();
        String telefonoText = telefono.getText();

        try {
            String query = "UPDATE TablaProveedores SET nombre = ?, contacto = ?, direccion = ?, telefono = ? WHERE ROWID = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, nombreText);
            statement.setString(2, contactoText);
            statement.setString(3, direccionText);
            statement.setString(4, telefonoText);
            statement.setString(5, idText);
            statement.executeUpdate();

            // Recargar datos en la tabla
            loadProveedores();

            // Limpiar campos
            id.clear();
            nombre.clear();
            contacto.clear();
            direccion.clear();
            telefono.clear();

            // Mostrar mensaje de éxito
            mostrarMensaje("Proveedor modificado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void eliminarProveedor(ActionEvent event) {
        Proveedor selectedProveedor = tableProveedores.getSelectionModel().getSelectedItem();
        if (selectedProveedor == null) {
            mostrarMensaje("Por favor, selecciona un proveedor para eliminar.");
            return;
        }

        String idText = selectedProveedor.getId();

        try {
            String query = "DELETE FROM TablaProveedores WHERE ROWID = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, idText);
            statement.executeUpdate();

            // Recargar datos en la tabla
            loadProveedores();

            // Limpiar campos
            id.clear();
            nombre.clear();
            contacto.clear();
            direccion.clear();
            telefono.clear();

            // Mostrar mensaje de éxito
            mostrarMensaje("Proveedor eliminado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
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