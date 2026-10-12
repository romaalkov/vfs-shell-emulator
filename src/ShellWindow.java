
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** Графическое окно эмулятора командной оболочки. */
public class ShellWindow extends JFrame {
    private final JTextArea outputArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final CommandProcessor processor;
    private final AppConfig config;
    private final CsvLogger logger;

    public ShellWindow(AppConfig config, VirtualFileSystem vfs) {
    this.config = config;
    this.logger = new CsvLogger(config.logPath());
    this.processor = new CommandProcessor(vfs);

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
        outputArea.append(config.describe() + "\n");
        outputArea.append(vfs.describe() + "\n");
    }

    /** Выполняет команду, введённую в графическом окне. */
    private void handleInput() {
        String input = inputField.getText().trim();
        inputField.setText("");

        if (!input.isEmpty()) {
            executeLine(input);
        }
    }

    /** Показывает и выполняет команды стартового скрипта по очереди. */
    public void executeStartupScript() {
        if (config.scriptPath() == null) {
            return;
        }

        try {
            for (String line : Files.readAllLines(
                    config.scriptPath(), StandardCharsets.UTF_8)) {

                String command = line.trim();
                if (command.isEmpty()) {
                    continue;
                }

                CommandProcessor.Result result = executeLine(command);

                if (result.hasError()) {
                    outputArea.append(
                            "Скрипт остановлен после первой ошибки.\n");
                    break;
                }

                if (result.shouldExit()) {
                    break;
                }
            }
        } catch (IOException e) {
            outputArea.append(
                    "Ошибка чтения скрипта: " + e.getMessage() + "\n");
        }
    }

    /** Выполняет команду, выводит результат и записывает его в лог. */
    private CommandProcessor.Result executeLine(String line) {
        outputArea.append("$ " + line + "\n");

        CommandProcessor.Result result = processor.execute(line);
        outputArea.append(result.message() + "\n");

        try {
            logger.log(line, result.hasError() ? result.message() : "");
        } catch (IOException e) {
            String error = "Ошибка записи лога: " + e.getMessage();
            outputArea.append(error + "\n");
            return new CommandProcessor.Result(error, false, true);
        }

        outputArea.setCaretPosition(outputArea.getDocument().getLength());

        if (result.shouldExit()) {
            dispose();
        }

        return result;
    }
}
