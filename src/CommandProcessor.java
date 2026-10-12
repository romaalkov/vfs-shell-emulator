
import java.util.Arrays;

/** Разбор и выполнение команд эмулятора. */
public class CommandProcessor {

    /** Результат команды: сообщение, завершение и признак ошибки. */
    public record Result(String message, boolean shouldExit, boolean hasError) {
    }

    /** Выполняет введённую команду. */
    public Result execute(String line) {
        if (line == null || line.isBlank()) {
            return new Result("", false, false);
        }

        String[] parts = line.trim().split("\\s+");
        String command = parts[0];
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        return switch (command) {
            case "ls" -> handleLs(args);
            case "cd" -> handleCd(args);
            case "exit" -> handleExit(args);
            default -> error("неизвестная команда '" + command + "'");
        };
    }

    private Result handleLs(String[] args) {
        if (args.length > 1) {
            return error("ls принимает не более одного аргумента");
        }

        return new Result("ls: " + Arrays.toString(args), false, false);
    }

    private Result handleCd(String[] args) {
        if (args.length != 1) {
            return error("cd требует один аргумент");
        }

        return new Result("cd: " + Arrays.toString(args), false, false);
    }

    private Result handleExit(String[] args) {
        if (args.length != 0) {
            return error("exit не принимает аргументы");
        }

        return new Result("Выход из эмулятора", true, false);
    }

    private Result error(String message) {
        return new Result("Ошибка: " + message, false, true);
    }
}
