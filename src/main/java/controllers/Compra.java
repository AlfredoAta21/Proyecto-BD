package controllers;

import java.time.LocalDateTime;
import java.util.List;

public class Compra {
    private String cliente;
    private LocalDateTime fecha;
    private double total;
    private List<DetalleCompra> detalles;

    public Compra(String cliente, LocalDateTime fecha, List<DetalleCompra> detalles) {
        this.cliente = cliente;
        this.fecha = fecha;
        this.detalles = detalles;
        this.total = calcularTotal();
    }

    private double calcularTotal() {
        return detalles.stream().mapToDouble(DetalleCompra::getSubtotal).sum();
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<DetalleCompra> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleCompra> detalles) {
        this.detalles = detalles;
    }
}
