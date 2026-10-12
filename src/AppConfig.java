
import java.nio.file.Path;

/** Параметры запуска приложения. */
public record AppConfig(Path vfsPath, Path logPath, Path scriptPath) {

    /** Считывает пары «ключ значение» из аргументов командной строки. */
    public static AppConfig parse(String[] args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Каждому параметру нужен путь.");
        }

        Path vfs = null;
        Path log = null;
        Path script = null;

        for (int i = 0; i < args.length; i += 2) {
            String key = args[i];
            String value = args[i + 1];

            if (value.isBlank() || value.startsWith("--")) {
                throw new IllegalArgumentException("Не указан путь для " + key);
            }

            Path path = Path.of(value);
            switch (key) {
                case "--vfs" -> vfs = path;
                case "--log" -> log = path;
                case "--script" -> script = path;
                default -> throw new IllegalArgumentException(
                        "Неизвестный параметр: " + key);
            }
        }

        return new AppConfig(vfs, log, script);
    }

    /** Отладочная информация о настройках. */
    public String describe() {
        return "Параметры запуска:\n"
                + "VFS: " + value(vfsPath) + "\n"
                + "Лог-файл: " + value(logPath) + "\n"
                + "Стартовый скрипт: " + value(scriptPath) + "\n";
    }

    private static String value(Path path) {
        return path == null ? "(не указан)" : path.toString();
    }
}
