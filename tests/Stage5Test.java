
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/** Проверяет команду chown и сохранность исходной VFS. */
public class Stage5Test {

    public static void main(String[] args) throws Exception {
        testChown();
        testErrors();
        testMemoryOnly();

        System.out.println("Тесты пятого этапа пройдены!");
    }

    /** Проверяет изменение владельца файла и каталога. */
    private static void testChown() throws Exception {
        VirtualFileSystem vfs = loadVfs();
        CommandProcessor processor = new CommandProcessor(vfs);

        String file = "/home/user/projects/readme.txt";

        check(vfs.ownerOf("/", file).equals("user"),
                "начальный владелец файла");

        check(!processor.execute("chown alice " + file).hasError(),
                "изменение владельца файла");

        check(vfs.ownerOf("/", file).equals("alice"),
                "новый владелец файла");

        check(!processor.execute("chown bob /home/user/projects").hasError(),
                "изменение владельца каталога");

        check(vfs.ownerOf("/", "/home/user/projects").equals("bob"),
                "новый владелец каталога");

        check(vfs.ownerOf("/", file).equals("alice"),
                "chown каталога не меняет владельца вложенного файла");

        processor.execute("cd /home/user/projects");
        check(!processor.execute("chown carol readme.txt").hasError(),
                "относительный путь");

        check(vfs.ownerOf("/", file).equals("carol"),
                "изменение по относительному пути");
    }

    /** Проверяет обработку неправильных аргументов. */
    private static void testErrors() throws Exception {
        CommandProcessor processor = new CommandProcessor(loadVfs());

        check(processor.execute("chown").hasError(),
                "нет аргументов");

        check(processor.execute("chown alice").hasError(),
                "нет пути");

        check(processor.execute("chown alice /etc extra").hasError(),
                "лишний аргумент");

        check(processor.execute("chown alice /missing").hasError(),
                "несуществующий путь");

        check(!processor.execute("ls /").hasError(),
                "работа старой команды ls");
    }

    /** Проверяет отсутствие изменений исходного JSON. */
    private static void testMemoryOnly() throws Exception {
        Path path = Path.of("data", "deep.json");
        byte[] before = Files.readAllBytes(path);

        VirtualFileSystem vfs = VirtualFileSystem.load(path);
        CommandProcessor processor = new CommandProcessor(vfs);

        check(!processor.execute("chown alice /etc").hasError(),
                "изменение владельца в памяти");

        byte[] after = Files.readAllBytes(path);

        check(Arrays.equals(before, after),
                "исходный JSON не должен изменяться");

        VirtualFileSystem reloaded = VirtualFileSystem.load(path);

        check(reloaded.ownerOf("/", "/etc").equals("root"),
                "повторная загрузка восстанавливает исходного владельца");
    }

    private static VirtualFileSystem loadVfs() throws Exception {
        return VirtualFileSystem.load(Path.of("data", "deep.json"));
    }

    private static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError("Не пройден тест: " + name);
        }
    }
}
