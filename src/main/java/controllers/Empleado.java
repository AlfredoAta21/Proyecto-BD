package controllers;

public class Empleado {
    private String nombre;
    private String direccion;
    private String telefono;
    private String puesto;
    private double salario;

    public Empleado(String nombre, String direccion, String telefono, String puesto, double salario) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.puesto = puesto;
        this.salario = salario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getPuesto() {
        return puesto;
    }

    public double getSalario() {
        return salario;
    }
}