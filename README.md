# Universidade de Fortaleza (UNIFOR)

**Disciplina:** Projeto de Sistemas Operacionais  
**Turma:** T303-26  
**Horário:** T35EF  
**Professor:** Izequiel  

## Equipe
* Savas Constantin Petalas Neto — Matrícula 2410432
* Levi de Pontes Lima Santos — Matrícula 2416516

---

# Simulador de Sistema de Arquivos com Journaling

## 1. Resumo do Projeto

Este projeto consiste no desenvolvimento de um Simulador de Sistema de Arquivos virtual, programado integralmente na linguagem Java. O simulador emula o comportamento das estruturas de armazenamento de um Sistema Operacional real, fornecendo abstrações lógicas de diretórios e arquivos. Além do provimento de rotinas fundamentais de manipulação de dados, a arquitetura incorpora e demonstra empiricamente a técnica de **Journaling**, que é vital para a garantia da integridade estrutural e proteção contra perda de dados. 

Com um foco acadêmico em robustez, o núcleo lógico foi isolado e encapsulado, persistindo os dados de maneira invisível ao sistema operacional hospedeiro. A interação com o usuário foi consolidada em uma moderna Interface Gráfica (GUI) baseada no padrão MVC, entregando uma ferramenta didática, profissional e de fácil compreensão para o estudo prático de Sistemas Operacionais.

## 2. Introdução

A responsabilidade de gerenciar dados de forma não volátil recai sobre o componente do sistema operacional conhecido como **Sistema de Arquivos** (File System). Ele atua como uma camada de abstração essencial que permite que sequências magnéticas em discos rígidos ou memórias flash em SSDs sejam interpretadas e exibidas ao usuário como "pastas", "fotos" e "documentos de texto".

A estruturação dessa organização lógica dita as regras de como as informações são guardadas, nomeadas e recuperadas eficientemente. Para a Ciência da Computação, compreender essa arquitetura vai além de saber utilizar o computador; exige o entendimento das complexas engrenagens algorítmicas (listas, mapas e árvores de memória) que regem tais alocações. 

Ademais, sistemas modernos precisam ser resilientes. Devido à suscetibilidade dos hardwares a quedas de energia e falhas de software (crashes), técnicas preditivas como o **Journaling** tornaram-se um paradigma indispensável para evitar que as árvores de arquivos se corrompam na iminência de uma falha global.

## 3. Objetivos do Trabalho

### Objetivo Geral
Desenvolver um simulador funcional de sistema de arquivos em Java que implemente operações primárias de manipulação de dados em conjunto com o suporte ao protocolo de Journaling, garantindo total isolamento do sistema operacional hospedeiro.

### Objetivos Específicos
* Modelar entidades lógicas utilizando a Orientação a Objetos.
* Implementar mecanismos para copiar, apagar, renomear e listar arquivos/diretórios por chamadas de métodos.
* Criar um módulo de registro e rastreabilidade (Journal) para proteger as operações.
* Construir uma Interface Gráfica intuitiva e de fácil manuseio para apresentações acadêmicas.
* Aplicar mecanismos de persistência que garantam a integridade dos dados simulados entre diferentes execuções.

## 4. Conceitos Teóricos

### Sistema de Arquivos
* **Conceito:** É um conjunto de estruturas lógicas e rotinas de software gerenciadas pelo kernel que controlam como os dados são armazenados e recuperados em um meio de armazenamento.
* **Estrutura:** Tradicionalmente estruturado em metadados (informações sobre o arquivo, como nome e permissões) e os dados brutos em si (a carga útil).
* **Funcionamento e Organização Hierárquica:** Utiliza uma arquitetura em Árvore N-ária. O sistema se inicia em um nó primordial (chamado *Root* ou "Diretório Raiz") que pode bifurcar infinitamente em novos nós, os quais podem ser documentos-finais (arquivos) ou novas subdivisões (subdiretórios).

### Journaling
* **Conceito:** É um diário de intenções do sistema operacional. Consiste em uma área ou arquivo seguro onde o SO registra detalhadamente qual alteração ele pretende fazer no disco antes de efetivamente realizá-la.
* **Objetivo:** Impedir que o sistema de arquivos sofra corrupção lógica de ponteiros e índices.
* **Benefícios e Recuperação após falhas:** Caso o computador desligue repentinamente durante a movimentação de um arquivo, no momento em que for religado, o SO consultará o Journal para saber o que foi interrompido. Ele poderá então desfazer a operação pela metade (*rollback*) ou finalizá-la, garantindo a integridade dos dados.

