package controllers;

public class Producto {
    private String id;
    private String nombre;
    private double precio;
    private int cantidad;
    private String descripcion;
    private String categoria;
    private String proveedor;

    public Producto(String id, String nombre, double precio, int cantidad, String descripcion, String categoria, String proveedor) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.proveedor = proveedor;
    }

    // Getters y setters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public String getProveedor() { return proveedor; }
}