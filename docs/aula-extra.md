- Maven, Quarkus e Java:
    - Maven: Gerenciador de dependências - ao instalar o maven, sua maquina aprende a entender comandos mvn e passa a ser possivel utilizar desses comandos no terminal. Ele gerencia as dependências de um projeto Java.
    - Quarkus: Framework Java - É um framework que roda em cima do Java e fornece diversas funcionalidades para o desenvolvimento de aplicações Java. É um framework moderno e leve, ideal para o desenvolvimento de microsserviços. Podemos dizer que ele é um "conjunto de ferramentas" que auxilia no desenvolvimento de aplicações Java.
    - Java: Linguagem de programação - É a linguagem de programação que utilizamos para escrever o código da nossa aplicação.

- Diferença entre 'implements' e 'extends':
    - 'implements': Qualquer classe que implemente uma interface deve ter os métodos da interface e a implementação deles, dizendo o que cada método faz.
    - 'extends': Qualquer classe que herda de outra classe (extende), herda todos os métodos já todos implementados e montados da classe que está sendo herdada. A classe que herda ainda pode implementar novos metodos.

- banco relacional (SQL) x não relacional (NoSQL) (exemplos)
    - Banco de dados relacional (SQL): É um banco de dados que armazena dados em tabelas, onde os dados são organizados em linhas e colunas, portanto, todas as linhas (instancias de uma tabela) devem ter as mesmas colunas.
    - Banco de dados não relacional (NoSQL): É um banco de dados que armazena dados em documentos, que são compostos por pares de chave-valor. Cada documento (como se fosse cada linha), não precisa ter as mesmas chaves (como se fossem as colunas) que as outras linhas.
    - Exemplo banco relacional: tabela usuarios tem colunas: nome, email, senha. Cada linha representa um usuario e tem valores para nome, email e senha.
    - Exemplo banco não relacional: documentos de usuarios tem:
        - Documento 1: nome, email, senha
        - Documento 2: nome, email
        - Documento 3: nome, email, telefone
    - O banco de dados relacional (SQL) é mais indicado para dados estruturados, ou seja, dados que tem um formato bem definido e que não mudam muito. Já o banco de dados não relacional (NoSQL) é mais indicado para dados não estruturados, ou seja, dados que tem um formato que muda muito e que não tem um formato bem definido.
    - Exemplos de bancos de dados relacionais: MySQL, PostgreSQL, Oracle, SQL Server.
    - Exemplos de bancos de dados não relacionais: MongoDB, Cassandra, Redis, DynamoDB.

- O que são APIs REST e como funcionam (endpoints, métodos HTTP)
    - API REST é um tipo de API que segue os princípios REST (Representational State Transfer), que são um conjunto de regras e protocolos que definem como os dados devem ser transferidos entre o cliente e o servidor.
    
- Estrutura basica (model/entity, DTO, Resource, Repository, Service)
    - **Resource (ou Controller)** *(O Garçom / A Porta de Entrada)*: Como você mesmo exemplificou, é a "porta de entrada" da sua aplicação para o mundo externo (internet, aplicativos). De forma simples: É onde você define as rotas (ex: /usuarios), os métodos HTTP (GET para buscar, POST para criar, etc.) e os parâmetros. Ele fica "escutando" as requisições que chegam. Quando o Resource recebe um pedido da internet, ele não resolve o problema sozinho, ele apenas anota o pedido e repassa para quem entende das regras (o Service).
    
    - **Service** *(O Gerente / O Chef de Cozinha)*: É onde mora o "cérebro" da sua aplicação, também chamado de Regra de Negócio. De forma simples: O Resource repassou o pedido para cá. O Service é quem vai fazer as validações pesadas: "Esse CPF é válido?", "Esse usuário tem saldo na conta para fazer esse investimento?", "A senha tem mais de 8 caracteres?". Ele processa a lógica toda. Se tudo estiver certo, ele chama o Repository para finalmente guardar os dados.
    
    - **Repository** *(O Estoquista / Acesso ao Cofre)*: É a única parte do sistema que sabe conversar de verdade com o Banco de Dados. De forma simples: O Service não quer saber se o banco de dados é MySQL, PostgreSQL ou MongoDB, ele só diz para o Repository: "Salva esse investimento aí pra mim". O Repository é quem conhece os comandos exatos (como o SQL) para ir lá no banco de dados e inserir, buscar, atualizar ou deletar os dados.
    
    - **Model / Entity (Entidade)** *(Os Ingredientes Originais / O Molde)*: É a classe que representa as coisas do mundo real dentro do seu código e como elas são no banco de dados. De forma simples: Se você tem um aplicativo de investimentos, você terá uma Entidade Investimento, uma Entidade Usuario. É como se fosse o "molde" ou a "tabela" do banco de dados traduzida para código. Ela tem todos os campos: id, nome, email, senha_criptografada, etc.
    
    - **DTO (Data Transfer Object)** *(A Bandeja de Entrega / O Filtro)*: Como o nome diz, é um Objeto de Transferência de Dados. Ele serve para carregar as informações de um lado pro outro de forma segura e enxuta. De forma simples: Imagine que alguém fez uma requisição para ver os dados de um Usuário. Se você devolver a Entidade (Model) inteira, a senha criptografada do usuário vai junto para a tela do celular! Para evitar isso, você cria um DTO (ex: UsuarioResponseDTO) que tem apenas o nome e o email. É uma versão "filtrada" e feita sob medida dos seus dados para viajar pela internet.
