package controllers;

import BasesDeDatos.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import org.bson.Document;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
            System.out.println("El carrito está vacío. No se puede finalizar la compra.");
            return;
        }

        List<DetalleCompra> detalles = new ArrayList<>();
        double totalCalculado = 0.0;

        for (Node node : vboxVenta.getChildren()) {
            if (node instanceof HBox hbox) {
                String nombre = (String) hbox.getUserData();
                Producto producto = oracleConection.buscarProductoPorNombre(nombre);
                int cantidad = Integer.parseInt(((Label) hbox.lookup(".cart-qty")).getText());

                if (producto == null) {
                    System.err.println("Producto no encontrado en la base de datos: " + nombre);
                    continue;
                }

                int stockActual = producto.getCantidad();
                int nuevoStock = stockActual - cantidad;

                if (nuevoStock < 0) {
                    System.out.println("No hay suficiente stock para el producto: " + producto.getNombre());
                    continue;
                }
                OracleConection.actualizarStock(producto.getId(), nuevoStock);
                detalles.add(new DetalleCompra(producto, cantidad));
                totalCalculado += producto.getPrecio() * cantidad;
            }
        }

        if (detalles.isEmpty()) {
            System.out.println("No se registró la compra. No hay productos válidos.");
            return;
        }

        // Convertir detalles a List<Document> para MongoDB
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

        String cliente = "";
        mongoConection.insertarCompra(cliente, totalCalculado, productosParaMongo);

        // Limpiar la vista
        vboxVenta.getChildren().clear();
        total = 0;
        totalLabel.setText("$0.00");

        System.out.println("Compra finalizada correctamente.");
    }


}
