
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;

/** Выполняет команды эмулятора с учётом текущего каталога VFS. */
public class CommandProcessor {
    private static final int CHOWN_ARGUMENT_COUNT = 2;
    private final VirtualFileSystem vfs;
    private String currentDirectory = "/";

    /** Результат выполнения команды. */
    public record Result(String message, boolean shouldExit, boolean hasError) {
    }

    public CommandProcessor() {
        this(VirtualFileSystem.empty());
    }

    public CommandProcessor(VirtualFileSystem vfs) {
        this.vfs = vfs;
    }

    /** Разбирает введённую команду. */
    public Result execute(String line) {
        if (line == null || line.isBlank()) {
            return new Result("", false, false);
        }

        String[] parts = line.trim().split("\\s+");
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        
        return switch (parts[0]) {
            case "ls" -> handleLs(args);
            case "cd" -> handleCd(args);
            case "du" -> handleDu(args);
            case "date" -> handleDate(args);
            case "chown" -> handleChown(args);
            case "vfs-init" -> handleVfsInit(args);
            case "exit" -> handleExit(args);
            default -> error("неизвестная команда '" + parts[0] + "'");
        };

    }

    private Result handleLs(String[] args) {
        if (args.length > 1) {
            return error("ls принимает не более одного аргумента");
        }

        String path = args.length == 0 ? "." : args[0];

        try {
            return success(vfs.list(currentDirectory, path));
        } catch (IllegalArgumentException e) {
            return error("ls: " + e.getMessage());
        }
    }

    private Result handleCd(String[] args) {
        if (args.length != 1) {
            return error("cd требует один аргумент");
        }

        try {
            currentDirectory = vfs.changeDirectory(currentDirectory, args[0]);
            return success("Текущий каталог: " + currentDirectory);
        } catch (IllegalArgumentException e) {
            return error("cd: " + e.getMessage());
        }
    }

    private Result handleDu(String[] args) {
        if (args.length > 1) {
            return error("du принимает не более одного аргумента");
        }

        String path = args.length == 0 ? "." : args[0];

        try {
            return success(vfs.diskUsage(currentDirectory, path));
        } catch (IllegalArgumentException e) {
            return error("du: " + e.getMessage());
        }
    }

    private Result handleDate(String[] args) {
        if (args.length != 0) {
            return error("date не принимает аргументы");
        }

        DateTimeFormatter format = DateTimeFormatter.ofPattern(
                "EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);

        return success(ZonedDateTime.now().format(format));
    }

    private Result handleVfsInit(String[] args) {
        if (args.length != 0) {
            return error("vfs-init не принимает аргументы");
        }

        try {
            vfs.reset();
            currentDirectory = "/";
            return success("VFS сброшена. " + vfs.describe());
        } catch (IOException e) {
            return error("не удалось сбросить VFS: " + e.getMessage());
        }
    }

    private Result handleExit(String[] args) {
        if (args.length != 0) {
            return error("exit не принимает аргументы");
        }

        return new Result("Выход из эмулятора", true, false);
    }

    private Result success(String message) {
        return new Result(message, false, false);
    }

    private Result error(String message) {
        return new Result("Ошибка: " + message, false, true);
    }
    
    /**
     * Изменяет владельца виртуального файла или каталога.
     */
    private Result handleChown(String[] args) {
        if (args.length != CHOWN_ARGUMENT_COUNT) {
            return error("chown требует два аргумента: владелец и путь");
        }

        try {
            String path = vfs.changeOwner(
                    currentDirectory, args[1], args[0]);

            return success("Владелец " + path
                    + " изменён на " + args[0]);

        } catch (IllegalArgumentException e) {
            return error("chown: " + e.getMessage());
        }
    }

}
