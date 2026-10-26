package modelo;

import java.time.LocalDate;

/** Un miembro del grupo familiar de un funcionario (tabla grupo_familiar). */
public class GrupoFamiliar {

    private final int id;
    private final String nombres;
    private final String apellidos;
    private final LocalDate fechaNacimiento; // puede ser null
    private final String rol;

    public GrupoFamiliar(int id, String nombres, String apellidos, LocalDate fechaNacimiento, String rol) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.rol = rol;
    }

    public int getId() {
        return id;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getRol() {
        return rol;
    }
}
