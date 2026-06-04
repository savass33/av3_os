package simulator.model;

public class File extends FileSystemNode {
    private static final long serialVersionUID = 1L;
    private String content;

    public File(String name, Directory parent) {
        super(name, parent);
        this.content = "";
    }

    public File(String name, Directory parent, String content) {
        super(name, parent);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }
}