
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Проверяет загрузку, сброс и ошибки виртуальной файловой системы. */
public class Stage3Test {
    private static final int EMPTY_COUNT = 0;
    private static final int ROOT_DIRECTORY_COUNT = 1;
    private static final int SEVERAL_FILE_COUNT = 3;
    private static final int DEEP_MIN_DIRECTORY_COUNT = 4;
    private static final int DEEP_FILE_COUNT = 2;

    public static void main(String[] args) throws Exception {
        testLoading();
        testReset();
        testErrors();
        System.out.println("Тесты третьего этапа пройдены!");
    }

    /** Проверяет три варианта виртуальной файловой системы. */
    private static void testLoading() throws IOException {
        VirtualFileSystem minimal = VirtualFileSystem.load(
                Path.of("data", "minimal.json"));
        check(minimal.directoryCount() == ROOT_DIRECTORY_COUNT);
        check(minimal.fileCount() == EMPTY_COUNT);

        VirtualFileSystem several = VirtualFileSystem.load(
                Path.of("data", "several.json"));
        check(several.fileCount() == SEVERAL_FILE_COUNT);

        VirtualFileSystem deep = VirtualFileSystem.load(
                Path.of("data", "deep.json"));
        check(deep.directoryCount() >= DEEP_MIN_DIRECTORY_COUNT);
        check(deep.fileCount() == DEEP_FILE_COUNT);
    }

    /** Проверяет сброс VFS и физического представления. */
    private static void testReset() throws IOException {
        Path temp = Files.createTempFile("vfs-test-", ".json");

        try {
            Files.copy(Path.of("data", "deep.json"), temp,
                    StandardCopyOption.REPLACE_EXISTING);
            VirtualFileSystem vfs = VirtualFileSystem.load(temp);
            CommandProcessor processor = new CommandProcessor(vfs);

            check(processor.execute("vfs-init wrong").hasError());
            check(!processor.execute("vfs-init").hasError());
            check(vfs.directoryCount() == ROOT_DIRECTORY_COUNT);
            check(vfs.fileCount() == EMPTY_COUNT);

            VirtualFileSystem reloaded = VirtualFileSystem.load(temp);
            check(reloaded.directoryCount() == ROOT_DIRECTORY_COUNT);
            check(reloaded.fileCount() == EMPTY_COUNT);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    /** Проверяет отсутствующий файл и некорректный JSON. */
    private static void testErrors() throws IOException {
        expectLoadError(Path.of("data", "missing.json"));
        Path invalid = Files.createTempFile("vfs-invalid-", ".json");

        try {
            Files.writeString(invalid, "{\"type\":\"directory\"}");
            expectLoadError(invalid);
        } finally {
            Files.deleteIfExists(invalid);
        }
    }

    /** Проверяет, что загрузка завершилась IOException. */
    private static void expectLoadError(Path path) {
        boolean failed = false;

        try {
            VirtualFileSystem.load(path);
        } catch (IOException e) {
            failed = true;
        }

        check(failed);
    }

    /** Прерывает тестирование при ошибке проверки. */
    private static void check(boolean condition) {
        if (!condition) {
            throw new AssertionError("Тест третьего этапа не пройден");
        }
    }
}
