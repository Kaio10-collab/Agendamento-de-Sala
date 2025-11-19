### 📚 AgendamentoSala API
Bem-vindo ao repositório do projeto AgendamentoSala, uma API RESTful construída em Java 21 e Spring Boot. Este serviço gerencia o agendamento de salas de reunião, garantindo validações de capacidade, disponibilidade de horários e um fluxo de confirmação e cancelamento assíncrono.

### 🌟 Visão Geral e Arquitetura
O projeto adota a Arquitetura Limpa (Clean Architecture), garantindo que as regras de negócio sejam independentes da infraestrutura (frameworks, banco de dados, mensageria, etc.).

A estrutura de pacotes principais reflete as camadas da Clean Architecture:
* domain: Contém as Regras de Negócio Core (Entidades, Validadores e Exceções). Totalmente independente de frameworks.
* application: Contém os Use Cases (Serviços de Aplicação) que orquestram o fluxo de dados e aplicam regras de domínio.
* infrastructure: Contém a lógica de integração com o mundo externo (APIs REST, Persistência via JPA/Hibernate, e Mensageria via RabbitMQ).

### 🏢 Serviço de Sala
Salas Disponíveis no Sistema
| Nome      | Capacidade | Status Inicial |
|:-------------:|:-------| :-------: |
| HULK | 5 pessoas |	Liberada |
| THOR | 10 pessoas |	Liberada |
| LOKI | 15 pessoas |	Liberada |
| DR STRANGE | 20 pessoas |	Liberada |
| CAP MARVEL | 25 pessoas | Liberada |

Status das Salas
* Liberada: Disponível para reserva
* Stand by: Temporariamente reservada (15 minutos)
* Pendente: Reservada e aguardando check-in
* Checked-in: Em uso

Regras de Reserva
* Horário de funcionamento: 08:00 às 18:00
* Tempo mínimo de reserva: 30 minutos
* Tempo de Stand by: 15 minutos
* Janela de check-in: 15 minutos antes até 15 minutos após o horário reservado

### ✅ Serviço de Check-in
Regras de Check-in:
* Usuário deve ter reserva com status "Pendente"
* Check-in permitido apenas dentro da janela de 30 minutos (15 min antes até 15 min após)
* Após check-in, status da sala muda para "Checked-in"

### 💻 Estrutura da Camada Domain (Domínio)
Abaixo estão as classes críticas de domínio que definem as regras e entidades do sistema:

#### 1. Modelos Principais (Entidades):

| Classe      | Descrição|
|:-------------:|:-------|
| User        | Representa um usuário (identificado pelo e-mail).    |
| Room        | Representa uma sala de reunião, incluindo nome e capacidade.    |
| Reservation | Entidade principal que armazena o agendamento (quem, onde, quando).    |
| RoomName    | Enum que define os nomes das salas (ex: THOR, HULK) e suas capacidades fixas. |
| RoomStatus  | Enum que define o status da reserva (ex: STAND_BY, PENDING, CHECKED_IN, AVAILABLE). |

