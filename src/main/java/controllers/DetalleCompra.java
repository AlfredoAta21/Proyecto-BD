package controllers;

public class DetalleCompra {
    private Producto productoCompra;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    public DetalleCompra(Producto productoCompra, int cantidad) {
        this.productoCompra = productoCompra;
        this.cantidad = cantidad;
        this.precioUnitario = productoCompra.getPrecio();
        this.subtotal = cantidad * precioUnitario;
    }

    public Producto getProducto() {
        return productoCompra;
    }

    public void setProducto(Producto producto) {
        this.productoCompra = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}