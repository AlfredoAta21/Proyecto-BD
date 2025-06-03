package controllers;

import BasesDeDatos.MongoConection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;

public class CartaProductoController {

    @FXML private Label cartaNombre;
    @FXML private Label cartaPrecio;
    @FXML private ImageView imagen;

    public void setProducto(Producto producto) {
        cartaNombre.setText(producto.getNombre());
        cartaPrecio.setText("$" + producto.getPrecio());


        String rutaImagen = MongoConection.recuperarImagen(producto.getId());

        if (rutaImagen != null && !rutaImagen.equals("Sin imagen")) {
            try {
                Image imagenProducto = new Image("file:" + rutaImagen);
                imagen.setImage(imagenProducto);
            } catch (Exception e) {
                System.err.println("Error al cargar la imagen: " + e.getMessage());
            }
        } else {
            System.out.println("No hay imagen para este producto.");
            // Opcional: cargar una imagen por defecto
            imagen.setImage(new Image("file:resources/images/no-image.png"));
        }
    }

            }
