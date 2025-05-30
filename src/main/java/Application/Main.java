package Application;
import BasesDeDatos.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/principal.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Principal");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        OracleConection oracleConection = new OracleConection();
        try {
            Connection connection = oracleConection.obtenerConexion();
            System.out.println("Conexión exitosa a la base de datos.");
        } catch (SQLException e) {
            // Manejar excepciones de conexión
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
            return; // Salir si no se puede conectar
        }
        oracleConection.consultarClientes();
        launch(args);
    }
}