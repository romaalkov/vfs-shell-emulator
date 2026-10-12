import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Тестирование виртуальной файловой системы. */
public class Stage3Test {

    public static void main(String[] args) throws Exception {
        testLoading();
        testReset();
        testErrors();
        System.out.println("Тесты третьего этапа пройдены!");
    }

    private static void testLoading() throws IOException {
        VirtualFileSystem minimal =
                VirtualFileSystem.load(Path.of("data", "minimal.json"));
        check(minimal.directoryCount() == 1);
        check(minimal.fileCount() == 0);

        VirtualFileSystem several =
                VirtualFileSystem.load(Path.of("data", "several.json"));
        check(several.fileCount() == 3);

        VirtualFileSystem deep =
                VirtualFileSystem.load(Path.of("data", "deep.json"));
        check(deep.directoryCount() >= 4);
        check(deep.fileCount() == 2);
    }

    private static void testReset() throws IOException {
        Path temp = Files.createTempFile("vfs-test-", ".json");

        try {
            Files.copy(Path.of("data", "deep.json"), temp,
                    StandardCopyOption.REPLACE_EXISTING);

            VirtualFileSystem vfs = VirtualFileSystem.load(temp);
            CommandProcessor processor = new CommandProcessor(vfs);

            check(processor.execute("vfs-init wrong").hasError());
            check(!processor.execute("vfs-init").hasError());
            check(vfs.directoryCount() == 1);
            check(vfs.fileCount() == 0);

            VirtualFileSystem reloaded = VirtualFileSystem.load(temp);
            check(reloaded.directoryCount() == 1);
            check(reloaded.fileCount() == 0);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

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

    private static void expectLoadError(Path path) {
        boolean failed = false;

        try {
            VirtualFileSystem.load(path);
        } catch (IOException e) {
            failed = true;
        }

        check(failed);
    }

    private static void check(boolean condition) {
        if (!condition) {
            throw new AssertionError("Тест третьего этапа не пройден");
        }
    }
}
