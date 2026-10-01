# Projeto de Microsserviços - MarketPlace

Este projeto possui uma arquitetura de microsserviços utilizando Spring Boot, Docker, JWT para autenticação, alem de padrões como Feign Client e Circuit Breaker.

## Arquitetura da Solução

O sistema é composto por 3 microsserviços principais e 2 bancos de dados:

1. **Auth Service (Porta 8083)**: 
   - Responsável pela autenticação dos usuários e emissão de tokens.
   - Não possui banco de dados próprio (usuários em memória para demonstração).
2. **Product Service (Porta 8081)**:
   - Gerencia o catálogo de produtos.
   - **Banco de Dados**: MongoDB (`mongodb` na porta 27017).
   - Este serviço é **protegido**, necessitando de um Token JWT válido para acesso.
3. **Order Service (Porta 8082)**:
   - Gerencia a criação e listagem de pedidos.
   - **Banco de Dados**: PostgreSQL (`postgres` na porta 5432).
   - Comunica-se de forma síncrona com o `product-service` usando *OpenFeign*.
   - Implementa o padrão *Circuit Breaker* (com Resilience4j) como fallback caso o serviço de produtos esteja indisponível.

Todos os serviços e bancos de dados rodam em containers Docker na mesma rede (`microservices-net`).

## Tecnologia Escolhida de Segurança

A tecnologia escolhida para segurança foi o **JWT (JSON Web Token)**. O `auth-service` valida as credenciais e emite dois tokens: um **Access Token** (tempo de expiração curto) e um **Refresh Token** (tempo de expiração longo). Os demais serviços (neste caso o `product-service`) interceptam as requisições, leem o token do cabeçalho `Authorization` e o validam utilizando a mesma chave secreta (JWT Secret) fornecida por variável de ambiente.

## Como Executar os Serviços

Certifique-se de ter o Docker e o Docker Compose instalados na sua máquina.

1. Navegue até a raiz do projeto (onde encontra-se o arquivo `docker-compose.yml`).
2. Execute o comando abaixo para realizar o build das imagens e subir os containers:
```bash
   docker-compose up -d --build
```

3. Aguarde alguns instantes para que os bancos de dados e os serviços Java inicializem.

## Como Realizar Autenticação

A autenticação é feita enviando as credenciais (username e password) para o endpoint de login do `auth-service`. 
As credenciais são:
- **Usuário 1**: admin / admin123
- **Usuário 2**: user / user123

Você receberá um `accessToken` e um `refreshToken` no corpo da resposta. O `accessToken` deve ser enviado no Header de requisições para serviços protegidos
## Como Utilizar o Endpoint de Refresh

Quando o seu `accessToken` expirar (por padrão 15 minutos), você não precisará fazer login novamente. Basta utilizar o endpoint de Refresh enviando o seu `refreshToken`. A API retornará um novo par de `accessToken` e `refreshToken`.

## Endpoints Públicos e Protegidos

### Públicos (Não necessitam de token)
*   **Auth Service (`localhost:8083`)**:
    *   `POST /api/auth/login`
    *   `POST /api/auth/refresh`
*   **Order Service (`localhost:8082`)**:
    *   `GET /api/orders`
    *   `POST /api/orders`

### Protegidos (Necessitam de token JWT via Header `Authorization`)
*   **Product Service (`localhost:8081`)**:
    *   `GET /api/products`
    *   `GET /api/products/{id}`
    *   `POST /api/products`

---

## Exemplos de Requisições para Teste

### 1. Realizar Login (Autenticação)
```bash
curl -X POST http://localhost:8083/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "admin", "password": "admin123"}'
```
*Guarde os valores de `accessToken` e `refreshToken` retornados.*

### 2. Cadastrar um Produto (Serviço Protegido)
Substitua `<SEU_ACCESS_TOKEN>` pelo token recebido no passo 1.
```bash
curl -X POST http://localhost:8081/api/products \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <SEU_ACCESS_TOKEN>" \
     -d '{"name": "Notebook Gamer", "price": 4500.00}'
```
*Guarde o ID do produto retornado (ex: `64a5c...`).*

### 3. Criar um Pedido
O `order-service` chamará internamente o `product-service` para verificar o preço do produto.
```bash
curl -X POST http://localhost:8082/api/orders \
     -H "Content-Type: application/json" \
     -d '{"productId": "<ID_DO_PRODUTO_AQUI>", "quantity": 2}'
```

### 4. Utilizar o Endpoint de Refresh
Substitua `<SEU_REFRESH_TOKEN>` pelo refresh token que você guardou no passo 1.
```bash
curl -X POST http://localhost:8083/api/auth/refresh \
     -H "Content-Type: application/json" \
     -d '{"refreshToken": "<SEU_REFRESH_TOKEN>"}'
```
