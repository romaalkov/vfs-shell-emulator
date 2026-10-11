import javax.swing.SwingUtilities;

/**
 * Точка входа в эмулятор.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ShellWindow window = new ShellWindow();
            window.setVisible(true);
        });
    }
}