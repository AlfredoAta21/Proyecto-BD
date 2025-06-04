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

public class GestionarClientesController {

    @FXML
    private TextField nombre;

    @FXML
    private TextField direccion;

    @FXML
    private TextField telefono;

    @FXML
    private TextField email;

    @FXML
    private TextField tipodeCliente;

    @FXML
    private Button buttonAgregarCliente;

    @FXML
    private TableView<Cliente> tableEmpleados;

    @FXML
    private TableColumn<Cliente, String> nombreCliente;

    @FXML
    private TableColumn<Cliente, String> direccionCliente;

    @FXML
    private TableColumn<Cliente, String> telefonoCliente;

    @FXML
    private TableColumn<Cliente, String> emailCliente;

    @FXML
    private TableColumn<Cliente, String> tipoCliente;

    private ObservableList<Cliente> clientes;

    private Connection connection;

    public void initialize() {
        // Configure table columns
        nombreCliente.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        direccionCliente.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        telefonoCliente.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        emailCliente.setCellValueFactory(new PropertyValueFactory<>("email"));
        tipoCliente.setCellValueFactory(new PropertyValueFactory<>("tipoCliente"));

        clientes = FXCollections.observableArrayList();
        tableEmpleados.setItems(clientes);

        // Connect to the database
        connectToDatabase();

        // Load initial data
        loadClientes();

        tableEmpleados.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                nombre.setText(newValue.getNombre());
                direccion.setText(newValue.getDireccion());
                telefono.setText(newValue.getTelefono());
                email.setText(newValue.getEmail());
                tipodeCliente.setText(newValue.getTipoCliente());
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

    private void loadClientes() {
        clientes.clear();
        try {
            String query = "SELECT nombre, direccion, telefono, email, tipo_cliente FROM TablaClientes";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                clientes.add(new Cliente(
                        resultSet.getString("nombre"),
                        resultSet.getString("direccion"),
                        resultSet.getString("telefono"),
                        resultSet.getString("email"),
                        resultSet.getString("tipo_cliente")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void agregarCliente(ActionEvent event) {
        String nombreText = nombre.getText();
        String direccionText = direccion.getText();
        String telefonoText = telefono.getText();
        String emailText = email.getText();
        String tipoClienteText = tipodeCliente.getText();

        try {
            String query = "INSERT INTO TablaClientes (nombre, direccion, telefono, email, tipo_cliente) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, nombreText);
            statement.setString(2, direccionText);
            statement.setString(3, telefonoText);
            statement.setString(4, emailText);
            statement.setString(5, tipoClienteText);
            statement.executeUpdate();

            // Refresh table data
            loadClientes();

            // Clear fields
            nombre.clear();
            direccion.clear();
            telefono.clear();
            email.clear();
            tipodeCliente.clear();

            // Show success message
            mostrarMensaje("Cliente agregado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensaje("Error al agregar el cliente.");
        }
    }

    @FXML
    private void modificarCliente(ActionEvent event) {
        Cliente selectedCliente = tableEmpleados.getSelectionModel().getSelectedItem();
        if (selectedCliente == null) {
            mostrarMensaje("Por favor, seleccione un cliente para modificar.");
            return;
        }

        String nombreText = nombre.getText();
        String direccionText = direccion.getText();
        String telefonoText = telefono.getText();
        String emailText = email.getText();
        String tipoClienteText = tipodeCliente.getText();

        try {
            String query = "UPDATE TablaClientes SET nombre = ?, direccion = ?, telefono = ?, email = ?, tipo_cliente = ? WHERE nombre = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, nombreText);
            statement.setString(2, direccionText);
            statement.setString(3, telefonoText);
            statement.setString(4, emailText);
            statement.setString(5, tipoClienteText);
            statement.setString(6, selectedCliente.getNombre());
            statement.executeUpdate();

            // Refresh table data
            loadClientes();

            // Clear fields
            nombre.clear();
            direccion.clear();
            telefono.clear();
            email.clear();
            tipodeCliente.clear();

            // Show success message
            mostrarMensaje("Cliente modificado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensaje("Error al modificar el cliente.");
        }
    }

    @FXML
    private void eliminarCliente(ActionEvent event) {
        Cliente selectedCliente = tableEmpleados.getSelectionModel().getSelectedItem();
        if (selectedCliente == null) {
            mostrarMensaje("Por favor, seleccione un cliente para eliminar.");
            return;
        }

        try {
            String query = "DELETE FROM TablaClientes WHERE nombre = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, selectedCliente.getNombre());
            statement.executeUpdate();

            // Refresh table data
            loadClientes();

            // Show success message
            mostrarMensaje("Cliente eliminado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensaje("Error al eliminar el cliente.");
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