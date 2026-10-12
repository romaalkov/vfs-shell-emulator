
import java.util.ArrayList;
import java.util.List;

/** Файл или каталог виртуальной файловой системы. */
public class VfsNode {
    String name;
    String type;
    String owner;
    String contentBase64;
    List<VfsNode> children;

    /** Создаёт пустую корневую директорию. */
    public static VfsNode defaultRoot() {
        VfsNode root = new VfsNode();
        root.name = "/";
        root.type = "directory";
        root.owner = "root";
        root.children = new ArrayList<>();
        return root;
    }
}
