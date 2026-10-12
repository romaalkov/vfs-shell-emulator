
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.HashSet;
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

    /** Создаёт VFS по умолчанию без физического файла. */
    public static VirtualFileSystem empty() {
        return new VirtualFileSystem(null, VfsNode.defaultRoot());
    }

    /** Загружает JSON в память и проверяет структуру. */
    public static VirtualFileSystem load(Path path) throws IOException {
        if (path == null) {
            return empty();
        }

        try (Reader reader = Files.newBufferedReader(
                path, StandardCharsets.UTF_8)) {
            VfsNode root = GSON.fromJson(reader, VfsNode.class);
            validate(root, true);
            return new VirtualFileSystem(path, root);
        } catch (JsonParseException | IllegalArgumentException e) {
            throw new IOException("Неверный формат VFS: "
                    + e.getMessage(), e);
        }
    }

    /** Проверяет объект файловой системы. */
    private static void validate(VfsNode node, boolean isRoot) {
        if (node == null || node.name == null
                || node.type == null || node.owner == null
                || node.owner.isBlank()) {
            throw new IllegalArgumentException("Некорректный объект VFS");
        }

        if (isRoot && !node.name.equals("/")) {
            throw new IllegalArgumentException("Корень должен иметь имя /");
        }

        if (!isRoot && (node.name.isBlank()
                || node.name.contains("/")
                || node.name.equals(".") || node.name.equals(".."))) {
            throw new IllegalArgumentException("Некорректное имя файла");
        }

        if (node.type.equals("directory")) {
            validateDirectory(node);
        } else if (node.type.equals("file") && !isRoot) {
            validateFile(node);
        } else {
            throw new IllegalArgumentException("Неизвестный тип объекта");
        }
    }

    /** Проверяет содержимое каталога. */
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

    /** Проверяет содержимое файла и данные Base64. */
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

    /** Подсчитывает объекты заданного типа. */
    private int count(VfsNode node, String type) {
        int result = node.type.equals(type) ? 1 : 0;

        if (node.children != null) {
            for (VfsNode child : node.children) {
                result += count(child, type);
            }
        }

        return result;
    }

    public int directoryCount() {
        return count(root, "directory");
    }

    public int fileCount() {
        return count(root, "file");
    }

    /** Возвращает краткую информацию о загруженной VFS. */
    public String describe() {
        return "VFS: каталогов " + directoryCount()
                + ", файлов " + fileCount();
    }

    /**
     * Сбрасывает VFS к пустому корневому каталогу.
     * Физический JSON перезаписывается только этой командой.
     */
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
}