#### 2. Validadores:
| Classe      | Propósito|
|:-------------:|:-------|
| EmailValidator      | Validação de formato de e-mail válido, ele valida o formato sintático de um e-mail utilizando regex. |
| NameValidator       | Valida o nome completo, que não pode ser nulo, vazio ou conter números/caracteres especiais (!@#$%¨&*()_+=). |
| ValidationException | Exceção customizada utilizada em toda a aplicação para falhas de regras de negócio. |

### ⚙️ Fluxo e Usecases (Serviços)
Os Usecases (ou Application Services) implementam as regras de negócio e manipulam o estado das entidades.
#### 1. Gerenciamento de Reservas (Core)
| Usecase      | Descrição|
|:-------------:|:-------|
| CreateReservationUseCase | Cria uma reserva no status STAND_BY. Verifica conflitos de horário, capacidade da sala e se o usuário já tem reserva ativa. Dispara o timeout assíncrono. |
| ConfirmReservationUseCase| Move uma reserva do status STAND_BY para PENDING (Após a confirmação do usuário). |
| DeleteReservationUseCase | Remove uma reserva ativa do sistema. |
| TriggerCheckInUseCase    | Inicia o processo de check-in do usuário. Valida se o check-in está sendo feito dentro da janela de 30 minutos (15 min antes até 15 min depois do início). Dispara a mensagem assíncrona para o consumer. |
| ListAvailableRoomsUseCase| Retorna a lista de salas que não possuem conflito de agendamento no período solicitado. |

#### 2. Processamento Assíncrono (Consumers)
| Usecase(Consumido por RabbitMQ)      | Descrição|
|:-------------:|:-------|
| ProcessStandByCancellationUseCase | Acionado pelo timeout do RabbitMQ. Se o status for STAND_BY, cancela a reserva, mudando o status para AVAILABLE. |
| ProcessCheckInUseCase | Acionado pelo TriggerCheckInUseCase. Se o status for PENDING, move a reserva para CHECKED_IN. |

#### 3. CRUD de Entidades
| Usecase      | Função|
|:-------------:|:-------|
| CreateRoomUseCase	| Cria uma nova sala, garantindo que o nome seja único.
| UpdateRoomUseCase	| Atualiza o nome de uma sala, verificando se o novo nome não está em uso. |
| DeleteRoomUseCase	| Exclui uma sala pelo ID. |
| ReadRoomUseCase	| Busca salas por ID ou todas as salas. |
| CreateUserUseCase	| Cria um usuário, garantindo que o e-mail seja único. |
| UpdateUserUseCase	| Atualiza os dados do usuário, tratando a mudança de e-mail (chave identificadora). |
| ReadUserUseCase	| Busca um usuário por e-mail. |
| DeleteUserUseCase | Exclui um user pelo ID. |

### 🚀 Endpoints da API (Exemplos)
Esta seção lista os principais endpoints RESTful da API.
#### 1. Usuários (/api/v1/users)
| Método| Endpoint| Usecase| Descrição|
|:-------------:|:-------:| :-------:| :-------|
| POST|	/api/v1/users |	CreateUserUseCase |	Cadastra um novo usuário. |
| GET|	/api/v1/users/{email} |	ReadUserUseCase |	Busca um usuário pelo e-mail. |
| PUT|	/api/v1/users/{currentEmail} |	UpdateUserUseCase |	Atualiza nome e/ou e-mail. |
| DELETE|	/api/v1/users/{email} |	DeleteUserUseCase |	Exclui um usuário. |

Exemplo: Criar Usuário (POST /api/v1/users):

Request JSON:
``` http
{
  "fullName": "Mariana Souza",
  "email": "mariana.souza@empresa.com"
}
```
Response JSON (201 Created):
``` http
{
  "fullName": "Mariana Souza",
  "email": "mariana.souza@empresa.com"
}
```

#### 2. Salas (/api/v1/rooms)
| Método| Endpoint| Usecase| Descrição|
|:-------------:|:-------:| :-------:| :-------|
| POST |	/api/v1/rooms/{roomName} |	CreateRoomUseCase |	Cria uma nova sala. |
| GET |	/api/v1/rooms |	ReadRoomUseCase |	Lista todas as salas. |
| PUT |	/api/v1/rooms/{id}/{newName} |	UpdateRoomUseCase |	Atualiza o nome de uma sala. |
| DELETE |	/api/v1/rooms/{id} |	DeleteRoomUseCase |	Exclui uma sala. |

Exemplo: Criar Sala (POST /api/v1/rooms):

Request JSON:
``` http
{
  "name": "SPARTAN" 
}
```
* Observação: SPARTAN deve ser um valor válido do enum RoomName. Ou seja, não irá conseguir salvar, já que não temos esse nome dentro da classe.

#### 3. Reservas (/api/v1/reservations)
| Método| Endpoint| Usecase| Descrição|
|:-------------:|:-------:| :-------:| :-------|
| POST | /api/v1/reservations/{userEmail} |	CreateReservationUseCase | Cria uma nova reserva (STAND_BY). |
| PUT |	/api/v1/reservations//confirm/{userEmail} |	ConfirmReservationUseCase | Confirma uma reserva STAND_BY para PENDING. |
| PUT |	/api/v1/reservations//check-in/{userEmail} |	TriggerCheckInUseCase | Inicia o processo de check-in (dentro da janela de 30 min). |
| GET |	/api/v1/reservations/available |	ListAvailableRoomsUseCase | Lista salas disponíveis para o período.| 
| GET |	/api/v1/reservations//user/{email} |	ReadReservationUseCase | Lista reservas ativas de um usuário. | 
| DELETE |	/api/v1/reservations/{userEmail} |	DeleteReservationUseCase | Exclui a reserva ativa de um usuário. | 

Exemplo: Criar Reserva (POST /api/v1/reservations):

Request JSON:
``` http
{
  "userEmail": "mariana.souza@empresa.com",
  "roomName": "THOR",
  "requiredPeople": 8,
  "startDateTime": "2025-12-20T14:00:00",
  "endDateTime": "2025-12-20T15:30:00"
}
```
Response JSON (201 Created):
``` http
{
  "id": 123,
  "userEmail": "mariana.souza@empresa.com",
  "roomName": "THOR",
  "requiredPeople": 8,
  "startDateTime": "2025-12-20T14:00:00",
  "endDateTime": "2025-12-20T15:30:00",
  "status": "STAND_BY" 
}
```

Exemplo: Iniciar Check-in (/api/v1/reservations//check-in/{userEmail}):

Request JSON:
``` http
{
  "email": "mariana.souza@empresa.com"
}
```
