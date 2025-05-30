package controllers;

public class Proveedor {
    private String id;
    private String nombre;
    private String contacto;
    private String direccion;
    private String telefono;

    public Proveedor(String id, String nombre, String contacto, String direccion, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.contacto = contacto;
        this.direccion = direccion;
        this.telefono = telefono;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getContacto() {
        return contacto;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }
}