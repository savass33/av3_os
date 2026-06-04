# Simulador de Sistema de Arquivos

## Título
Simulador de Sistema de Arquivos em Java com Suporte a Journaling, Interface Gráfica e Persistência Isolada

## Resumo
Este trabalho propõe o desenvolvimento de um simulador para compreender e emular um sistema de arquivos completo. Através da implementação de uma estrutura orientada a objetos em Java, o sistema suporta criação, deleção, renomeação, manipulação interna de textos e listagem de diretórios e arquivos virtuais. O simulador incorpora o conceito de Journaling (Write-Ahead Logging) para registro contínuo das operações e garante persistência integral dos dados (salvando o estado entre execuções) em um ambiente 100% isolado, não expondo ou criando estruturas de pastas no sistema operacional hospedeiro. A interação é mediada por uma Interface Gráfica interativa de uso intuitivo.

## Introdução
O gerenciamento eficiente de arquivos é crucial para o funcionamento dos sistemas operacionais. Arquivos e diretórios compõem a visão do usuário sobre o armazenamento não volátil. Para isso, entender como é montado e organizado um sistema é a base para a compreensão dos sistemas operacionais. Sistemas de arquivos modernos implementam mecanismos de tolerância a falhas, como o Journaling, que previne corrupção de dados em caso de quedas de energia ou falhas do sistema, registrando a intenção de modificar o sistema antes da modificação em si.

## Objetivo
Desenvolver um simulador de sistema de arquivos em Java que implemente funcionalidades avançadas de manipulação de dados, com suporte robusto a Journaling para garantir a integridade. O simulador opera como um ecossistema encapsulado e persistente, permitindo operações como copiar, apagar, renomear, ler e escrever arquivos virtuais. O projeto entrega uma experiência de usuário (UX) completa com duas vertentes: Interface Gráfica (GUI) baseada em Swing e um Modo Shell Avançado por linha de comando.

## Metodologia
O simulador foi desenvolvido na linguagem de programação Java nativa. Toda a lógica baseia-se no padrão arquitetural Model-View-Controller (MVC). Os nós de arquivos ficam armazenados em árvores lógicas construídas na memória (RAM) utilizando Collections do Java (`HashMap` e `List`). Visando atender rigorosamente à regra de isolamento do sistema hospedeiro, todos os dados manipulados na aplicação são empacotados e mantidos fechados numa interface unificada visual (`VpeGUI`).

## Parte 1: Introdução ao Sistema de Arquivos com Journaling
**Descrição do sistema de arquivos:** Um sistema de arquivos é um conjunto de estruturas lógicas e rotinas que permitem ao sistema operacional controlar o acesso ao armazenamento, gerenciando como os dados são guardados, nomeados e recuperados.

**Journaling:** O Journaling é uma técnica para evitar a perda ou corrupção de dados. O tipo utilizado neste simulador é baseado no conceito de *write-ahead logging*, onde o sistema registra rigorosamente suas intenções (por exemplo, `CREATE_FILE root -> arquivo.txt`) na aba de log do *Journal* antes de invocar a rotina de alteração nas árvores de diretórios. Se o sistema falhasse, o log reteria as últimas diretrizes.

## Parte 2: Arquitetura do Simulador
**Estrutura de Dados em Memória:**
- `FileSystemNode`: Classe abstrata base assinada com `Serializable`, garantindo ramificações polimórficas padronizadas.
- `File`: Representação virtual de arquivos. Além de herdar o nome, contém um buffer interno do tipo `String` para leitura e injeção de conteúdos dinâmicos.
- `Directory`: Gerenciador hierárquico, contendo mapas para referenciar subdiretórios ou arquivos associados.

**Persistência Isolada (O grande diferencial):** 
Para que as alterações não se percam ao fechar a janela, toda operação que modifica o estado do sistema invoca o método `saveState()`. Esse algoritmo cria uma **serialização binária** convertendo a memória inteira em um arquivo protegido proprietário chamado **`system_data.vfs`** salvo na raiz do projeto. O professor ou avaliador não verá diretórios fictícios sujando a pasta local do seu Windows; tudo fica fechado e protegido no cofre `.vfs`. Ao reiniciar o programa, `loadState()` varre o arquivo restabelecendo árvores e a integridade completa do Log de Journaling.

## Parte 3: Implementação em Java
- **Classe `FileSystemSimulator` (O Núcleo):** Centraliza as funções de persistência, Journaling (como injeção no log), roteamento entre arquivos atuais, tratamento restritivo contra invasões de caracteres do SO (Directory Traversal como `../` ou `C:\`) e as famosas rotinas do CRUD de arquivos.
- **Classe `VpeGUI` (Ambiente de Processamento Virtual Isolado):** Interface Gráfica elaborada em Java Swing contendo visualizador de árvores hierárquicas, tabela exploradora em lista, caixas de diálogo modais para manipulação de arquivos de texto nativo, abas de relatórios de sistema e visualização in-interrupta do arquivo de log Journaling.
- **Classe `Shell`:** Implementação legada que provê um CLI (Command Line Interface) de retaguarda, caso a interação em prompt virtual seja requisitada.

## Parte 4: Instalação e Funcionamento

**Pré-requisitos:**
- Java Development Kit (JDK) versão 8 ou superior instalado. Nenhuma biblioteca externa necessária.

**Passo a passo de Execução:**
1. Clone ou baixe o repositório para o seu ambiente local.
2. Abra um terminal (CMD, PowerShell ou Bash) e navegue até a raiz do projeto (onde a pasta `src` está localizada).
3. Compile todos os códigos de Java executando o comando:
   ```bash
   javac -d out src/main/java/simulator/*/*.java
   ```
4. **INICIAR COM INTERFACE GRÁFICA (Opção Principal):**
   ```bash
   java -cp out simulator.ui.VpeGUI
   ```
   - Uma interface moderna e limpa será aberta. 
   - Utilize a barra de ferramentas no topo para invocar: Nova Coleção (Pasta), Novo Documento (Arquivo), Renomear, Excluir ou Editar Textos.
   - Analise os feedbacks diretamente nas telas de Journaling na base da aplicação.

5. **INICIAR COM MODO SHELL (Opção Avançada por CLI):**
   ```bash
   java -cp out simulator.ui.Shell
   ```
   - Digite comandos clássicos no terminal como `mkdir`, `touch`, `write`, `cat` e `journal`. Digite `help` para ver a lista de rotinas suportadas.

## Resultados Esperados
Espera-se que o simulador entregue uma solução coesa e à prova de falhas para estudos acadêmicos sobre Sistemas de Arquivos. Durante a avaliação, através do fluxo prático implementado (criar árvores intrincadas, visualizar atualizações vivas de Journaling, simular perdas e retornos de persistência criptografada), a arquitetura consolida de forma palpável a teoria da construção de elementos base de um Sistema Operacional sem necessitar de partições físicas perigosas no HD do usuário.

## Repositório GitHub
**Link:** [INSERIR LINK DO GITHUB AQUI] *(Colem a URL real do projeto aqui antes de salvar em PDF)*
# av3_os
