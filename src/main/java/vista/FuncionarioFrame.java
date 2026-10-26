package vista;

import dao.CatalogoDAO;
import dao.FuncionarioDAO;
import excepciones.DaoException;
import modelo.Catalogo;
import modelo.Funcionario;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana principal: lista los funcionarios y ofrece los botones del CRUD.
 * Solo conoce las INTERFACES FuncionarioDAO y CatalogoDAO, nunca SQL.
 */
public class FuncionarioFrame extends JFrame {

    private final FuncionarioDAO dao;
    private List<Catalogo> tiposDocumento = new ArrayList<>();
    private List<Catalogo> estadosCiviles = new ArrayList<>();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Tipo doc.", "Número", "Nombres", "Apellidos", "Estado civil", "Sexo", "Teléfono"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);

    public FuncionarioFrame(FuncionarioDAO dao, CatalogoDAO catalogoDao) {
        super("Gestión de funcionarios - Recursos Humanos");
        this.dao = dao;

        try {
            tiposDocumento = catalogoDao.listarTiposDocumento();
            estadosCiviles = catalogoDao.listarEstadosCiviles();
        } catch (DaoException e) {
            mostrarError(e);
        }

        construirInterfaz();
        cargarTabla();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 480);
        setLocationRelativeTo(null);
    }

    private void construirInterfaz() {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.setAutoCreateRowSorter(true);
        tabla.getColumnModel().getColumn(0).setMaxWidth(50);

        JButton btnNuevo = new JButton("Nuevo");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnActualizar = new JButton("Actualizar lista");

        btnNuevo.addActionListener(e -> nuevo());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnActualizar.addActionListener(e -> cargarTabla());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        botones.add(btnNuevo);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(btnActualizar);

        setLayout(new BorderLayout());
        add(botones, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    // ------------------------------------------------------------------ LISTAR

    private void cargarTabla() {
        modelo.setRowCount(0);
        try {
            for (Funcionario f : dao.listar()) {
                modelo.addRow(new Object[]{
                        f.getId(),
                        f.getTipoDocumento().getNombre(),
                        f.getNumeroIdentificacion(),
                        f.getNombres(),
                        f.getApellidos(),
                        f.getEstadoCivil().getNombre(),
                        f.getSexo(),
                        f.getTelefono()
                });
            }
        } catch (DaoException e) {
            mostrarError(e);
        }
    }

    // ------------------------------------------------------------------- CREAR

    private void nuevo() {
        FuncionarioDialog dialogo = new FuncionarioDialog(this, dao, tiposDocumento, estadosCiviles, null);
        dialogo.setVisible(true);
        if (dialogo.isGuardado()) {
            cargarTabla();
        }
    }

    // ------------------------------------------------------------------ EDITAR

    private void editar() {
        Integer id = idSeleccionado();
        if (id == null) {
            return;
        }
        try {
            Funcionario f = dao.buscarPorId(id);
            FuncionarioDialog dialogo = new FuncionarioDialog(this, dao, tiposDocumento, estadosCiviles, f);
            dialogo.setVisible(true);
            if (dialogo.isGuardado()) {
                cargarTabla();
            }
        } catch (DaoException e) {
            mostrarError(e);
        }
    }

    // ---------------------------------------------------------------- ELIMINAR

    private void eliminar() {
        Integer id = idSeleccionado();
        if (id == null) {
            return;
        }
        int fila = tabla.convertRowIndexToModel(tabla.getSelectedRow());
        String nombre = modelo.getValueAt(fila, 3) + " " + modelo.getValueAt(fila, 4);
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Eliminar a " + nombre + "?\nTambién se borrarán su grupo familiar y su formación académica.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dao.eliminar(id);
            cargarTabla();
        } catch (DaoException e) {
            mostrarError(e);
        }
    }

    // --------------------------------------------------------------- AUXILIARES

    /** Devuelve el id de la fila seleccionada, o avisa al usuario si no hay ninguna. */
    private Integer idSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione primero un funcionario de la tabla.",
                    "Sin selección", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return (Integer) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
    }

    private void mostrarError(DaoException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
