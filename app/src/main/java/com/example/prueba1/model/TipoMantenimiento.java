package com.example.prueba1.model;

public class TipoMantenimiento {
    String id_tipo;
    String nombre;
    public TipoMantenimiento(){}

    public TipoMantenimiento(String id_tipo, String nombre) {
        this.id_tipo = id_tipo;
        this.nombre = nombre;
    }

    public String getId_tipo() {
        return id_tipo;
    }

    public void setId_tipo(String id_tipo) {
        this.id_tipo = id_tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