### Tipos de Journaling
* **Write-Ahead Logging (WAL):** Todas as intenções de modificação devem ser rigorosamente registradas e salvas no log antes de a aplicação tocar nas árvores de dados originais. (Modelo principal implementado neste projeto).
* **Metadata Journaling:** Registra no diário exclusivamente as alterações de metadados estruturais (nomes, caminhos). Mantém alta performance, mas não protege o conteúdo interno do arquivo em si caso ocorra uma falha durante a gravação de dados extensos.
* **Full Data Journaling:** O modelo mais seguro e mais lento. Além dos metadados, o arquivo inteiro é copiado para o log antes da operação ser aplicada. Ocupa muito processamento, mas anula completamente o risco de corrupção do conteúdo.
* **Log Structured File System (LFS):** Não possui um Journal separado. O próprio disco inteiro atua como um log sequencial. Novas alterações são adicionadas ao final do registro em vez de sobrescrever o bloco original do disco.

## 5. Arquitetura da Solução

O simulador foi desenhado em uma arquitetura limpa inspirada no padrão **MVC (Model-View-Controller)**, com forte divisão de pacotes:

* **Estrutura Geral e Fluxo:** A camada `ui` (View) captura os cliques do usuário e invoca os métodos públicos da camada `core` (Controller). O `core` aplica regras rigorosas de segurança, registra a ação no Journal, manipula as árvores na camada `model` e, finalmente, realiza o *dump* (salvamento) completo da memória para o disco.

