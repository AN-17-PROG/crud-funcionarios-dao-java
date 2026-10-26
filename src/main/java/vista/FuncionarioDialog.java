package vista;

import dao.FuncionarioDAO;
import excepciones.DaoException;
import excepciones.ValidacionException;
import modelo.Catalogo;
import modelo.FormacionAcademica;
import modelo.Funcionario;
import modelo.GrupoFamiliar;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Formulario para crear o editar un funcionario (si "existente" es null, es una creación).
 * Incluye dos pestañas para las relaciones: grupo familiar y formación académica.
 * Si el DAO lanza una DaoException, el formulario sigue abierto para no perder lo escrito.
 */
public class FuncionarioDialog extends JDialog {

    private final FuncionarioDAO dao;
    private final Funcionario existente;
    private boolean guardado = false;

    private final JComboBox<Catalogo> cmbTipoDocumento;
    private final JComboBox<Catalogo> cmbEstadoCivil;
    private final JComboBox<String> cmbSexo = new JComboBox<>(new String[]{"Masculino", "Femenino", "Otro"});
    private final JTextField txtNumero = new JTextField(20);
    private final JTextField txtNombres = new JTextField(20);
    private final JTextField txtApellidos = new JTextField(20);
    private final JTextField txtDireccion = new JTextField(20);
    private final JTextField txtTelefono = new JTextField(20);
    private final JTextField txtFechaNacimiento = new JTextField(10);

    private final DefaultTableModel modeloFamilia = new DefaultTableModel(
            new String[]{"Nombres", "Apellidos", "Fecha nac. (AAAA-MM-DD)", "Rol (parentesco)"}, 0);
    private final JTable tablaFamilia = new JTable(modeloFamilia);

    private final DefaultTableModel modeloFormacion = new DefaultTableModel(
            new String[]{"Universidad", "Nivel de estudio", "Título"}, 0);
    private final JTable tablaFormacion = new JTable(modeloFormacion);

    public FuncionarioDialog(JFrame propietario, FuncionarioDAO dao, List<Catalogo> tiposDocumento,
                             List<Catalogo> estadosCiviles, Funcionario existente) {
        super(propietario, existente == null ? "Nuevo funcionario" : "Editar funcionario", true);
        this.dao = dao;
        this.existente = existente;
        this.cmbTipoDocumento = new JComboBox<>(tiposDocumento.toArray(new Catalogo[0]));
        this.cmbEstadoCivil = new JComboBox<>(estadosCiviles.toArray(new Catalogo[0]));

        construirInterfaz();
        if (existente != null) {
            cargarDatos(existente);
        }

        pack();
        setMinimumSize(new Dimension(700, 560));
        setLocationRelativeTo(propietario);
    }

    public boolean isGuardado() {
        return guardado;
    }

    // ------------------------------------------------------------ INTERFAZ

    private void construirInterfaz() {
        JPanel datos = new JPanel(new GridBagLayout());
        datos.setBorder(BorderFactory.createTitledBorder("Datos personales"));
        agregarCampo(datos, 0, "Tipo de documento *", cmbTipoDocumento);
        agregarCampo(datos, 1, "Número de identificación *", txtNumero);
        agregarCampo(datos, 2, "Nombres *", txtNombres);
        agregarCampo(datos, 3, "Apellidos *", txtApellidos);
        agregarCampo(datos, 4, "Estado civil *", cmbEstadoCivil);
        agregarCampo(datos, 5, "Sexo *", cmbSexo);
        agregarCampo(datos, 6, "Dirección", txtDireccion);
        agregarCampo(datos, 7, "Teléfono", txtTelefono);
        agregarCampo(datos, 8, "Fecha de nacimiento * (AAAA-MM-DD)", txtFechaNacimiento);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Grupo familiar", panelTabla(tablaFamilia, modeloFamilia, 4));
        pestanas.addTab("Formación académica", panelTabla(tablaFormacion, modeloFormacion, 3));
        pestanas.setPreferredSize(new Dimension(680, 200));

        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        botones.add(btnGuardar);
        botones.add(btnCancelar);

        setLayout(new BorderLayout());
        add(datos, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnGuardar);
    }

