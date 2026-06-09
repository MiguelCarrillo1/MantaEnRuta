package com.example.mantaenruta.data;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "usuarios",
        indices = {
                @Index(value = {"correo"}, unique = true),
                @Index(value = {"usuario"}, unique = true)
        }
)
public class Usuario {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String nombres;
    public String correo;
    public String usuario;
    public String contrasena;
    public String telefono;
    public String fechaRegistro;
    public double latitud;
    public double longitud;

    public Usuario(
            int id,
            String nombres,
            String correo,
            String usuario,
            String contrasena,
            String telefono,
            String fechaRegistro,
            double latitud,
            double longitud
    ) {
        this.id = id;
        this.nombres = nombres;
        this.correo = correo;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.telefono = telefono;
        this.fechaRegistro = fechaRegistro;
        this.latitud = latitud;
        this.longitud = longitud;
    }
}