### FileSystemSimulator (O Controlador Core)
A classe central de operações. Possui a responsabilidade de manter os ponteiros de qual é o diretório atual, aplicar validações contra caracteres ilícitos (`isValidName`) para impedir invasão de caminhos do Windows (`C:\` ou `../`), e comandar as serializações automáticas (`saveState()` e `loadState()`).

### File
Herdeira de `FileSystemNode`. Representa a abstração lógica de um arquivo. Diferentemente de arquivos convencionais vazios, ela possui um buffer interno mutável (`content`) em formato de String que nos permite ler e escrever textos diretamente na memória do simulador.

### Directory
Entidade gestora. Adota uma estrutura orientada a recursão, contendo um mapa interno para guardar referências aos seus próprios filhos (sejam eles outros `Directory` ou instâncias de `File`).

### Journal
Mecanismo de log contínuo da aplicação. Mantém um registro assíncrono em lista (FIFO) de toda ação submetida ao simulador, garantindo rastreabilidade fundamental das operações.

### Interface Gráfica
Construída com a biblioteca **Java Swing**. Garante altíssima responsividade sem depender de bibliotecas pesadas de terceiros (o que facilita o uso em ambiente universitário). Implementa uma Árvore Hierárquica interativa à esquerda (`JTree`), um Explorador detalhado à direita (`JTable`) e um painel imutável inferior em `JTextArea` para o acompanhamento ao vivo do Journaling.

## 6. Estruturas de Dados Utilizadas

Para modelar as complexidades de alocação de um sistema de arquivos, utilizamos as seguintes estruturas:

* **HashMap (`Map<String, FileSystemNode>`):** Utilizado dentro da classe `Directory` para armazenar os filhos. Escolhido por sua complexidade de busca `O(1)` na média. Essa estrutura permite localizar arquivos e pastas de forma instantânea sem precisar realizar varreduras iterativas lentas, simulando a performance de alocação real.
* **ArrayList (`List<String>`):** Empregado na classe `Journal`. Listas dinâmicas são perfeitas para logs que crescem sequencialmente (FIFO - *First In, First Out*), mantendo a ordem histórica dos timestamps inalterada.
* **Árvores N-árias (Logicamente estruturada):** A relação de `parent` e `children` compõe nativamente a árvore hierárquica base da navegação do sistema.

## 7. Funcionalidades Implementadas

### Criar Diretórios
* **Objetivo:** Estabelecer novas partições lógicas de organização.
* **Funcionamento e Fluxo:** O nome cruza a validação de segurança -> Registra `CREATE_DIR` no Journal -> Instancia um objeto `Directory` em memória -> Insere a referência no HashMap do diretório pai -> Persiste o estado do sistema.
* **Benefícios:** Permite a ramificação livre da árvore de arquivos.

### Remover Diretórios
* **Objetivo:** Excluir pastas do sistema simulado.
* **Funcionamento e Fluxo:** O sistema busca o nó pelo nome. Após registrar `DELETE_DIR`, a referência é removida do HashMap, e o Garbage Collector do Java se encarrega de limpar os objetos que ficaram sem acesso.
* **Benefícios:** Libera recursos logicamente de forma rápida e segura.

### Renomear Diretórios
* **Objetivo:** Alterar a identificação de uma coleção sem perder seu conteúdo.
* **Funcionamento e Fluxo:** Verifica se o novo nome já está em uso na coleção. Registra no Journal. Remove a referência velha do mapa, altera a propriedade `name` e a reinsere sob a nova chave.
* **Benefícios:** Protege contra colisões de identificação na mesma hierarquia.

### Copiar Arquivos
* **Objetivo:** Clonar as instâncias lógicas e o conteúdo de arquivos.
* **Funcionamento e Fluxo:** Puxa os dados originais e instila o conteúdo cru em um novo construtor da classe `File`, alocando a cópia no HashMap do diretório atual.
* **Benefícios:** Cópia limpa (Deep Copy) efetuada exclusivamente em memória sem depender dos comandos de transação do SO hospedeiro.

### Remover Arquivos
* **Objetivo:** Exclusão cirúrgica de instâncias unitárias.
* **Funcionamento e Fluxo:** Semelhante à remoção de diretórios, localiza a String na coleção do diretório atual e remove seu ponteiro lógico após anotar `DELETE_FILE` no diário.
* **Benefícios:** Evita que os usuários removam diretórios cheios por engano, separando a regra de negócio da remoção unitária.

### Renomear Arquivos
* **Objetivo:** Mudar a nomenclatura de um documento.
* **Funcionamento e Fluxo:** Verifica restrições de nomes repetidos no escopo atual. Após validação, altera a propriedade no objeto e reconecta a nova chave no dicionário.
* **Benefícios:** Preserva a integridade e evita substituição de arquivos essenciais.

### Listar Conteúdo dos Diretórios
* **Objetivo:** Consultar as coleções atuais para a apresentação visual.
* **Funcionamento e Fluxo:** Um `for-each` itera sobre os `.values()` do HashMap, extraindo seus nomes, tamanhos e se são "Coleções" (Pastas) ou "Docs Virtuais".
* **Benefícios:** Alimenta a GUI e o Console em tempo real.

### Registro de Journaling
* **Objetivo:** Auditar todas as transações realizadas (Write-Ahead Logging).
* **Funcionamento e Fluxo:** Invoca imperativamente a gravação do texto formatado na Array antes que qualquer linha manipuladora de `Hashes` seja executada.
* **Benefícios:** É o cerne da garantia contra a corrupção do ecossistema estudado na disciplina.

## 8. Requisitos da Atividade Atendidos

| Requisito | Status | Evidência |
| --------- | ------ | --------- |
| Copiar arquivos | ✅ Atendido | Método `copyFile` implementado na camada `core`. |
| Apagar arquivos | ✅ Atendido | Método `deleteFile` em `FileSystemSimulator.java`. |
| Renomear arquivos | ✅ Atendido | Método `renameFile` testado e validado. |
| Criar diretórios | ✅ Atendido | Método `createDirectory` instanciando novos mapas lógicos. |
| Apagar diretórios | ✅ Atendido | Método `deleteDirectory`. |
| Renomear diretórios | ✅ Atendido | Método `renameDirectory`. |
| Listar arquivos de um diretório | ✅ Atendido | Exposto na GUI interativa (JTable) e via método `listFiles()`. |
| Operações via chamadas de métodos | ✅ Atendido | Todos os botões mapeiam para as interfaces públicas da classe Simulador. |
| Suporte a Journaling | ✅ Atendido | Classe `Journal` grava operações imperativamente. Exposto dinamicamente na UI. |
| Uso de Linguagem Java | ✅ Atendido | Código nativo isolado em pacotes Java (.java). |
| Modo Avançado (Shell/GUI) | ✅ Atendido | Criada GUI interativa com Java Swing e Interface CLI avançada complementar. |
| Estrutura do relatório no README | ✅ Atendido | O documento foi integralmente parametrizado às regras acadêmicas do edital. |

## 9. Diferenciais da Implementação

Nosso projeto excedeu substancialmente a documentação mínima de requisitos, ofertando diferenciais de alto rigor técnico:

* **Persistência Total e Isolada (Binária):** Criamos a persistência contínua através da serialização do Java. Após cada operação, o modelo é convertido no arquivo invisível `system_data.vfs`. Dessa forma, o Windows do avaliador não é exposto a pastas geradas por software acadêmico, e a aplicação garante a restauração perfeita dos arquivos caso fechada.
* **Interface Gráfica Java Swing:** Optou-se pelo desenvolvimento visual rico MVC (com janelas nativas de Windows, exploradores e barras de log dinâmicas) no lugar de terminais escuros de texto cru.
* **Anti-Directory Traversal:** Mecanismos de segurança blindam o sistema impedindo que inserções como `C:\arquivos` interfiram na nossa memória estrita.
* **Editor Interno de Documentos:** Ao invés de pastas vazias, nossos arquivos comportam injeção de conteúdos de texto e podem ser reabertos em caixas modais de edição da própria ferramenta.

## 10. Instalação e Execução

### Pré-requisitos
* Sistema Operacional **Windows 10** ou **Windows 11**.
* Ter o **Java Development Kit (JDK) 8** ou superior instalado e com o `javac` mapeado nas variáveis de ambiente (*Path*).
* Nenhuma biblioteca ou IDE externa é necessária (Sem Maven ou Gradle).

### Execução no Ambiente Windows
1. Realize o download (`.zip`) ou o Clone do Repositório do projeto.
2. Abra um terminal do Windows (Prompt de Comando "CMD" ou PowerShell).
3. Navegue até a raiz do projeto extraído (diretório onde está a pasta `src`).
   ```bash
   cd C:\Users\SeuUsuario\Downloads\SimuladorArquivos
   ```
4. **Compile todos os códigos-fonte:** (Esse comando criará automaticamente a pasta `out` de binários).
   ```bash
   javac -d out src/main/java/simulator/*/*.java
   ```
5. **Execute a Interface Gráfica interativa:**
   ```bash
   java -cp out simulator.ui.VpeGUI
   ```

*(Caso deseje avaliar via linha de comando (Shell Interativo), execute `java -cp out simulator.ui.Shell`)*.

## 11. Estrutura do Projeto

O código-fonte foi padronizado em pacotes orientados a responsabilidades:

```text
/src/main/java/simulator/
 ├── model/           (Entidades puras que detêm o estado lógico e Serializável)
 │    ├── FileSystemNode.java  # Classe Abstrata Polimórfica Base
 │    ├── File.java            # Arquivos (Folhas com buffer de conteúdo)
 │    ├── Directory.java       # Pastas (Coleções de HashMaps)
 │    └── Journal.java         # Registrador contínuo das operações (Log)
 │
 ├── core/            (Lógica de Negócios e Motor de Integração)
 │    └── FileSystemSimulator.java  # Controla as regras de Segurança, CRUD e Persistência
 │
 └── ui/              (Interação Humano-Computador)
      ├── Shell.java           # Aplicação CLI legada em texto
      └── VpeGUI.java          # Ambiente Gráfico Rico (VPE)
```

## 12. Resultados Obtidos
Ao utilizar o simulador, comprova-se visualmente a velocidade e confiabilidade que o uso de Coleções avançadas (como `HashMaps`) conferem ao acesso em partições virtuais. Constatamos empiricamente que o **Journaling** é, de fato, a última fronteira de segurança sistêmica: no nosso simulador, a capacidade de serializar o Journal no disco junto aos metadados permitiu que testes de desligamento abruptos fossem realizados, retornando os arquivos e o histórico sempre intactos. A aplicação auxilia profundamente a assimilação da Engenharia de Sistemas Operacionais, provando que um "Disco Local" nada mais é do que uma imensa árvore alocada eficientemente.

## 13. Conclusão
A execução prático-acadêmica do Simulador consolidou conhecimentos massivos no tratamento de Sistemas Lógicos Complexos. Ficou provado que arquitetar um ecossistema de arquivos requer responsabilidade irredutível quanto ao isolamento do fluxo. A aplicação de persistência seriada associada a um *Write-Ahead Logging* desmistificou o que era apenas teoria. Termina-se o desenvolvimento com um software palpável e elegante que não serve unicamente a uma nota acadêmica, mas entrega utilidade pedagógica de excelência para futuras apresentações na área da Ciência da Computação.

## 14. Repositório GitHub

📍 **Link oficial do projeto no GitHub:**  
👉 [INSERIR_LINK_DO_GITHUB_AQUI] 👈