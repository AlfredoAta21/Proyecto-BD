package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GestionarCategoriaController {

    @FXML
    private TextField id;

    @FXML
    private TextField nombre;

    @FXML
    private TextField descripcion;

    @FXML
    private Button buttonAgregarCategoria;

    @FXML
    private TableView<Categoria> tablaCategoria;

    @FXML
    private TableColumn<Categoria, String> idCategoria;

    @FXML
    private TableColumn<Categoria, String> nombreCategoria;

    @FXML
    private TableColumn<Categoria, String> descripcionCategoria;

    private ObservableList<Categoria> categorias;

    private Connection connection;

    public void initialize() {
        // Configurar columnas de la tabla
        idCategoria.setCellValueFactory(new PropertyValueFactory<>("id"));
        nombreCategoria.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        descripcionCategoria.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        categorias = FXCollections.observableArrayList();
        tablaCategoria.setItems(categorias);

        // Conectar a la base de datos
        connectToDatabase();

        // Cargar datos iniciales
        loadCategorias();
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/FREEPDB1", "administrador", "ContraseñaSegura2025");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCategorias() {
        categorias.clear();
        try {
            String query = "SELECT ROWID AS id, nombre, descripcion FROM TablaCategorias";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                categorias.add(new Categoria(
                        resultSet.getString("id"),
                        resultSet.getString("nombre"),
                        resultSet.getString("descripcion")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void agregarCategoria(ActionEvent event) {
        String nombreText = nombre.getText();
        String descripcionText = descripcion.getText();

        try {
            String query = "INSERT INTO TablaCategorias (nombre, descripcion) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, nombreText);
            statement.setString(2, descripcionText);
            statement.executeUpdate();

            // Recargar datos en la tabla
            loadCategorias();

            // Limpiar campos
            nombre.clear();
            descripcion.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}