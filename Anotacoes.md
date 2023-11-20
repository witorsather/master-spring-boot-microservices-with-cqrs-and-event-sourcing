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


