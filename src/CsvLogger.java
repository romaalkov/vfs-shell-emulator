
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Сохраняет вызовы команд в CSV-файле. */
public class CsvLogger {
    private final Path logPath;

    public CsvLogger(Path logPath) {
        this.logPath = logPath;
    }

    /** Записывает событие выполнения команды и возникшую ошибку. */
    public void log(String line, String errorMessage) throws IOException {
        if (logPath == null) {
            return;
        }

        Path parent = logPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        boolean needsHeader = Files.notExists(logPath)
                || Files.size(logPath) == 0;

        String[] parts = line.trim().split("\\s+");
        String arguments = String.join(" ",
                Arrays.copyOfRange(parts, 1, parts.length));

        try (BufferedWriter writer = Files.newBufferedWriter(
                logPath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            if (needsHeader) {
                writer.write("timestamp,command,arguments,error\n");
            }

            writer.write(csv(OffsetDateTime.now().toString()) + ","
                    + csv(parts[0]) + "," + csv(arguments) + ","
                    + csv(errorMessage) + "\n");
        }
    }

    private String csv(String text) {
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
