package principal;

import dao.CatalogoDAOImpl;
import dao.FuncionarioDAOImpl;
import vista.FuncionarioFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Punto de entrada: arma la ventana principal inyectándole las implementaciones del DAO. */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorada) {
                // Si falla, Swing usa su apariencia por defecto.
            }
            new FuncionarioFrame(new FuncionarioDAOImpl(), new CatalogoDAOImpl()).setVisible(true);
        });
    }
}
