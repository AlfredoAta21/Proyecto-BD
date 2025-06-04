package controllers;

import BasesDeDatos.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import org.bson.Document;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static BasesDeDatos.OracleConection.obtenerConexion;

public class VentaVistaController {
    @FXML private FlowPane flowPaneProductos;
    @FXML private VBox vboxVenta;
    @FXML private Label totalLabel;
    @FXML private TextField busquedaField;

    private double total = 0.0;
    private OracleConection oracleConection = new OracleConection();
    private List<Producto> todosLosProductos = OracleConection.obtenerProductos();
    private MongoConection mongoConection = new MongoConection();



    public void initialize() {
        todosLosProductos = OracleConection.obtenerProductos()
                .stream()
                .filter(p -> p.getDisponible() == 1)
                .collect(Collectors.toList());
        mostrarProductos(todosLosProductos);

        busquedaField.textProperty().addListener((obs, oldVal, newVal) -> buscarProducto(newVal));
    }
    private void buscarProducto(String busqueda) {
        String filtro = busqueda.trim().toLowerCase();
        List<Producto> filtrados = todosLosProductos.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(filtro) ||
                        p.getCategoria().toLowerCase().contains(filtro) ||
                        p.getDescripcion().toLowerCase().contains(filtro))
                .toList();

        if (filtrados.isEmpty()) {
            flowPaneProductos.getChildren().clear();        }
                        Label noResultados = new Label("No se encontraron productos");
                        noResultados.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
                        flowPaneProductos.getChildren().add(noResultados);

