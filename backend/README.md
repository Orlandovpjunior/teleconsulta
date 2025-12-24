# Teleconsulta API - Backend

API REST para sistema de teleconsulta médica desenvolvida com Spring Boot.

## 🚀 Tecnologias

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Security** com JWT
- **Spring Data JPA**
- **PostgreSQL**
- **Lombok**
- **Swagger/OpenAPI**

## 📋 Pré-requisitos

- Java 17+
- Maven 3.8+
- Docker e Docker Compose (para o banco de dados)

## 🔧 Configuração

### 1. Iniciar o banco de dados PostgreSQL

```bash
docker-compose up -d
```

### 2. Executar a aplicação

```bash
./mvnw spring-boot:run
```

Ou com Maven instalado:

```bash
mvn spring-boot:run
```

### 3. Acessar a documentação da API

Após iniciar a aplicação, acesse:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🧪 Executar os Testes

```bash
./mvnw test
```

Ou para testes com relatório detalhado:

```bash
./mvnw test -Dtest=*Test
```

## 👤 Usuários de Teste (Ambiente Dev)

A aplicação cria automaticamente os seguintes usuários no ambiente de desenvolvimento:

| Tipo | Email | Senha | Descrição |
|------|-------|-------|-----------|
| Admin | admin@teleconsulta.com | admin123 | Administrador do sistema |
| Médico | joao.silva@teleconsulta.com | doctor123 | Dr. João Silva - Clínica Geral |
| Médico | maria.santos@teleconsulta.com | doctor123 | Dra. Maria Santos - Cardiologia |
| Médico | pedro.oliveira@teleconsulta.com | doctor123 | Dr. Pedro Oliveira - Dermatologia |
| Paciente | carlos@email.com | patient123 | Paciente de teste |

## 📚 Endpoints Principais

### Autenticação
- `POST /api/auth/register` - Registrar novo usuário
- `POST /api/auth/login` - Fazer login

### Usuários
- `GET /api/users/me` - Dados do usuário logado
- `GET /api/users/{id}` - Buscar usuário por ID
- `PUT /api/users/{id}` - Atualizar usuário

### Médicos (Público)
- `GET /api/doctors/public` - Listar todos os médicos
- `GET /api/doctors/public/specialty/{specialty}` - Buscar por especialidade

### Planos
- `GET /api/plans/public` - Listar planos disponíveis (público)
- `POST /api/plans/{planId}/subscribe` - Assinar plano
- `DELETE /api/plans/subscription` - Cancelar assinatura

### Consultas
- `GET /api/appointments` - Listar minhas consultas
- `POST /api/appointments` - Agendar nova consulta
- `PATCH /api/appointments/{id}/cancel` - Cancelar consulta
- `PATCH /api/appointments/{id}/confirm` - Confirmar consulta (médico)

## 🏗️ Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/teleconsulta/
│   │   ├── config/          # Configurações (Security, OpenAPI)
│   │   ├── controller/      # REST Controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # Entidades JPA
│   │   ├── exception/       # Exceções customizadas
│   │   ├── repository/      # Repositórios JPA
│   │   ├── security/        # JWT e filtros de segurança
│   │   └── service/         # Lógica de negócio
│   └── resources/
│       ├── application.properties
│       ├── application-dev.properties
│       └── application-prod.properties
└── test/
    ├── java/com/teleconsulta/
    │   ├── controller/      # Testes de controllers
    │   └── service/         # Testes de services
    └── resources/
        └── application-test.properties
```

## 🔐 Autenticação

A API utiliza JWT (JSON Web Token) para autenticação. Para acessar endpoints protegidos:

1. Faça login em `POST /api/auth/login`
2. Copie o token da resposta
3. Adicione o header: `Authorization: Bearer <seu-token>`

## 📝 Exemplo de Uso

### Fazer Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "carlos@email.com",
    "password": "patient123"
  }'
```

### Listar Médicos

```bash
curl http://localhost:8080/api/doctors/public
```

### Agendar Consulta

```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <seu-token>" \
  -d '{
    "doctorId": 2,
    "scheduledAt": "2024-12-30T10:00:00",
    "patientComplaint": "Dor de cabeça frequente"
  }'
```

## 🔄 Próximos Passos

- [ ] Integração com serviço de videochamada
- [ ] Sistema de notificações (email/push)
- [ ] Prontuário eletrônico
- [ ] Integração com gateway de pagamento
- [ ] Frontend Next.js

## 📄 Licença

Este projeto está sob a licença MIT.

