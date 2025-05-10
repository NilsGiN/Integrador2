package com.example.prueba1.model;

public class Mantenimiento {
    String id_tipo, fecha, fecha_prox, km_actual, km_prox, costo, notas;
    public Mantenimiento(){}

    public Mantenimiento(String id_tipo, String fecha, String fecha_prox, String km_actual, String km_prox, String costo, String notas) {
        this.id_tipo = id_tipo;
        this.fecha = fecha;
        this.fecha_prox = fecha_prox;
        this.km_actual = km_actual;
        this.km_prox = km_prox;
        this.costo = costo;
        this.notas = notas;
    }

    public String getId_tipo() {
        return id_tipo;
    }

    public void setId_tipo(String id_tipo) {
        this.id_tipo = id_tipo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getFecha_prox() {
        return fecha_prox;
    }

    public void setFecha_prox(String fecha_prox) {
        this.fecha_prox = fecha_prox;
    }

    public String getKm_actual() {
        return km_actual;
    }

    public void setKm_actual(String km_actual) {
        this.km_actual = km_actual;
    }

    public String getKm_prox() {
        return km_prox;
    }

    public void setKm_prox(String km_prox) {
        this.km_prox = km_prox;
    }

    public String getCosto() {
        return costo;
    }

    public void setCosto(String costo) {
        this.costo = costo;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