    private void agregarCampo(JPanel panel, int fila, String etiqueta, java.awt.Component campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 6, 3, 6);
        c.gridy = fila;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panel.add(campo, c);
    }

    /** Crea una pestaña con una tabla editable y los botones para agregar o quitar filas. */
    private JPanel panelTabla(JTable tabla, DefaultTableModel modelo, int columnas) {
        tabla.setRowHeight(22);
        JButton btnAgregar = new JButton("Agregar fila");
        JButton btnQuitar = new JButton("Quitar fila");
        btnAgregar.addActionListener(e -> {
            Object[] vacia = new Object[columnas];
            for (int i = 0; i < columnas; i++) {
                vacia[i] = "";
            }
            modelo.addRow(vacia);
        });
        btnQuitar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                if (tabla.isEditing()) {
                    tabla.getCellEditor().stopCellEditing();
                }
                modelo.removeRow(fila);
            }
        });
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        botones.add(btnAgregar);
        botones.add(btnQuitar);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(botones, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return panel;
    }

    // ----------------------------------------------------- CARGAR (EDITAR)

    private void cargarDatos(Funcionario f) {
        cmbTipoDocumento.setSelectedItem(f.getTipoDocumento());
        cmbEstadoCivil.setSelectedItem(f.getEstadoCivil());
        cmbSexo.setSelectedItem(f.getSexo());
        txtNumero.setText(f.getNumeroIdentificacion());
        txtNombres.setText(f.getNombres());
        txtApellidos.setText(f.getApellidos());
        txtDireccion.setText(f.getDireccion() == null ? "" : f.getDireccion());
        txtTelefono.setText(f.getTelefono() == null ? "" : f.getTelefono());
        txtFechaNacimiento.setText(f.getFechaNacimiento() == null ? "" : f.getFechaNacimiento().toString());

        for (GrupoFamiliar g : f.getGrupoFamiliar()) {
            modeloFamilia.addRow(new Object[]{
                    g.getNombres(), g.getApellidos(),
                    g.getFechaNacimiento() == null ? "" : g.getFechaNacimiento().toString(),
                    g.getRol()});
        }
        for (FormacionAcademica fa : f.getFormaciones()) {
            modeloFormacion.addRow(new Object[]{fa.getUniversidad(), fa.getNivelEstudio(), fa.getTitulo()});
        }
    }

    // ------------------------------------------------------------- GUARDAR

    private void guardar() {
        detenerEdicion(tablaFamilia);
        detenerEdicion(tablaFormacion);
        try {
            Funcionario f = construirFuncionario();
            if (existente == null) {
                dao.crear(f);
            } else {
                dao.actualizar(f);
            }
            guardado = true;
            dispose();
        } catch (ValidacionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (DaoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void detenerEdicion(JTable tabla) {
        if (tabla.isEditing()) {
            tabla.getCellEditor().stopCellEditing();
        }
    }

    /** Lee el formulario, valida cada campo y arma el objeto Funcionario. */
    private Funcionario construirFuncionario() throws ValidacionException {
        Catalogo tipo = (Catalogo) cmbTipoDocumento.getSelectedItem();
        Catalogo estado = (Catalogo) cmbEstadoCivil.getSelectedItem();
        if (tipo == null || estado == null) {
            throw new ValidacionException("No hay tipos de documento o estados civiles cargados. "
                    + "Ejecute primero el script de poblado.");
        }

        String numero = txtNumero.getText().trim();
        if (!numero.matches("[A-Za-z0-9]{4,20}")) {
            throw new ValidacionException("El número de identificación debe tener entre 4 y 20 letras o números, sin espacios.");
        }
        String nombres = obligatorio(txtNombres.getText(), "Nombres", 60);
        String apellidos = obligatorio(txtApellidos.getText(), "Apellidos", 60);

        String telefono = txtTelefono.getText().trim();
        if (!telefono.isEmpty() && !telefono.matches("[0-9+ ]{7,15}")) {
            throw new ValidacionException("El teléfono debe tener entre 7 y 15 dígitos (puede incluir + y espacios).");
        }
        String direccion = txtDireccion.getText().trim();
        if (direccion.length() > 100) {
            throw new ValidacionException("La dirección no puede superar los 100 caracteres.");
        }

        Funcionario f = new Funcionario();
        if (existente != null) {
            f.setId(existente.getId());
        }
        f.setTipoDocumento(tipo);
        f.setNumeroIdentificacion(numero);
        f.setNombres(nombres);
        f.setApellidos(apellidos);
        f.setEstadoCivil(estado);
        f.setSexo((String) cmbSexo.getSelectedItem());
        f.setDireccion(direccion.isEmpty() ? null : direccion);
        f.setTelefono(telefono.isEmpty() ? null : telefono);
        f.setFechaNacimiento(leerFecha(txtFechaNacimiento.getText(), "Fecha de nacimiento", true));
        f.setGrupoFamiliar(leerFamilia());
        f.setFormaciones(leerFormaciones());
        return f;
    }

    private List<GrupoFamiliar> leerFamilia() throws ValidacionException {
        List<GrupoFamiliar> lista = new ArrayList<>();
        for (int i = 0; i < modeloFamilia.getRowCount(); i++) {
            String nombres = celda(modeloFamilia, i, 0);
            String apellidos = celda(modeloFamilia, i, 1);
            String fecha = celda(modeloFamilia, i, 2);
            String rol = celda(modeloFamilia, i, 3);
            if (nombres.isEmpty() && apellidos.isEmpty() && fecha.isEmpty() && rol.isEmpty()) {
                continue; // fila vacía: se ignora
            }
            int n = i + 1;
            if (nombres.isEmpty() || apellidos.isEmpty() || rol.isEmpty()) {
                throw new ValidacionException("Grupo familiar, fila " + n + ": nombres, apellidos y rol son obligatorios.");
            }
            if (nombres.length() > 60 || apellidos.length() > 60 || rol.length() > 30) {
                throw new ValidacionException("Grupo familiar, fila " + n + ": un texto supera el tamaño permitido.");
            }
            lista.add(new GrupoFamiliar(0, nombres, apellidos,
                    leerFecha(fecha, "Grupo familiar, fila " + n + ": fecha de nacimiento", false), rol));
        }
        return lista;
    }

    private List<FormacionAcademica> leerFormaciones() throws ValidacionException {
        List<FormacionAcademica> lista = new ArrayList<>();
        for (int i = 0; i < modeloFormacion.getRowCount(); i++) {
            String universidad = celda(modeloFormacion, i, 0);
            String nivel = celda(modeloFormacion, i, 1);
            String titulo = celda(modeloFormacion, i, 2);
            if (universidad.isEmpty() && nivel.isEmpty() && titulo.isEmpty()) {
                continue;
            }
            int n = i + 1;
            if (universidad.isEmpty() || nivel.isEmpty() || titulo.isEmpty()) {
                throw new ValidacionException("Formación académica, fila " + n
                        + ": universidad, nivel de estudio y título son obligatorios.");
            }
            if (universidad.length() > 100 || nivel.length() > 50 || titulo.length() > 100) {
                throw new ValidacionException("Formación académica, fila " + n + ": un texto supera el tamaño permitido.");
            }
            lista.add(new FormacionAcademica(0, universidad, nivel, titulo));
        }
        return lista;
    }

    // --------------------------------------------------------- VALIDACIONES

    private String obligatorio(String valor, String campo, int maximo) throws ValidacionException {
        String limpio = valor.trim();
        if (limpio.isEmpty()) {
            throw new ValidacionException("El campo \"" + campo + "\" es obligatorio.");
        }
        if (limpio.length() > maximo) {
            throw new ValidacionException("El campo \"" + campo + "\" no puede superar los " + maximo + " caracteres.");
        }
        return limpio;
    }

    private LocalDate leerFecha(String texto, String campo, boolean obligatoria) throws ValidacionException {
        String limpio = texto.trim();
        if (limpio.isEmpty()) {
            if (obligatoria) {
                throw new ValidacionException("El campo \"" + campo + "\" es obligatorio.");
            }
            return null;
        }
        try {
            LocalDate fecha = LocalDate.parse(limpio);
            if (fecha.isAfter(LocalDate.now())) {
                throw new ValidacionException(campo + " no puede ser una fecha futura.");
            }
            return fecha;
        } catch (DateTimeParseException e) {
            throw new ValidacionException(campo + " no es válida. Use el formato AAAA-MM-DD (ejemplo: 1990-05-23).");
        }
    }

    private String celda(DefaultTableModel modelo, int fila, int columna) {
        Object valor = modelo.getValueAt(fila, columna);
        return valor == null ? "" : valor.toString().trim();
    }
}
