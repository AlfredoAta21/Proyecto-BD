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

public class GestionarEmpleadosController {

    @FXML
    private TextField nombre;

    @FXML
    private TextField direccion;

    @FXML
    private TextField telefono;

    @FXML
    private TextField puesto;

    @FXML
    private TextField salario;

    @FXML
    private Button buttonAgregarEmpleado;

    @FXML
    private Button buttonElimarEmpleado;

    @FXML
    private TableView<Empleado> tableEmpleados;

    @FXML
    private TableColumn<Empleado, String> nombreEmpleado;

    @FXML
    private TableColumn<Empleado, String> direccionEmpleado;

    @FXML
    private TableColumn<Empleado, String> telefonoEmpleado;

    @FXML
    private TableColumn<Empleado, String> puestoEmpleado;

    @FXML
    private TableColumn<Empleado, Double> salarioEmpleado;

    private ObservableList<Empleado> empleados;

    private Connection connection;

    public void initialize() {
        // Configurar columnas de la tabla
        nombreEmpleado.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        direccionEmpleado.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        telefonoEmpleado.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        puestoEmpleado.setCellValueFactory(new PropertyValueFactory<>("puesto"));
        salarioEmpleado.setCellValueFactory(new PropertyValueFactory<>("salario"));

        empleados = FXCollections.observableArrayList();
        tableEmpleados.setItems(empleados);

        // Conectar a la base de datos
        connectToDatabase();

        // Cargar datos iniciales
        loadEmpleados();

        tableEmpleados.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                nombre.setText(newValue.getNombre());
                direccion.setText(newValue.getDireccion());
                telefono.setText(newValue.getTelefono());
                puesto.setText(newValue.getPuesto());
                salario.setText(String.valueOf(newValue.getSalario()));
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

    private void loadEmpleados() {
        empleados.clear();
        try {
            String query = "SELECT nombre, direccion, telefono, puesto, salario FROM TablaEmpleados";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                System.out.println("Empleado encontrado:" + resultSet.getString("nombre"));
                empleados.add(new Empleado(
                        resultSet.getString("nombre"),
                        resultSet.getString("direccion"),
                        resultSet.getString("telefono"),
                        resultSet.getString("puesto"),
                        resultSet.getDouble("salario")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void agregarEmpleado(ActionEvent event) {
        String nombreText = nombre.getText();
        String direccionText = direccion.getText();
        String telefonoText = telefono.getText();
        String puestoText = puesto.getText();
        double salarioValue = Double.parseDouble(salario.getText());

        try {
            String query = "INSERT INTO TablaEmpleados (nombre, direccion, telefono, puesto, salario) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, nombreText);
            statement.setString(2, direccionText);
            statement.setString(3, telefonoText);
            statement.setString(4, puestoText);
            statement.setDouble(5, salarioValue);
            statement.executeUpdate();

            // Recargar datos en la tabla
            loadEmpleados();

            // Limpiar campos
            nombre.clear();
            direccion.clear();
            telefono.clear();
            puesto.clear();
            salario.clear();

            // Mostrar mensaje de éxito
            mostrarMensaje("Empleado agregado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void modificarEmpleado(ActionEvent event) {
        Empleado selectedEmpleado = tableEmpleados.getSelectionModel().getSelectedItem();
        if (selectedEmpleado == null) {
            mostrarMensaje("Por favor, seleccione un empleado para modificar.");
            return;
        }

        String nombreText = nombre.getText();
        String direccionText = direccion.getText();
        String telefonoText = telefono.getText();
        String puestoText = puesto.getText();
        double salarioValue;

        try {
            salarioValue = Double.parseDouble(salario.getText());
        } catch (NumberFormatException e) {
            mostrarMensaje("El salario debe ser un número válido.");
            return;
        }

        try {
            String query = "UPDATE TablaEmpleados SET nombre = ?, direccion = ?, telefono = ?, puesto = ?, salario = ? WHERE nombre = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, nombreText);
            statement.setString(2, direccionText);
            statement.setString(3, telefonoText);
            statement.setString(4, puestoText);
            statement.setDouble(5, salarioValue);
            statement.setString(6, selectedEmpleado.getNombre());
            statement.executeUpdate();

            // Reload data in the table
            loadEmpleados();

            // Clear fields
            nombre.clear();
            direccion.clear();
            telefono.clear();
            puesto.clear();
            salario.clear();

            // Show success message
            mostrarMensaje("Empleado modificado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensaje("Error al modificar el empleado.");
        }
    }


    @FXML
    private void eliminarEmpleado(ActionEvent event) {
        Empleado selectedEmpleado = tableEmpleados.getSelectionModel().getSelectedItem();
        if (selectedEmpleado == null) {
            mostrarMensaje("Por favor, seleccione un empleado para eliminar.");
            return;
        }

        try {
            String query = "DELETE FROM TablaEmpleados WHERE nombre = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, selectedEmpleado.getNombre());
            statement.executeUpdate();

            // Reload data in the table
            loadEmpleados();

            // Clear fields
            nombre.clear();
            direccion.clear();
            telefono.clear();
            puesto.clear();
            salario.clear();

            // Show success message
            mostrarMensaje("Empleado eliminado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensaje("Error al eliminar el empleado.");
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