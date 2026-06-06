package simulator.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import simulator.core.FileSystemSimulator;
import simulator.model.Directory;
import simulator.model.File;
import simulator.model.FileSystemNode;

public class VpeGUI extends JFrame {

    private FileSystemSimulator fs;
    private JTree treeNavigation;
    private DefaultTreeModel treeModel;
    private JTable tableFiles;
    private DefaultTableModel tableModel;
    private JTextArea textJournal;
    private JTextArea textConsole;
    private JLabel labelStatus;

    public VpeGUI() {
        fs = new FileSystemSimulator();

        setTitle("Ambiente de Processamento Virtual Isolado (VPE) - Simulador de Arquivos");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initComponents();
        setupSystemOutRedirect();
        refreshAll();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // --- BARRA SUPERIOR (TOOLBAR) ---
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        JButton btnUp = new JButton("Subir Diretório");
        JButton btnNewDir = new JButton("Nova Coleção (Dir)");
        JButton btnNewFile = new JButton("Novo Documento");
        JButton btnDelete = new JButton("Excluir");
        JButton btnRename = new JButton("Renomear");
        JButton btnCopy = new JButton("Copiar");
        JButton btnEdit = new JButton("Editar Texto (Write/Cat)");
        JButton btnRefresh = new JButton("Atualizar");

        toolBar.add(btnUp);
        toolBar.addSeparator();
        toolBar.add(btnNewDir);
        toolBar.add(btnNewFile);
        toolBar.addSeparator();
        toolBar.add(btnDelete);
        toolBar.add(btnRename);
        toolBar.add(btnCopy);
        toolBar.addSeparator();
        toolBar.add(btnEdit);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(btnRefresh);

        add(toolBar, BorderLayout.NORTH);

        // --- PAINEL ESQUERDO (ÁRVORE DE NAVEGAÇÃO) ---
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(fs.getRoot().getName());
        treeModel = new DefaultTreeModel(rootNode);
        treeNavigation = new JTree(treeModel);
        treeNavigation.setShowsRootHandles(true);
        treeNavigation.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) treeNavigation.getLastSelectedPathComponent();
            if (selectedNode != null) {
                // Find the directory object by traversing from root
                Object[] path = selectedNode.getUserObjectPath();
                Directory dir = fs.getRoot();
                for (int i = 1; i < path.length; i++) {
                    FileSystemNode child = dir.getChild(path[i].toString());
                    if (child != null && child.isDirectory()) {
                        dir = (Directory) child;
                    }
                }
                fs.setCurrentDirectory(dir);
                refreshFilesTable();
                updateStatus("Diretório atual: " + buildPath(dir));
            }
        });
        JScrollPane scrollTree = new JScrollPane(treeNavigation);
        scrollTree.setPreferredSize(new Dimension(250, 0));

        // --- ÁREA PRINCIPAL (LISTA DE ARQUIVOS) ---
        String[] columnNames = {"Nome", "Tipo", "Tamanho/Conteúdo"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableFiles = new JTable(tableModel);
        tableFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableFiles.setRowHeight(25);
        tableFiles.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableFiles.getSelectedRow();
                    if (row >= 0) {
                        String name = (String) tableModel.getValueAt(row, 0);
                        String type = (String) tableModel.getValueAt(row, 1);
                        if (type.equals("[Coleção]")) {
                            Directory child = (Directory) fs.getCurrentDirectory().getChild(name);
                            if (child != null) {
                                fs.setCurrentDirectory(child);
                                refreshAll();
                            }
                        } else {
                            openEditor(name);
                        }
                    }
                }
            }
        });
        JScrollPane scrollTable = new JScrollPane(tableFiles);

        // --- PAINEL INFERIOR (JOURNAL E CONSOLE) ---
        JTabbedPane bottomTabs = new JTabbedPane();
        
        textJournal = new JTextArea();
        textJournal.setEditable(false);
        textJournal.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textJournal.setBackground(new Color(245, 245, 245));
        JScrollPane scrollJournal = new JScrollPane(textJournal);
        
        textConsole = new JTextArea();
        textConsole.setEditable(false);
        textConsole.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textConsole.setBackground(Color.BLACK);
        textConsole.setForeground(Color.GREEN);
        JScrollPane scrollConsole = new JScrollPane(textConsole);

        bottomTabs.addTab("Journaling Log", scrollJournal);
        bottomTabs.addTab("Console do Sistema", scrollConsole);
        bottomTabs.setPreferredSize(new Dimension(0, 200));

        // Layout Splits
        JSplitPane splitRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTable, bottomTabs);
        splitRight.setResizeWeight(0.7);

        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTree, splitRight);
        add(splitMain, BorderLayout.CENTER);

        // --- BARRA DE STATUS ---
        JPanel statusPanel = new JJPanel(new BorderLayout());
        labelStatus = new JLabel(" Sistema Inicializado. Tudo pronto.");
        labelStatus.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        statusPanel.add(labelStatus, BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);

        // --- AÇÕES DOS BOTÕES ---
        btnUp.addActionListener(e -> {
            Directory parent = fs.getCurrentDirectory().getParent();
            if (parent != null) {
                fs.setCurrentDirectory(parent);
                refreshAll();
            } else {
                showError("Já está no diretório raiz.");
            }
        });

        btnNewDir.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Nome da Nova Coleção (Diretório):", "Nova Coleção", JOptionPane.PLAIN_MESSAGE);
            if (name != null && !name.trim().isEmpty()) {
                fs.createDirectory(name.trim());
                refreshAll();
            }
        });

        btnNewFile.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Nome do Novo Documento:", "Novo Documento", JOptionPane.PLAIN_MESSAGE);
            if (name != null && !name.trim().isEmpty()) {
                fs.createFile(name.trim());
                refreshAll();
            }
        });

        btnDelete.addActionListener(e -> {
            
            FileSystemNode node = getSelectedNode();

            if (node == null)
                return;

            if (node == fs.getRoot()) {
                showError("Não é possível excluir o diretório raiz.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Tem certeza que deseja excluir '" + node.getName() + "'?",
                    "Confirmar Exclusão",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {

                if (node.isDirectory()) {

                    fs.setCurrentDirectory(node.getParent());

                    fs.deleteDirectory(node.getName());

                } else {

                    fs.deleteFile(node.getName());
                }

                refreshAll();
            }
        });

        btnRename.addActionListener(e -> {

            FileSystemNode node = getSelectedNode();

            if (node == null)
                return;

            if (node == fs.getRoot()) {
                showError("Não é possível renomear o diretório raiz.");
                return;
            }

            String newName = JOptionPane.showInputDialog(
                    this,
                    "Novo nome:",
                    node.getName());

            if (newName == null || newName.trim().isEmpty())
                return;

            if (node.isDirectory()) {

                Directory parent = node.getParent();

                fs.setCurrentDirectory(parent);

                fs.renameDirectory(
                        node.getName(),
                        newName.trim());

            } else {

                fs.renameFile(
                        node.getName(),
                        newName.trim());
            }

            refreshAll();
        });

        btnCopy.addActionListener(e -> {
            String sourceName = getSelectedName();
            if (sourceName != null) {
                FileSystemNode node = fs.getCurrentDirectory().getChild(sourceName);
                if (node.isDirectory()) {
                    showError("Não é possível copiar coleções (diretórios) na implementação atual.");
                } else {
                    String destName = JOptionPane.showInputDialog(this, "Nome do documento de destino:", sourceName + "_copia");
                    if (destName != null && !destName.trim().isEmpty()) {
                        fs.copyFile(sourceName, destName.trim());
                        refreshAll();
                    }
                }
            }
        });

        btnEdit.addActionListener(e -> {
            String name = getSelectedName();
            if (name != null) {
                FileSystemNode node = fs.getCurrentDirectory().getChild(name);
                if (node != null && !node.isDirectory()) {
                    openEditor(name);
                } else {
                    showError("Selecione um Documento Virtual (não uma Coleção) para editar.");
                }
            }
        });

        btnRefresh.addActionListener(e -> refreshAll());
    }

    private void openEditor(String fileName) {
        FileSystemNode node = fs.getCurrentDirectory().getChild(fileName);
        if (node != null && !node.isDirectory()) {
            File file = (File) node;
            JTextArea textArea = new JTextArea(10, 40);
            textArea.setText(file.getContent());
            JScrollPane scrollPane = new JScrollPane(textArea);
            int result = JOptionPane.showConfirmDialog(this, scrollPane, "Editar Documento: " + fileName, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                fs.writeFileContent(fileName, textArea.getText());
                refreshAll();
            }
        }
    }

    private FileSystemNode getSelectedNode() {

        // Primeiro tenta a tabela
        int row = tableFiles.getSelectedRow();

        if (row >= 0) {
            String name = (String) tableModel.getValueAt(row, 0);
            return fs.getCurrentDirectory().getChild(name);
        }

        // Se não houver item na tabela, tenta a árvore
        DefaultMutableTreeNode treeNode =
                (DefaultMutableTreeNode) treeNavigation.getLastSelectedPathComponent();

        if (treeNode != null) {

            Object[] path = treeNode.getUserObjectPath();

            Directory dir = fs.getRoot();

            for (int i = 1; i < path.length; i++) {

                FileSystemNode child = dir.getChild(path[i].toString());

                if (child != null && child.isDirectory()) {
                    dir = (Directory) child;
                }
            }

            return dir;
        }

        showError("Selecione um item.");
        return null;
    }

    private String getSelectedName() {
        int row = tableFiles.getSelectedRow();
        if (row >= 0) {
            return (String) tableModel.getValueAt(row, 0);
        }
        showError("Selecione um item na lista.");
        return null;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
    
    // Extensão personalizada do JOptionPane para evitar que crie classes não encontradas
    private static class JJPanel extends JPanel {
        public JJPanel(LayoutManager layout) { super(layout); }
    }

    private void setupSystemOutRedirect() {
        // Redireciona System.out para capturarmos os retornos dos comandos e exibir no console/mensagens de erro
        PrintStream originalOut = System.out;
        PrintStream interceptor = new PrintStream(new ByteArrayOutputStream()) {
            @Override
            public void println(String x) {
                // originalOut.println(x); // Removido para não sujar o terminal em background
                appendConsole(x);
                if (x != null && x.startsWith("Erro")) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(VpeGUI.this, x, "Mensagem do Sistema", JOptionPane.WARNING_MESSAGE));
                }
            }
            @Override
            public void print(String x) {
                // originalOut.print(x); // Removido para não sujar o terminal em background
                appendConsole(x);
            }
        };
        System.setOut(interceptor);
    }

    private void appendConsole(String text) {
        SwingUtilities.invokeLater(() -> {
            textConsole.append(text + (text.endsWith("\n") ? "" : "\n"));
            textConsole.setCaretPosition(textConsole.getDocument().getLength());
        });
    }

    private void refreshAll() {
        refreshFilesTable();
        refreshTree();
        refreshJournal();
        updateStatus("Pronto. Diretório atual: " + buildPath(fs.getCurrentDirectory()));
    }

    private void refreshFilesTable() {
        tableModel.setRowCount(0);
        Map<String, FileSystemNode> children = fs.getCurrentDirectory().getChildren();
        for (FileSystemNode node : children.values()) {
            String type = node.isDirectory() ? "[Coleção]" : "[Doc Virtual]";
            String info = node.isDirectory() ? ((Directory)node).getChildren().size() + " itens" : ((File)node).getContent().length() + " bytes";
            tableModel.addRow(new Object[]{node.getName(), type, info});
        }
    }

    private void refreshTree() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(fs.getRoot().getName());
        buildTree(fs.getRoot(), rootNode);
        treeModel.setRoot(rootNode);
        treeModel.reload();
        // Expande tudo (simples para fins didáticos)
        for (int i = 0; i < treeNavigation.getRowCount(); i++) {
            treeNavigation.expandRow(i);
        }
    }

    private void buildTree(Directory dir, DefaultMutableTreeNode node) {
        for (FileSystemNode child : dir.getChildren().values()) {
            if (child.isDirectory()) {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child.getName());
                node.add(childNode);
                buildTree((Directory) child, childNode);
            }
        }
    }

    private void refreshJournal() {
        textJournal.setText("");
        List<String> logs = fs.getJournal().getLog();
        for (String log : logs) {
            textJournal.append(log + "\n");
        }
    }

    private void updateStatus(String status) {
        labelStatus.setText(" " + status);
    }

    private String buildPath(Directory dir) {
        if (dir.getParent() == null) return "/" + dir.getName();
        return buildPath(dir.getParent()) + "/" + dir.getName();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VpeGUI().setVisible(true);
        });
    }
}