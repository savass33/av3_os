package simulator.model;

import java.io.Serializable;

public abstract class FileSystemNode implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String name;
    protected Directory parent;

    public FileSystemNode(String name, Directory parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Directory getParent() {
        return parent;
    }

    public void setParent(Directory parent) {
        this.parent = parent;
    }

    public abstract boolean isDirectory();
}