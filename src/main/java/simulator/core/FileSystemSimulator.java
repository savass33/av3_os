package simulator.core;

import java.io.*;
import simulator.model.Directory;
import simulator.model.File;
import simulator.model.FileSystemNode;
import simulator.model.Journal;

public class FileSystemSimulator {
    private Directory root;
    private Directory currentDirectory;
    private Journal journal;
    private static final String FS_DATA_FILE = "system_data.vfs";

    public FileSystemSimulator() {
        if (!loadState()) {
            this.root = new Directory("root", null);
            this.currentDirectory = root;
            this.journal = new Journal();
            journal.recordOperation("INIT_FS");
            saveState();
        } else {
            System.out.println("Estado do sistema restaurado com sucesso a partir do arquivo persistente proprietário.");
        }
    }

    private void saveState() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FS_DATA_FILE))) {
            oos.writeObject(root);
            oos.writeObject(journal);
        } catch (IOException e) {
            System.out.println("Erro Crítico: Falha ao persistir dados no armazenamento isolado. " + e.getMessage());
        }
    }

    private boolean loadState() {
        java.io.File file = new java.io.File(FS_DATA_FILE);
        if (!file.exists()) return false;
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            this.root = (Directory) ois.readObject();
            this.journal = (Journal) ois.readObject();
            this.currentDirectory = this.root; // Ao religar, reseta o ponteiro de navegação para a raiz por segurança
            return true;
        } catch (Exception e) {
            System.out.println("Aviso: Arquivo de persistência não encontrado ou formato incompatível. Iniciando sistema limpo.");
            return false;
        }
    }

    public Directory getRoot() { return root; }
    public Directory getCurrentDirectory() { return currentDirectory; }
    public void setCurrentDirectory(Directory currentDirectory) { this.currentDirectory = currentDirectory; }
    public Journal getJournal() { return journal; }

    // SANITIZAÇÃO E ENCAPSULAMENTO:
    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        if (name.contains("/") || name.contains("\\") || name.equals(".") || name.equals("..")) {
            return false;
        }
        return true;
    }

    public void createDirectory(String name) {
        if (!isValidName(name)) {
            System.out.println("Erro de Segurança: Caracteres inválidos ou caminhos de SO detectados no nome.");
            return;
        }
        journal.recordOperation("CREATE_DIR " + currentDirectory.getName() + " -> " + name);
        if (currentDirectory.getChild(name) != null) {
            System.out.println("Erro: Já existe um recurso interno com o nome '" + name + "'.");
            return;
        }
        Directory newDir = new Directory(name, currentDirectory);
        currentDirectory.addChild(newDir);
        saveState();
        System.out.println("Diretório interno '" + name + "' instanciado com persistência.");
    }

    public void deleteDirectory(String name) {
        if (!isValidName(name)) {
            System.out.println("Erro de Segurança: Nome inválido.");
            return;
        }
        journal.recordOperation("DELETE_DIR " + currentDirectory.getName() + " -> " + name);
        FileSystemNode node = currentDirectory.getChild(name);
        if (node == null || !node.isDirectory()) {
            System.out.println("Erro: Diretório '" + name + "' não encontrado no contexto atual.");
            return;
        }
        currentDirectory.removeChild(name);
        saveState();
        System.out.println("Diretório '" + name + "' removido.");
    }

    public void renameDirectory(String oldName, String newName) {
        if (!isValidName(oldName) || !isValidName(newName)) {
            System.out.println("Erro de Segurança: Caracteres inválidos detectados.");
            return;
        }
        journal.recordOperation("RENAME_DIR " + oldName + " TO " + newName);
        FileSystemNode node = currentDirectory.getChild(oldName);
        if (node == null || !node.isDirectory()) {
            System.out.println("Erro: Diretório '" + oldName + "' não encontrado.");
            return;
        }
        if (currentDirectory.getChild(newName) != null) {
            System.out.println("Erro: Já existe um recurso com o nome '" + newName + "'.");
            return;
        }
        currentDirectory.removeChild(oldName);
        node.setName(newName);
        currentDirectory.addChild(node);
        saveState();
        System.out.println("Recurso renomeado com sucesso no buffer interno.");
    }

    public void createFile(String name) {
        if (!isValidName(name)) {
            System.out.println("Erro de Segurança: Tentativa de travessia de diretório bloqueada.");
            return;
        }
        journal.recordOperation("CREATE_FILE " + currentDirectory.getName() + " -> " + name);
        if (currentDirectory.getChild(name) != null) {
            System.out.println("Erro: Já existe um recurso com o nome '" + name + "'.");
            return;
        }
        File newFile = new File(name, currentDirectory, "");
        currentDirectory.addChild(newFile);
        saveState();
        System.out.println("Documento '" + name + "' persistido no repositório virtual.");
    }

    public void deleteFile(String name) {
        if (!isValidName(name)) return;
        journal.recordOperation("DELETE_FILE " + currentDirectory.getName() + " -> " + name);
        FileSystemNode node = currentDirectory.getChild(name);
        if (node == null || node.isDirectory()) {
            System.out.println("Erro: Documento '" + name + "' não encontrado.");
            return;
        }
        currentDirectory.removeChild(name);
        saveState();
        System.out.println("Documento '" + name + "' destruído.");
    }

    public void renameFile(String oldName, String newName) {
        if (!isValidName(oldName) || !isValidName(newName)) return;
        journal.recordOperation("RENAME_FILE " + oldName + " TO " + newName);
        FileSystemNode node = currentDirectory.getChild(oldName);
        if (node == null || node.isDirectory()) {
            System.out.println("Erro: Documento '" + oldName + "' não encontrado.");
            return;
        }
        if (currentDirectory.getChild(newName) != null) {
            System.out.println("Erro: Conflito de nomenclatura interno.");
            return;
        }
        currentDirectory.removeChild(oldName);
        node.setName(newName);
        currentDirectory.addChild(node);
        saveState();
        System.out.println("Documento renomeado no repositório.");
    }

    public void copyFile(String sourceName, String destName) {
        if (!isValidName(sourceName) || !isValidName(destName)) return;
        journal.recordOperation("COPY_FILE " + sourceName + " TO " + destName);
        FileSystemNode node = currentDirectory.getChild(sourceName);
        if (node == null || node.isDirectory()) {
            System.out.println("Erro: Documento de origem não encontrado.");
            return;
        }
        if (currentDirectory.getChild(destName) != null) {
            System.out.println("Erro: Documento de destino já existe.");
            return;
        }
        File sourceFile = (File) node;
        File newFile = new File(destName, currentDirectory, sourceFile.getContent());
        currentDirectory.addChild(newFile);
        saveState();
        System.out.println("Documento clonado internamente para '" + destName + "'.");
    }

    public void writeFileContent(String name, String content) {
        if (!isValidName(name)) return;
        journal.recordOperation("WRITE_DATA " + name);
        FileSystemNode node = currentDirectory.getChild(name);
        if (node == null || node.isDirectory()) {
            System.out.println("Erro: Documento não encontrado para escrita.");
            return;
        }
        ((File) node).setContent(content);
        saveState();
        System.out.println("Dados processados e salvos no documento '" + name + "'.");
    }

    public void readFileContent(String name) {
        if (!isValidName(name)) return;
        FileSystemNode node = currentDirectory.getChild(name);
        if (node == null || node.isDirectory()) {
            System.out.println("Erro: Documento não encontrado para leitura.");
            return;
        }
        System.out.println("--- Visualizador Interno (" + name + ") ---");
        System.out.println(((File) node).getContent());
        System.out.println("-----------------------------------");
    }

    public void listFiles() {
        System.out.println("Recursos persistidos [" + currentDirectory.getName() + "]:");
        if (currentDirectory.getChildren().isEmpty()) {
             System.out.println(" (Repositório vazio)");
             return;
        }
        for (FileSystemNode node : currentDirectory.getChildren().values()) {
            String type = node.isDirectory() ? "[Coleção]" : "[Doc Virtual]";
            System.out.println(type + " " + node.getName());
        }
    }
    
    public void printJournal() {
        journal.printLog();
    }
}