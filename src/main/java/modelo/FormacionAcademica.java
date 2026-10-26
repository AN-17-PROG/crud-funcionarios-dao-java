package modelo;

/** Un estudio realizado por un funcionario (tabla formacion_academica). */
public class FormacionAcademica {

    private final int id;
    private final String universidad;
    private final String nivelEstudio;
    private final String titulo;

    public FormacionAcademica(int id, String universidad, String nivelEstudio, String titulo) {
        this.id = id;
        this.universidad = universidad;
        this.nivelEstudio = nivelEstudio;
        this.titulo = titulo;
    }

    public int getId() {
        return id;
    }

    public String getUniversidad() {
        return universidad;
    }

    public String getNivelEstudio() {
        return nivelEstudio;
    }

    public String getTitulo() {
        return titulo;
    }
}
