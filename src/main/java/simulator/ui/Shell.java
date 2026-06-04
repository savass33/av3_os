package simulator.ui;

import java.util.Scanner;
import simulator.core.FileSystemSimulator;

public class Shell {
    public static void main(String[] args) {
        FileSystemSimulator fs = new FileSystemSimulator();
        Scanner scanner = new Scanner(System.in);
        System.out.println("=====================================================");
        System.out.println("  Ambiente de Processamento Virtual Isolado (VPE)");
        System.out.println("  Todos os dados são geridos em memória e isolados.");
        System.out.println("=====================================================");
        System.out.println("Digite 'help' para acessar o menu da aplicação.");

        while (true) {
            System.out.print("vpe> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            String[] tokens = input.split("\\s+", 3); // O limite de 3 ajuda no comando de write
            String command = tokens[0].toLowerCase();

            try {
                switch (command) {
                    case "mkdir":
                        if (tokens.length < 2) System.out.println("Uso interno: mkdir <nome_da_colecao>");
                        else fs.createDirectory(tokens[1]);
                        break;
                    case "rmdir":
                        if (tokens.length < 2) System.out.println("Uso interno: rmdir <nome_da_colecao>");
                        else fs.deleteDirectory(tokens[1]);
                        break;
                    case "rendir":
                        if (tokens.length < 3) System.out.println("Uso interno: rendir <antigo> <novo>");
                        else fs.renameDirectory(tokens[1], tokens[2]);
                        break;
                    case "touch":
                        if (tokens.length < 2) System.out.println("Uso interno: touch <nome_do_documento>");
                        else fs.createFile(tokens[1]);
                        break;
                    case "write":
                        if (tokens.length < 3) System.out.println("Uso interno: write <documento> <conteúdo em texto...>");
                        else fs.writeFileContent(tokens[1], tokens[2]);
                        break;
                    case "cat":
                        if (tokens.length < 2) System.out.println("Uso interno: cat <nome_do_documento>");
                        else fs.readFileContent(tokens[1]);
                        break;
                    case "rm":
                        if (tokens.length < 2) System.out.println("Uso interno: rm <nome_do_documento>");
                        else fs.deleteFile(tokens[1]);
                        break;
                    case "ren":
                        if (tokens.length < 3) System.out.println("Uso interno: ren <antigo> <novo>");
                        else fs.renameFile(tokens[1], tokens[2]);
                        break;
                    case "cp":
                        if (tokens.length < 3) System.out.println("Uso interno: cp <origem> <destino>");
                        else fs.copyFile(tokens[1], tokens[2]);
                        break;
                    case "ls":
                        fs.listFiles();
                        break;
                    case "journal":
                        fs.printJournal();
                        break;
                    case "help":
                        printHelp();
                        break;
                    case "exit":
                        System.out.println("Encerrando Ambiente Virtual. Dados isolados persistidos com sucesso.");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Operação inválida no ambiente virtual.");
                }
            } catch (Exception e) {
                System.out.println("Erro de processamento interno: " + e.getMessage());
            }
        }
    }

    private static void printHelp() {
        System.out.println("Gerenciamento do Repositório Virtual:");
        System.out.println("  mkdir <nome>         - Instanciar nova coleção (diretório)");
        System.out.println("  rmdir <nome>         - Destruir coleção");
        System.out.println("  rendir <antigo> <novo> - Renomear coleção");
        System.out.println("  touch <nome>         - Criar documento virtual vazio");
        System.out.println("  write <nome> <texto> - Injetar conteúdo no documento (processamento interno)");
        System.out.println("  cat <nome>           - Visualizar conteúdo do documento virtual");
        System.out.println("  rm <nome>            - Destruir documento virtual");
        System.out.println("  ren <antigo> <novo>  - Renomear documento virtual");
        System.out.println("  cp <origem> <destino>- Clonar documento em memória");
        System.out.println("  ls                   - Listar recursos do repositório");
        System.out.println("  journal              - Visualizar auditoria interna (Log)");
        System.out.println("  exit                 - Encerrar sessão");
    }
}