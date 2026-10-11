
import javax.swing.*;
import java.awt.*;

/**
 * Графическое окно эмулятора командной оболочки.
 */
public class ShellWindow extends JFrame {

    private final JTextArea outputArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final CommandProcessor processor = new CommandProcessor();

    public ShellWindow() {
        setTitle("VFS - Shell Emulator");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("$ "), BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);

        add(new JScrollPane(outputArea), BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        inputField.addActionListener(e -> handleInput());
        outputArea.append("Welcome to VFS Shell Emulator\n");
    }

    /**
     * Читает команду и отображает результат её выполнения.
     */
    private void handleInput() {
        String input = inputField.getText().trim();
        inputField.setText("");

        if (input.isEmpty()) {
            return;
        }

        outputArea.append("$ " + input + "\n");

        CommandProcessor.Result result = processor.execute(input);
        outputArea.append(result.message() + "\n");

        if (result.shouldExit()) {
            dispose();
        }
    }
}
