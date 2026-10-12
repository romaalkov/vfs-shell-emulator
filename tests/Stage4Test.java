
import java.nio.file.Path;

/** Проверяет реальные команды работы с VFS. */
public class Stage4Test {

    public static void main(String[] args) throws Exception {
        VirtualFileSystem vfs = VirtualFileSystem.load(
                Path.of("data", "deep.json"));

        CommandProcessor processor = new CommandProcessor(vfs);

        check(processor.execute("ls").message().equals("etc\nhome"),
                "ls корневого каталога");

        check(processor.execute("cd home").message()
                .equals("Текущий каталог: /home"), "cd home");

        check(processor.execute("ls").message().equals("user"),
                "ls текущего каталога");

        check(processor.execute("cd user").message()
                .equals("Текущий каталог: /home/user"), "cd user");

        check(processor.execute("cd projects").message()
                .equals("Текущий каталог: /home/user/projects"), "cd projects");

        check(processor.execute("ls").message().equals("readme.txt"),
                "ls вложенного каталога");

        check(processor.execute("du readme.txt").message()
                .equals("5\t/home/user/projects/readme.txt"), "du файла");

        check(processor.execute("du /").message()
                .equals("11\t/"), "du корневого каталога");

        check(!processor.execute("date").message().isBlank(), "date");

        check(processor.execute("cd ..").message()
                .equals("Текущий каталог: /home/user"), "cd ..");

        check(processor.execute("ls /missing").hasError(),
                "несуществующий путь");

        check(processor.execute("cd /etc/config.txt").hasError(),
                "cd к файлу");

        check(processor.execute("du /missing").hasError(),
                "du несуществующего файла");

        check(processor.execute("date extra").hasError(),
                "неправильные аргументы date");

        System.out.println("Тесты четвёртого этапа пройдены!");
    }

    private static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError("Не пройден тест: " + name);
        }
    }
}
