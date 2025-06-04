package BasesDeDatos;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.mongodb.client.MongoCollection;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MongoConection {

    public static MongoDatabase database;

    public static MongoDatabase connect() {
        String user = "administrador";
        String password = "ContraseñaSegura2025";
        String dbName = "POS";

        MongoCredential credential = MongoCredential.createCredential(user, dbName, password.toCharArray());

        MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(Collections.singletonList(new ServerAddress("localhost", 27017))))
                        .credential(credential)
                        .build());

        System.out.println("Conexión exitosa a MongoDB");
        database = mongoClient.getDatabase(dbName);
        return database;
    }

    public static void insertarCompra(String cliente, double total, List<Document> productos) {
        try {
            if (database == null) {
                connect();
            }

            MongoCollection<Document> coleccion = database.getCollection("compras");

            Document compra = new Document()
                    .append("cliente", cliente)
                    .append("fecha", new Date())
                    .append("total", total)
                    .append("productos", productos);

            coleccion.insertOne(compra);

            System.out.println("Compra registrada en MongoDB.");
        } catch (Exception e) {
            System.err.println("Error al insertar compra en MongoDB: " + e.getMessage());
        }
    }

    public static void insertarProducto(String nombre, String rowId, String rutaImagen) {
        try {
            if (database == null) {
                connect();
            }

            MongoCollection<Document> coleccionProductos = database.getCollection("productos");

            Document doc = new Document("nombre", nombre)
                    .append("oracle_rowid", rowId)
                    .append("ruta_imagen", rutaImagen != null ? rutaImagen : "Sin imagen");

            coleccionProductos.insertOne(doc);

            System.out.println("Producto insertado en MongoDB.");
        } catch (Exception e) {
            System.err.println("Error al insertar producto en MongoDB: " + e.getMessage());
        }
    }

    public static String recuperarImagen(String rowId) {
        try {
            if (database == null) {
                connect();
            }

            MongoCollection<Document> coleccionProductos = database.getCollection("productos");

            Document filtro = new Document("oracle_rowid", rowId);
            Document resultado = coleccionProductos.find(filtro).first();

            if (resultado != null) {
                return resultado.getString("ruta_imagen");
            } else {
                System.out.println("No se encontró un producto con el Row ID proporcionado.");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error al recuperar la imagen del producto: " + e.getMessage());
            return null;
        }
    }

    public static void actualizarRutaImagen(String rowId, String nuevaRutaImagen) {
        try {
            if (database == null) {
                connect();
            }

            MongoCollection<Document> coleccionProductos = database.getCollection("productos");

            Document filtro = new Document("oracle_rowid", rowId);
            Document actualizacion = new Document("$set", new Document("ruta_imagen", nuevaRutaImagen));

            coleccionProductos.updateOne(filtro, actualizacion);

            System.out.println("Ruta de imagen actualizada en MongoDB.");
        } catch (Exception e) {
            System.err.println("Error al actualizar la ruta de la imagen: " + e.getMessage());
        }
    }


}
