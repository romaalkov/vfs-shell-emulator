public class CommandProcessorTest {

    public static void main(String[] args) {
        CommandProcessor processor = new CommandProcessor();

        check(processor.execute("ls").message().equals("ls: []"));
        check(processor.execute("ls home").message().equals("ls: [home]"));
        check(processor.execute("cd home").message().equals("cd: [home]"));

        check(processor.execute("cd").message().startsWith("Ошибка"));
        check(processor.execute("hello").message().startsWith("Ошибка"));
        check(processor.execute("ls a b").message().startsWith("Ошибка"));

        check(!processor.execute("exit abc").shouldExit());
        check(processor.execute("exit").shouldExit());

        System.out.println("Все тесты пройдены!");
    }

    private static void check(boolean condition) {
        if (!condition) {
            throw new AssertionError("Тест не пройден");
        }
    }
}