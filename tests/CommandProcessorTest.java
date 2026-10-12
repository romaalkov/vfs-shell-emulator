
import java.nio.file.Path;

/** Проверка разбора и выполнения команд. */
public class CommandProcessorTest {

    public static void main(String[] args) throws Exception {
        VirtualFileSystem vfs = VirtualFileSystem.load(
                Path.of("data", "several.json"));
        CommandProcessor processor = new CommandProcessor(vfs);

        check(processor.execute("ls").message().contains("documents"),
                "ls без аргументов");

        check(processor.execute("ls documents").message().contains("hello.txt"),
                "ls с каталогом");

        check(processor.execute("cd documents").message()
                .equals("Текущий каталог: /documents"), "cd");

        check(processor.execute("ls hello.txt").message()
                .equals("hello.txt"), "ls с файлом");

        check(processor.execute("cd ..").message()
                .equals("Текущий каталог: /"), "cd ..");

        check(processor.execute("cd").hasError(), "cd без аргумента");
        check(processor.execute("hello").hasError(), "неизвестная команда");
        check(processor.execute("ls a b").hasError(), "лишние аргументы");

        check(!processor.execute("exit abc").shouldExit(),
                "неправильный exit");
        check(processor.execute("exit").shouldExit(), "exit");

        System.out.println("Все тесты пройдены!");
    }

    private static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError("Не пройден тест: " + name);
        }
    }
}
