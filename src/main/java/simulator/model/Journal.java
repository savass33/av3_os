package simulator.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Journal implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<String> log;

    public Journal() {
        this.log = new ArrayList<>();
    }

    public void recordOperation(String operation) {
        log.add(operation);
        // System.out.println("[Journal] Log registrado: " + operation); // Debug removido
    }

    public List<String> getLog() {
        return log;
    }

    public void printLog() {
        System.out.println("--- Log do Journaling ---");
        for (String entry : log) {
            System.out.println(entry);
        }
        System.out.println("-------------------------");
    }
}