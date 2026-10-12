
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;

/** Точка входа в эмулятор оболочки. */
public class Main {
    public static void main(String[] args) {
        AppConfig config;

        try {
            config = AppConfig.parse(args);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Ошибка параметров", JOptionPane.ERROR_MESSAGE);
            return;
        }

        VirtualFileSystem vfs;

        try {
            vfs = VirtualFileSystem.load(config.vfsPath());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Ошибка загрузки VFS", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            ShellWindow window = new ShellWindow(config, vfs);
            window.setVisible(true);
            SwingUtilities.invokeLater(window::executeStartupScript);
        });
    }
}
