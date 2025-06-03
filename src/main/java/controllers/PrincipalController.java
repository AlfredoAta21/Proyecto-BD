package controllers;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;


public class PrincipalController {

    private String rol;

    @FXML
    private Text dia;

    @FXML
    private Text hora;

    @FXML
    private Button cerrarSesion;

    @FXML
    private Button agregarEmpleado;

    @FXML
    private Pane contenedor;
    @FXML
    private void initialize() {
        System.out.println("Controller initialized!");
        obtenerDia();
    }

    public void obtenerDia(){
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dia.setText(localDate.format(formatter));
        actualizarHora();

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> actualizarHora())
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void actualizarHora() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        hora.setText(dateFormat.format(date));
    }

    @FXML
    void handleAgregarProducto(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestionarProductos.fxml"));
            Parent root = loader.load();
            contenedor.getChildren().clear();
            contenedor.getChildren().add(root);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    void handleAgregarCategoria(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestionarCategorias.fxml"));
            Parent root = loader.load();
            contenedor.getChildren().clear();
            contenedor.getChildren().add(root);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    void handleAgregarProvedor(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestionarProveedores.fxml"));
            Parent root = loader.load();
            contenedor.getChildren().clear();
            contenedor.getChildren().add(root);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    void handleAgregarEmpleado(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestionarEmpleados.fxml"));
            Parent root = loader.load();
            contenedor.getChildren().clear();
            contenedor.getChildren().add(root);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    void handlerVentas(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ventaVista.fxml"));
            Parent root = loader.load();
            contenedor.getChildren().clear();
            contenedor.getChildren().add(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }













}