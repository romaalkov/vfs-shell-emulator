
import java.util.Arrays;

/**
 * Обрабатывает команды пользователя.
 */
public class CommandProcessor {

    public record Result(String message, boolean shouldExit) {
    }

    /**
     * Разбирает введённую строку и выполняет команду.
     *
     * @param line строка пользователя
     * @return результат выполнения
     */
    public Result execute(String line) {
        if (line == null || line.isBlank()) {
            return new Result("", false);
        }

        String[] parts = line.trim().split("\\s+");
        String command = parts[0];
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        return switch (command) {
            case "ls" -> handleLs(args);
            case "cd" -> handleCd(args);
            case "exit" -> handleExit(args);
            default -> new Result(
                "Ошибка: неизвестная команда '" + command + "'", false
            );
        };
    }

    private Result handleLs(String[] args) {
        if (args.length > 1) {
            return new Result("Ошибка: ls принимает не более одного аргумента", false);
        }

        return new Result("ls: " + Arrays.toString(args), false);
    }

    private Result handleCd(String[] args) {
        if (args.length != 1) {
            return new Result("Ошибка: cd требует один аргумент", false);
        }

        return new Result("cd: " + Arrays.toString(args), false);
    }

    private Result handleExit(String[] args) {
        if (args.length != 0) {
            return new Result("Ошибка: exit не принимает аргументы", false);
        }

        return new Result("Выход из эмулятора", true);
    }
}