        mostrarProductos(filtrados);
    }

    private void agregarProductoACesta(Producto producto) {
        for (Node node : vboxVenta.getChildren()) {
            if (node instanceof HBox hbox && producto.getNombre().equals(hbox.getUserData())) {
                Label cantidadLabel = (Label) hbox.lookup(".cart-qty");
                Label precioLabel = (Label) hbox.lookup(".cart-price");

                int cantidad = Integer.parseInt(cantidadLabel.getText()) + 1;
                cantidadLabel.setText(String.valueOf(cantidad));

                double precioTotalProducto = cantidad * producto.getPrecio();
                precioLabel.setText("$" + String.format("%.2f", precioTotalProducto));

                total += producto.getPrecio();
                totalLabel.setText("$" + String.format("%.2f", total));
                return;
            }
        }

        HBox hbox = new HBox();
        hbox.setSpacing(10);
        hbox.setUserData(producto.getNombre());
        hbox.getStyleClass().add("cart-row");

        Button btnRestar = new Button("➖");
        btnRestar.getStyleClass().add("button");

        Label cantidadLabel = new Label("1");
        cantidadLabel.getStyleClass().add("cart-qty");

        Label nombreLabel = new Label(producto.getNombre());
        nombreLabel.getStyleClass().add("cart-name");

        Region spacer = new Region();
        spacer.getStyleClass().add("spacer");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label precioLabel = new Label("$" + String.format("%.2f", producto.getPrecio()));
        precioLabel.getStyleClass().add("cart-price");

        btnRestar.setOnAction(e -> {
            int cantidad = Integer.parseInt(cantidadLabel.getText()) - 1;
            total -= producto.getPrecio();

            if (cantidad <= 0) {
                vboxVenta.getChildren().remove(hbox);
            } else {
                cantidadLabel.setText(String.valueOf(cantidad));
                double precioTotalProducto = cantidad * producto.getPrecio();
                precioLabel.setText("$" + String.format("%.2f", precioTotalProducto));
            }

            totalLabel.setText("$" + String.format("%.2f", total));
        });

        hbox.getChildren().addAll(btnRestar, cantidadLabel, nombreLabel, spacer, precioLabel);
        vboxVenta.getChildren().add(hbox);

        total += producto.getPrecio();
        totalLabel.setText("$" + String.format("%.2f", total));
    }

    private void mostrarProductos(List<Producto> productos) {
        flowPaneProductos.getChildren().clear();
        for (Producto producto : productos) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/CartasProductos.fxml"));
                Node card = loader.load();
                FlowPane.setMargin(card, new Insets(10));
                CartaProductoController controller = loader.getController();
                controller.setProducto(producto);

                card.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 1) {
                        agregarProductoACesta(producto);
                    }
                });
                flowPaneProductos.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void finalizarCompra() {
        if (vboxVenta.getChildren().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Carrito vacío", "No hay productos en el carrito para finalizar la compra.");
            return;
        }

        // Obtener lista de clientes desde Oracle
        List<String> clientes = OracleConection.obtenerNombresClientes();

        // Crear ComboBox y CheckBox
        ComboBox<String> comboClientes = new ComboBox<>();
        comboClientes.getItems().addAll(clientes);
        comboClientes.setPromptText("Seleccione un cliente");

        CheckBox clienteGeneralCheck = new CheckBox("Venta a Cliente General");
        clienteGeneralCheck.setSelected(true);
        comboClientes.disableProperty().bind(clienteGeneralCheck.selectedProperty());

        VBox content = new VBox(10, new Label("Seleccione el cliente para la venta:"), comboClientes, clienteGeneralCheck);
        content.setPadding(new Insets(10));

        Alert clienteAlert = new Alert(Alert.AlertType.CONFIRMATION);
        clienteAlert.setTitle("Asignar cliente");
        clienteAlert.setHeaderText("¿A nombre de quién se realiza la venta?");
        clienteAlert.getDialogPane().setContent(content);

        if (clienteAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        String nombreCliente;
        if (clienteGeneralCheck.isSelected()) {
            nombreCliente = "Cliente General";
        } else {
            nombreCliente = comboClientes.getValue();
            if (nombreCliente == null || nombreCliente.trim().isEmpty()) {
                mostrarAlerta(Alert.AlertType.ERROR, "Cliente no seleccionado", "Debe seleccionar un cliente o marcar la opción 'Cliente General'.");
                return;
            }
        }

        // Confirmar compra
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar compra");
        confirmacion.setHeaderText("¿Estás seguro de que deseas finalizar la compra?");
        confirmacion.setContentText("Total a pagar: $" + String.format("%.2f", total));

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        // Procesar productos del carrito
        List<DetalleCompra> detalles = new ArrayList<>();
        double totalCalculado = 0.0;

        for (Node node : vboxVenta.getChildren()) {
            if (node instanceof HBox hbox) {
                String nombre = (String) hbox.getUserData();
                Producto producto = oracleConection.buscarProductoPorNombre(nombre);

                if (producto == null) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Producto no encontrado", "El producto '" + nombre + "' no se encontró en la base de datos.");
                    continue;
                }

                int cantidad = Integer.parseInt(((Label) hbox.lookup(".cart-qty")).getText());
                int stockActual = producto.getCantidad();

                if (stockActual < cantidad) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Stock insuficiente", "No hay suficiente stock para el producto: " + producto.getNombre());
                    continue;
                }

                OracleConection.actualizarStock(producto.getId(), stockActual - cantidad);

                detalles.add(new DetalleCompra(producto, cantidad));
                totalCalculado += producto.getPrecio() * cantidad;
            }
        }

        if (detalles.isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Compra no registrada", "No se pudo registrar la compra. Verifica el stock o la base de datos.");
            return;
        }

        // Construir documentos para MongoDB
        List<Document> productosParaMongo = new ArrayList<>();
        for (DetalleCompra detalle : detalles) {
            Document docProducto = new Document()
                    .append("id", detalle.getProducto().getId())
                    .append("nombre", detalle.getProducto().getNombre())
                    .append("cantidad", detalle.getCantidad())
                    .append("precioUnitario", detalle.getProducto().getPrecio())
                    .append("subtotal", detalle.getSubtotal());
            productosParaMongo.add(docProducto);
        }

        mongoConection.insertarCompra(nombreCliente, totalCalculado, productosParaMongo);

        vboxVenta.getChildren().clear();
        total = 0;
        totalLabel.setText("$0.00");

        mostrarAlerta(Alert.AlertType.INFORMATION, "Compra registrada", "La compra fue registrada exitosamente.");
    }



    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }


}
