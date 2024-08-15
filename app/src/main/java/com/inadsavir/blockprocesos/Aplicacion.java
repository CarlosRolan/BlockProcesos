package com.inadsavir.blockprocesos;

public class Aplicacion {

    public String nombre;
    public int icono;

    public Aplicacion(String nombre, int icono) {
        this.nombre = nombre;
        this.icono = icono;
    }

    public Aplicacion(){

    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getIcono() {
        return icono;
    }

    public void setIcono(int icono) {
        this.icono = icono;
    }
}

