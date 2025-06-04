package controllers;

import BasesDeDatos.MongoConection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistorialVentasController {

    @FXML
    private TableView<Document> tablaVentas;
    @FXML
    private TableColumn<Document, String> colFecha;
    @FXML
    private TableColumn<Document, String> colCliente;
    @FXML
    private TableColumn<Document, String> colTotal;
    @FXML
    private ListView<String> listaDetalles;
    @FXML
    private Label labelTotalVenta;
    @FXML
    private TextField buscarField;

    private MongoDatabase database;

    @FXML
    public void initialize() {
        database = MongoConection.connect();

        colFecha.setCellValueFactory(data -> {
            Date fecha = data.getValue().getDate("fecha");
            return new javafx.beans.property.SimpleStringProperty(
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(fecha));
        });

        colCliente.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getString("cliente")));

        colTotal.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("$%.2f", data.getValue().getDouble("total"))));

        tablaVentas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                mostrarDetallesVenta(newSel);
            }
        });

        cargarVentas("");

//        buscarField.setOnKeyReleased(this::buscarVentas);
    }

    private void cargarVentas(String filtro) {
        tablaVentas.getItems().clear();

        MongoCollection<Document> coleccion = database.getCollection("compras");
        MongoCursor<Document> cursor = coleccion.find().iterator();

        while (cursor.hasNext()) {
            Document venta = cursor.next();
            String cliente = venta.getString("cliente");
            Date fecha = venta.getDate("fecha");

            if (filtro.isEmpty() ||
                    cliente.toLowerCase().contains(filtro.toLowerCase()) ||
                    new SimpleDateFormat("dd/MM/yyyy").format(fecha).contains(filtro)) {
                tablaVentas.getItems().add(venta);
            }
        }
    }

    private void mostrarDetallesVenta(Document venta) {
        listaDetalles.getItems().clear();

        List<Document> productos = (List<Document>) venta.get("productos");
        if (productos != null) {
            for (Document producto : productos) {
                String nombre = producto.getString("nombre");
                int cantidad = producto.getInteger("cantidad", 1);
                double subtotal = producto.getDouble("subtotal");

                listaDetalles.getItems().add(
                        String.format("%dx %s - $%.2f", cantidad, nombre, subtotal));
            }
        }

        labelTotalVenta.setText("Total: $" + String.format("%.2f", venta.getDouble("total")));
    }

}
