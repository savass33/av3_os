package simulator.model;

import java.util.HashMap;
import java.util.Map;

public class Directory extends FileSystemNode {
    private static final long serialVersionUID = 1L;
    private Map<String, FileSystemNode> children;

    public Directory(String name, Directory parent) {
        super(name, parent);
        this.children = new HashMap<>();
    }

    public void addChild(FileSystemNode node) {
        children.put(node.getName(), node);
        node.setParent(this);
    }

    public void removeChild(String name) {
        children.remove(name);
    }

    public FileSystemNode getChild(String name) {
        return children.get(name);
    }

    public Map<String, FileSystemNode> getChildren() {
        return children;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }
}