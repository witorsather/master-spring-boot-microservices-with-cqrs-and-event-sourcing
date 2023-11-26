# Curso: Master Spring Boot Microservices with CQRS and Event Sourcing

## CQRS (Command Query Responsibility Segregation)

- Separação de responsabilidades entre leitura (Query) e escrita (Command) em uma aplicação.
- Exemplo: Ter uma camada de comandos para operações de escrita (e.g., salvar) e uma camada de consultas para operações de leitura (e.g., listar).

## Event Sourcing

- Utiliza um banco de eventos para armazenar o ciclo de vida de um objeto.
- Os eventos (create, update, delete) são armazenados e nunca apagados.
- Útil para ter um histórico completo das mudanças em um objeto.

## Microserviços

- Arquitetura que visa a independência e a falha isolada de serviços.
- Cada microserviço deve operar de forma independente, sem depender de outros serviços.
- Um microserviço deve considerar que "tudo falha o tempo todo" e deve falhar de forma independente.
- A comunicação entre microserviços ocorre através de um barramento de eventos (Event Bus).
- Os eventos são publicados por um microserviço e consumidos por outros, garantindo que as operações sejam realizadas de forma assíncrona e resiliente.
- O barramento de eventos permite que os eventos sejam persistidos mesmo se um consumidor falhar, garantindo a consistência quando o consumidor é reiniciado.

## Comandos, Queries e Eventos

- **Command**: Ordem de mudança no sistema, geralmente no formato de um verbo no imperativo (e.g., salvar, criar), pode conter dados sobre como executar a ordem.
- **Query**: Solicitação de um estado específico do objeto, operação de leitura.
- **Evento**: Objeto que descreve algo que já aconteceu, geralmente no formato de um verbo no passado (e.g., criado, atualizado).

## CQRS sem Event Sourcing

- Possibilidade de utilizar CQRS sem armazenamento de eventos, mantendo apenas o estado atual do sistema.

## Event Sourcing sem CQRS

- Utilização de Event Sourcing sem separação de responsabilidades entre leitura e escrita.

## API (Interface de Programação de Aplicação)
- Uma API é uma coleção de endpoints (pontos de extremidade) que permite a comunicação na web entre diferentes sistemas. Cada endpoint representa uma operação específica, que geralmente é realizada utilizando o protocolo HTTP.

-- **user.cmd.api**: API que não usa 

-- **user.query.api**: API que usa MongoDB

-- **user.core**: O projeto user.core não é uma API, pois não expõe endpoints restful, ele é uma biblioteca de classe interna, para apenas sistemas locais na rede springbankNet usar, por isso ele não possui a dependência Spring Web feita para usar controllers restful para expor endpoints.

## Biblioteca de Classes user.core
Quando o Axon Framework precisa interagir com o MongoDB (por exemplo, para armazenar eventos em um Event Store), ele usa a instância do MongoClient fornecida por esse bean. Essa configuração permite uma integração suave entre o Axon Framework e o MongoDB no contexto de um sistema que utiliza CQRS e Event Sourcing.

Ao Utilizar uma biblioteca de classe user.core na user.cmd.api eu posso reaproveitar o models > User da biblioteca em vez de ter que criar meus próprios modelos, uma vez que os modelos são os mesmos para a user.cmd.api e a user.query.api

![](imagens/user-cmd-user-core.png)

## Domínio de Usuário 
Para a gestão do domínio de usuário, adotamos a abordagem CQRS, que se traduz na divisão do domínio em duas APIs distintas: **user.cmd.api** e **user.query.api**. Ao contrário do modelo tradicional, onde uma única interface lida tanto com operações de leitura quanto de escrita, optamos por caminhos separados para otimizar cada responsabilidade.

Essa separação possibilita a otimização de cada caminho de acordo com suas funções específicas, resultando em benefícios tangíveis como melhorias na escalabilidade e desempenho. Com o CQRS, podemos modelar os comandos e as consultas de maneira independente, ajustando cada um conforme necessário para atender aos requisitos particulares de cada operação.

Interessante notar que para a visão de aplicação do usuário, existe apenas um ponto focal dos recursos, a interface visual que ele está usando, mas na visão API interface de computador, quando o usuário solicita leitura usa user.query.api e quando ele solicita registrar algo, usa user.cmd.api controller updtate.

## Comandos do **user.cmd.api** e Eventos do **user.core** (Biblioteca de Classes)
- Todo comando resulta em um evento, os eventos ficam no user.core e são criados através do user.cmd.api pacote commands e pelos 3 comandos, Register, Update e Remove, portanto comandos do user.cmd.api e eventos do user.core estão fortemente relacionados, na verdade eles possuem a mesma aparência, a única diferença é que nos comandos tem a anotação do Axon @TargetAggregateIdentifier. Vale notar que a user.query.api não produz nenhum comando, portanto não produz nenhum evento, ela apenas consome eventos e dados do banco de leitura.

