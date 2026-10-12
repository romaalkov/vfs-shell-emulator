
import java.nio.file.Files;
import java.nio.file.Path;

/** Проверяет параметры запуска и запись CSV. */
public class Stage2Test {

    public static void main(String[] args) throws Exception {
        AppConfig config = AppConfig.parse(new String[] {
                "--vfs", "data/example.json",
                "--log", "logs/test.csv",
                "--script", "tests/startup-ok.txt"
        });

    check(config.vfsPath().equals(Path.of("data", "example.json")));
    check(config.logPath().equals(Path.of("logs", "test.csv")));
    check(config.scriptPath().equals(Path.of("tests", "startup-ok.txt")));

        boolean failed = false;

        try {
            AppConfig.parse(new String[] {"--log"});
        } catch (IllegalArgumentException e) {
            failed = true;
        }

        check(failed);

        Path file = Files.createTempFile("vfs-stage2-", ".csv");

        try {
            CsvLogger logger = new CsvLogger(file);
            logger.log("ls documents", "");
            logger.log("cd", "Ошибка: cd требует один аргумент");

            String csv = Files.readString(file);

            check(csv.contains("timestamp,command,arguments,error"));
            check(csv.contains("\"ls\",\"documents\",\"\""));
            check(csv.contains("Ошибка: cd требует один аргумент"));
        } finally {
            Files.deleteIfExists(file);
        }

        System.out.println("Тесты второго этапа пройдены!");
    }

    private static void check(boolean condition) {
        if (!condition) {
            throw new AssertionError("Тест второго этапа не пройден");
        }
    }
}
