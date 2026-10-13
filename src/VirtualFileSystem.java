
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Виртуальная файловая система, хранящаяся в памяти. */
public class VirtualFileSystem {
    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private final Path sourcePath;
    private VfsNode root;

    private VirtualFileSystem(Path path, VfsNode root) {
        this.sourcePath = path;
        this.root = root;
    }

    /** Создаёт VFS с пустым корневым каталогом. */
    public static VirtualFileSystem empty() {
        return new VirtualFileSystem(null, VfsNode.defaultRoot());
    }

    /** Загружает JSON-файл, проверяя формат и структуру VFS. */
    public static VirtualFileSystem load(Path path) throws IOException {
        if (path == null) {
            return empty();
        }

        try (Reader reader = Files.newBufferedReader(
                path, StandardCharsets.UTF_8)) {
            VfsNode loadedRoot = GSON.fromJson(reader, VfsNode.class);
            validate(loadedRoot, true);
            return new VirtualFileSystem(path, loadedRoot);
        } catch (JsonParseException | IllegalArgumentException e) {
            throw new IOException("Неверный формат VFS: "
                    + e.getMessage(), e);
        }
    }

    /** Проверяет узел, делегируя отдельные проверки небольшим методам. */
    private static void validate(VfsNode node, boolean isRoot) {
        validateRequiredFields(node);
        validateName(node.name, isRoot);

        if (node.type.equals("directory")) {
            validateDirectory(node);
            return;
        }

        if (node.type.equals("file") && !isRoot) {
            validateFile(node);
            return;
        }

        throw new IllegalArgumentException("Неизвестный тип объекта");
    }

    /** Проверяет обязательные поля файла или каталога. */
    private static void validateRequiredFields(VfsNode node) {
        if (node == null || node.name == null || node.type == null) {
            throw new IllegalArgumentException("Некорректный объект VFS");
        }

        if (node.owner == null || node.owner.isBlank()) {
            throw new IllegalArgumentException("Некорректный объект VFS");
        }
    }

    /** Проверяет имя корневого или вложенного узла. */
    private static void validateName(String name, boolean isRoot) {
        if (isRoot) {
            if (!name.equals("/")) {
                throw new IllegalArgumentException("Корень должен иметь имя /");
            }
            return;
        }

        if (name.isBlank() || name.contains("/")) {
            throw new IllegalArgumentException("Некорректное имя файла");
        }
        if (name.equals(".") || name.equals("..")) {
            throw new IllegalArgumentException("Некорректное имя файла");
        }
    }

    /** Проверяет каталог и уникальность имён дочерних объектов. */
    private static void validateDirectory(VfsNode node) {
        if (node.children == null || node.contentBase64 != null) {
            throw new IllegalArgumentException("Неверный формат каталога");
        }

        Set<String> names = new HashSet<>();
        for (VfsNode child : node.children) {
            validate(child, false);
            if (!names.add(child.name)) {
                throw new IllegalArgumentException(
                        "Повторяющееся имя: " + child.name);
            }
        }
    }

    /** Проверяет файл и корректность содержимого Base64. */
    private static void validateFile(VfsNode node) {
        if (node.children != null || node.contentBase64 == null) {
            throw new IllegalArgumentException("Неверный формат файла");
        }

        try {
            Base64.getDecoder().decode(node.contentBase64);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Некорректные данные Base64", e);
        }
    }

    /** Рекурсивно подсчитывает объекты заданного типа. */
    private int count(VfsNode node, String type) {
        int result = node.type.equals(type) ? 1 : 0;

        if (node.children != null) {
            for (VfsNode child : node.children) {
                result += count(child, type);
            }
        }
        return result;
    }

    /** Возвращает количество каталогов, включая корневой. */
    public int directoryCount() {
        return count(root, "directory");
    }

    /** Возвращает количество файлов. */
    public int fileCount() {
        return count(root, "file");
    }

    /** Возвращает краткую информацию о VFS. */
    public String describe() {
        return "VFS: каталогов " + directoryCount()
                + ", файлов " + fileCount();
    }

    /** Сбрасывает VFS и её физическое представление, если оно задано. */
    public void reset() throws IOException {
        VfsNode freshRoot = VfsNode.defaultRoot();

        if (sourcePath != null) {
            Files.writeString(sourcePath, GSON.toJson(freshRoot),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        }
        root = freshRoot;
    }

    /** Возвращает список объектов каталога или имя файла. */
    public String list(String currentDirectory, String path) {
        Location target = resolve(currentDirectory, path);
        if (target.node().type.equals("file")) {
            return target.node().name;
        }

        List<String> names = new ArrayList<>();
        for (VfsNode child : target.node().children) {
            names.add(child.name);
        }
        Collections.sort(names);
        return String.join("\n", names);
    }

    /** Проверяет каталог и возвращает его абсолютный путь. */
    public String changeDirectory(String currentDirectory, String path) {
        Location target = resolve(currentDirectory, path);
        if (!target.node().type.equals("directory")) {
            throw new IllegalArgumentException(
                    "не является каталогом: " + target.path());
        }
        return target.path();
    }

    /** Возвращает размер файла или каталога в байтах. */
    public String diskUsage(String currentDirectory, String path) {
        Location target = resolve(currentDirectory, path);
        return sizeInBytes(target.node()) + "\t" + target.path();
    }

    /** Рекурсивно суммирует размеры содержимого файлов. */
    private long sizeInBytes(VfsNode node) {
        if (node.type.equals("file")) {
            return Base64.getDecoder().decode(node.contentBase64).length;
        }

        long total = 0;
        for (VfsNode child : node.children) {
            total += sizeInBytes(child);
        }
        return total;
    }

    /** Изменяет владельца файла или каталога только в памяти. */
    public String changeOwner(String currentDirectory, String path, String owner) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("не указан владелец");
        }

        Location target = resolve(currentDirectory, path);
        target.node().owner = owner;
        return target.path();
    }

    /** Возвращает владельца файла или каталога. */
    public String ownerOf(String currentDirectory, String path) {
        return resolve(currentDirectory, path).node().owner;
    }

    /** Находит виртуальный файл или каталог по пути. */
    private Location resolve(String currentDirectory, String path) {
        String absolutePath = normalize(currentDirectory, path);
        VfsNode node = root;

        if (absolutePath.equals("/")) {
            return new Location("/", node);
        }

        for (String part : absolutePath.substring(1).split("/")) {
            node = findChild(node, part, absolutePath);
        }
        return new Location(absolutePath, node);
    }

    /** Находит дочерний объект и сообщает об ошибке отсутствующего пути. */
    private VfsNode findChild(VfsNode parent, String name, String fullPath) {
        if (!parent.type.equals("directory")) {
            throw new IllegalArgumentException("не является каталогом: " + name);
        }

        for (VfsNode child : parent.children) {
            if (child.name.equals(name)) {
                return child;
            }
        }
        throw new IllegalArgumentException("путь не найден: " + fullPath);
    }

    /** Преобразует путь с учётом текущего каталога, "." и "..". */
    private String normalize(String currentDirectory, String path) {
        String full = path.startsWith("/")
                ? path : currentDirectory + "/" + path;
        Deque<String> parts = new ArrayDeque<>();

        for (String part : full.split("/")) {
            if (part.isEmpty() || part.equals(".")) {
                continue;
            }
            if (part.equals("..")) {
                if (!parts.isEmpty()) {
                    parts.removeLast();
                }
            } else {
                parts.addLast(part);
            }
        }
        return "/" + String.join("/", parts);
    }

    /** Найденный виртуальный объект и его абсолютный путь. */
    private record Location(String path, VfsNode node) {
    }
}