## user.cmd.api
### UserAggregate
Interessante notar que nessa classe temos 2 tipos de manipuladores, manipuladores de comandos e manipuladores de eventos, ou seja, o agregado é responsável por receber comandos transforma-los em eventos e logo em seguida outra método seu manipular esse evento transformado pelo manipulador de comando pra evento.

Reponsável por receber um comando dos tipos Register, Updtate e Remove user e retornar um evento. Todos os manipuladores de fornercimento de eventos combinados formam o agregado.

Principal método do UserAggregate que é um construtor da classe que recebe um comando, processa esse comando, criando um novo usuário em memória com a senha em hash e cria um novo evento baseado nos dados do comando, interessante notar como o AggregateLifecycle.apply pega esse evento e *salva ele no banco de dados MongoDB e publica no Ônibus de Eventos* (mensageria) onde a user.query.api irá consumir posteriormente.

``` java
    @CommandHandler
    public UserAggregate(RegisterUserCommand command) {
        var newUser = command.getUser();
        newUser.setId(command.getId());
        var password = newUser.getAccount().getPassword();
        passwordEncoder = new PasswordEncoderImpl();
        var hashPassword = passwordEncoder.hashPassword(password);
        newUser.getAccount().setPassword(hashPassword);

        var event = new UserRegisteredEvent().builder()
                .id(command.getId())
                .user(newUser)
                .build();

        AggregateLifecycle.apply(event);
    }
```
O user.api.cmd > aggregates > UserAggregate, possui 3 métodos que manipulam os comandos, para que servem? Esses métodos recebem um comando e validam se esse comando é válido e aplicam a regra de negócio, ou seja, o agregado é um ponto central para validação de comandos para criação de eventos corretos. Também possui 3 métodos on, responsáveis por atualizar o estado do agregado após a criação de um novo evento, permitindo assim a recriação do agregado em caso de falha e mantendo a consistência do agregado, excelente para sistemas distribuídos. Dessa forma o agregado nesas arquitetura CQRS é um serviço que recebe comandos e retorna eventos.


## user.query.api
### handlers > UserEventHandler
Para manipular/processar comandos na user.cmd.api temos o serviço UserAggregate e para manipular/processar os eventos temos no user.query.api o UserEventHandler. É interessante notar que o UserEventHandler da user.query.api não tem acesso ao banco de eventos (escrita), seu acesso no que se refere a eventos é restrito ao Event Bus (barramento de eventos).

Manipular/Processar 
- *Comandos* => user.cmd.api.UserAggregate recebe comando e cria evento pro Event Bus (barramento de eventos) e pro Event Storage (banco de escrita).
- *Eventos* => user.cmd.api.UserEventHandler recebe evento e cria registro no banco de leitura.

### UserEventHandler vs QueryEventHandler
No UserEventHandler eu pego um evento do Event Bus e persisto no banco de leitura, já o QueryEventHandler eu apenas consulto os dados no banco de leitura, note novamente a separação de responsabilidades de leitura e escrita dentro da user.query.api, apesar das duas usarem o userRepository eu dividi em 2 responsabilidade escrita e leitura e cada uma usa separadamente o userRepository.

### controllers > RegisterUserController
O controller recebe o comando do cliente da api, com o comando em mãos ele o envia para o Axon Framework usando o commandGateway.send(command), aí o Axon Framework com um comando em mãos se pergunta, tem algum método marcado com a anotação @CommandHandler? A tem, então manda pra ele processar.

```java
    @PostMapping
    public ResponseEntity<RegisterUserResponse> registerUser(RegisterUserCommand command) {
        try {
            this.commandGateway.sendAndWait(command);
            
        } catch (Exception e) {
            var safeErrorMessage = "Error while processing register user request for id - " + command.getId();
            System.out.println(e.toString());

            return new ResponseEntity<>(new RegisterUserResponse(safeErrorMessage), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

```

sai da controller pro Axon Framework e depois pro aggregate através do AggregateLifecycle publicar no barramento de eventos e armazenar no banco de dados de eventos.

```java
@CommandHandler
    public UserAggregate(RegisterUserCommand command) {
        var newUser = command.getUser();
        newUser.setId(command.getId());
        var password = newUser.getAccount().getPassword();
        passwordEncoder = new PasswordEncoderImpl();
        var hashPassword = passwordEncoder.hashPassword(password);
        newUser.getAccount().setPassword(hashPassword);

        var event = new UserRegisteredEvent().builder()
                .id(command.getId())
                .user(newUser)
                .build();

        AggregateLifecycle.apply(event);
```



