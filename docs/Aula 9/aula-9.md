# 🎓 Aula 9: Segurança de Verdade — Autenticação, Autorização e Dono do Recurso

> **Projeto:** Invest-Tracker (Backend)  
> **Tecnologias:** Java 21, Quarkus 3.39+, SmallRye JWT, `@Authenticated`, `@RolesAllowed`, `@PermitAll`, CDI `@RequestScoped`.  
> **Pré-requisitos:** Aula 7 (CRUD e CDI), Aula 8 (exceções em 3 camadas) e o fluxo de cadastro/login com JWT já implementado.

---

## 🎯 1. Objetivo da Aula

Na aula anterior o sistema ganhou **login**: o usuário se cadastra, verifica o e-mail e recebe um **token JWT**. Mas ter login não é o mesmo que ter segurança. Nesta aula vamos:

1. Entender a diferença entre **Autenticação** (quem é você?) e **Autorização** (o que você pode fazer?).
2. Aplicar o princípio **"negar por padrão"** em toda a API.
3. Proteger endpoints por **papel** (`USER` × `ADMIN`) com `@RolesAllowed`.
4. Implementar a regra de **dono do recurso**: usuário comum só vê e altera o que é **dele**; o ADMIN vê e altera tudo.
5. Parar de confiar no `userId` enviado pelo cliente: **a identidade vem do JWT**.
6. Padronizar as respostas **401** e **403** no `GlobalExceptionHandler`.
7. Corrigir **vazamentos de informação** (mensagens de erro, enumeração de e-mails).
8. Resolver o problema do **primeiro administrador** sem colocar senha no código.

---

## 🔍 2. O Diagnóstico: o que estava inseguro?

Antes desta aula, **apenas `/auth/me`** exigia token. Todo o resto estava aberto para a internet.

| Problema Identificado | Como um atacante exploraria | Solução Adotada |
| :--- | :--- | :--- |
| **Endpoints sem nenhuma proteção** | `DELETE /users/{id}` sem token apagava qualquer usuário. | `@Authenticated` / `@RolesAllowed` em todos os Resources + **negar por padrão**. |
| **`userId` vindo do corpo da requisição** (`WalletForm`) | Basta trocar o `userId` no JSON para criar carteiras na conta de outra pessoa. | O dono é lido do **JWT** (`CurrentUser`). `userId` virou opcional e só o ADMIN pode informá-lo. |
| **Listagens globais** | `GET /wallets` devolvia as carteiras de **todos** os usuários. | Usuário comum recebe só as suas; ADMIN recebe todas. |
| **Sem checagem de dono** | Com o UUID de uma carteira alheia, era possível ler, alterar e excluir. | `checkAccess(ownerId)` em todo acesso por ID → **403 Forbidden**. |
| **Erro 500 devolvia `ex.getMessage()`** | Mensagens do Hibernate/SQL revelam tabelas, colunas e constraints. | Detalhe vai para o **log**; o cliente recebe uma mensagem genérica. |
| **`resend-verification` respondia "User not found"** | Permite descobrir quais e-mails têm conta (**enumeração de usuários**). | Resposta **sempre igual**, exista ou não o e-mail. |
| **Busca de e-mail sensível a maiúsculas** | `Ana@Mail.com` passava pela checagem de duplicidade e estourava 500 no banco. | `UserRepository.findByEmail` normaliza (`trim().toLowerCase()`). |
| **Nenhuma forma segura de criar um ADMIN** | A role só podia ser alterada direto no banco. | `AdminSeeder` lê e-mail/senha de **variáveis de ambiente** na inicialização. |
| **Senha do banco e CORS `*` fixos no código** | Mesmos segredos em dev e produção; qualquer site podia chamar a API. | `${DB_PASSWORD:postgres}`, `${CORS_ORIGINS:...}` com valores só para dev. |

---

## 🧠 3. Conceitos Fundamentais

### 3.1. Autenticação × Autorização

| | Autenticação (AuthN) | Autorização (AuthZ) |
| :--- | :--- | :--- |
| **Pergunta** | *Quem é você?* | *Você pode fazer isso?* |
| **Como resolvemos** | Login → JWT assinado pelo servidor | Roles no token + regra de dono no Service |
| **Falhou? HTTP…** | **401 Unauthorized** (sem token, token inválido ou expirado) | **403 Forbidden** (sei quem você é, mas não pode) |

> 💡 **Analogia:** o crachá da empresa é a **autenticação** — ele prova quem você é. Mas o crachá do estagiário não abre a sala do servidor: isso é **autorização**.

### 3.2. Relembrando: o que tem dentro do nosso JWT?

O `TokenService` gera o token com:

```java
Jwt.issuer(issuer)
   .upn(user.email)
   .subject(user.id.toString())   // "sub"   -> QUEM é o usuário
   .groups(Set.of(user.role))     // "groups" -> QUAIS roles ele tem (USER ou ADMIN)
   .expiresIn(Duration.ofHours(24))
   .sign();                       // assinado com a privateKey.pem
```

O token é **assinado** com a chave privada do servidor. Se o cliente alterar uma vírgula (por exemplo, trocar `"groups":["USER"]` por `["ADMIN"]`), a assinatura deixa de bater e o Quarkus rejeita o token com **401**. Por isso podemos **confiar** no `sub` e nos `groups` do token — e **não** podemos confiar em um `userId` digitado no JSON.

> ⚠️ O JWT é **assinado**, não **criptografado**. Qualquer um consegue ler o conteúdo (cole um token em [jwt.io](https://jwt.io)). Nunca coloque senha ou dado sensível dentro dele.

### 3.3. As três camadas de proteção

```text
Requisição HTTP
   │
   ▼
① Anotação no Resource ........ "Precisa estar logado? Precisa ser ADMIN?"   (@Authenticated / @RolesAllowed)
   │
   ▼
② Regra de dono no Service .... "Essa carteira é SUA?"                      (currentUser.checkAccess(...))
   │
   ▼
③ Filtro nas consultas ........ "Liste só o que é seu."                     (listByUserId(currentUser.id()))
```

A camada ① não basta sozinha: `@Authenticated` garante que **alguém** está logado, mas não que ele é dono da carteira `f439...`. Isso depende do **dado** e só pode ser verificado no **Service**, depois de buscar o registro no banco.

---

## 🏛️ 4. A Nova Estrutura de Segurança

```text
backend/src/main/java/
├── security/                         <-- NOVO pacote
│   ├── Roles.java                    (constantes USER / ADMIN)
│   ├── CurrentUser.java              (quem está logado nesta requisição)
│   └── AdminSeeder.java              (cria o 1º ADMIN na inicialização)
├── exception/custom/
│   └── AccessDeniedException.java    (NOVA → HTTP 403)
├── exception/handler/
│   └── GlobalExceptionHandler.java   (+ 401, + 403, 500 sem vazamento)
└── dto/user/
    └── RoleForm.java                 (NOVO → troca de role pelo ADMIN)
```

### 4.1. `Roles`: por que constantes e não um `enum`?

```java
public final class Roles {
    public static final String USER = "USER";
    public static final String ADMIN = "ADMIN";
}
```

Anotações Java só aceitam **constantes de compilação**. `@RolesAllowed(Roles.ADMIN)` compila; `@RolesAllowed(Role.ADMIN.name())` **não**. Com a constante, evitamos espalhar a string `"ADMIN"` pelo código (um erro de digitação como `"ADMN"` bloquearia todo mundo sem nenhum aviso).

### 4.2. `CurrentUser`: o usuário da requisição

```java
@RequestScoped
public class CurrentUser {

    @Inject
    JsonWebToken jwt;

    public UUID id() {
        String subject = jwt.getSubject();
        if (subject == null) {
            throw new UnauthorizedException("Authentication is required.");
        }
        return UUID.fromString(subject);
    }

    public boolean isAdmin() {
        return jwt.getGroups() != null && jwt.getGroups().contains(Roles.ADMIN);
    }

    public void checkAccess(UUID ownerId) {
        if (!isAdmin() && !id().equals(ownerId)) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }
    }

    public void requireAdmin(String message) {
        if (!isAdmin()) {
            throw new AccessDeniedException(message);
        }
    }
}
```

**Pontos para discutir com o aluno:**
- **Por que `@RequestScoped`?** Cada requisição HTTP tem um token diferente. Se fosse `@ApplicationScoped` (uma instância para a aplicação inteira), os usuários se misturariam. O Quarkus cria um `CurrentUser` novo por requisição e o injeta até em Services `@ApplicationScoped` (através de um *proxy* do CDI — conceito da Aula 7).
- **Por que um bean separado?** A regra *"é o dono **ou** é ADMIN"* aparece em Users, Wallets, Transactions e Snapshots. Escrita em **um lugar só**, se a regra mudar, muda uma vez.

### 4.3. `AccessDeniedException` (403) — a 4ª exceção customizada

Seguindo a arquitetura da Aula 8:

| Exceção | HTTP | Quando |
| :--- | :--- | :--- |
| `BusinessException` | 400 | Regra de negócio violada |
| `ResourceNotFoundException` | 404 | Registro não existe |
| `ConflictException` | 409 | Unicidade / integridade |
| **`AccessDeniedException`** | **403** | **Está logado, mas o recurso não é dele** |

---

## 🔐 5. Camada ①: Anotações nos Resources

### 5.1. As anotações

| Anotação | Significado |
| :--- | :--- |
| `@PermitAll` | Rota **pública** (não precisa de token). |
| `@Authenticated` | Precisa de um token válido, de **qualquer** role. |
| `@RolesAllowed(Roles.ADMIN)` | Precisa de token **e** da role ADMIN. |

Uma anotação no **método** sobrescreve a da **classe**. Isso permite: "a classe inteira exige login, mas estes três métodos exigem ADMIN".

### 5.2. Negar por padrão (`deny-unannotated-endpoints`)

No `application.properties`:

```properties
quarkus.security.jaxrs.deny-unannotated-endpoints=true
```

Com isso, **qualquer endpoint sem anotação de segurança é bloqueado**. Imagine que daqui a 3 meses alguém crie um `ReportResource` e esqueça de proteger: sem essa linha, a rota nasce **aberta para a internet**; com ela, nasce **fechada** (403) e o erro aparece logo no primeiro teste.

> 💡 Segurança boa é aquela em que **esquecer** algo resulta em **bloqueio**, e não em **brecha**.

Por isso as rotas de `/auth` agora precisam declarar explicitamente que são públicas:

```java
@POST
@Path("/login")
@PermitAll
public Response login(@Valid LoginForm form) { ... }
```

### 5.3. Mapa de permissões da API

| Recurso | Leitura | Criação / Alteração / Exclusão |
| :--- | :--- | :--- |
| `/auth/register`, `/login`, `/verify-email`, `/resend-verification` | Público | Público |
| `/auth/me` | Logado | — |
| `/users` (listar, criar) | **ADMIN** | **ADMIN** |
| `/users/{id}` | Próprio usuário ou ADMIN | Próprio usuário ou ADMIN |
| `/users/{id}/role` | — | **ADMIN** (não pode alterar a própria role) |
| `/wallets` | Só as suas (ADMIN: todas) | Só as suas (ADMIN: todas). Transferir para outro dono: **ADMIN** |
| `/transactions` | Só das suas carteiras (ADMIN: todas) | Só nas suas carteiras (ADMIN: todas) |
| `/wallet-snapshots` | Só das suas carteiras (ADMIN: todas) | **ADMIN** (dado calculado pelo sistema) |
| `/assets`, `/daily-quotes` | Logado | **ADMIN** (catálogo global) |

**Por que `assets` e `daily-quotes` são só do ADMIN?** Eles não pertencem a nenhum usuário: o ativo `PETR4` é o mesmo para todos. Se qualquer usuário pudesse editar a cotação de `PETR4`, alteraria o patrimônio de **todas** as pessoas.

**Por que snapshots são só leitura para o usuário?** O snapshot é uma "foto" **calculada** pelo sistema (pelo futuro scheduler diário). Se o usuário pudesse editá-la, o histórico de rentabilidade deixaria de ser confiável.

### 5.4. Exemplo: `UserResource`

```java
@Path("/users")
@Authenticated                          // ← toda a classe exige login
public class UserResource {

    @GET
    @RolesAllowed(Roles.ADMIN)          // ← só ADMIN lista todos
    public Response findAll() { ... }

    @GET
    @Path("/{id}")                      // ← logado; o Service decide se é o dono
    public Response findById(@PathParam("id") UUID id) { ... }

    @PUT
    @Path("/{id}/role")
    @RolesAllowed(Roles.ADMIN)          // ← só ADMIN promove/rebaixa
    public Response updateRole(@PathParam("id") UUID id, @Valid RoleForm roleForm) { ... }
}
```

---

## 🧱 6. Camada ②: Regra de Dono nos Services

### 6.1. O padrão `findAccessibleXxx`

Todo acesso por ID passa por um método que **busca** e **valida o dono** de uma vez:

```java
// WalletService
public Wallet findAccessibleWallet(UUID id) {
    Wallet wallet = walletRepository.findByIdOptional(id)
            .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for ID: " + id));
    currentUser.checkAccess(wallet.users.id);   // ← dono ou ADMIN, senão 403
    return wallet;
}
```

E ele é reaproveitado por `getById`, `update` e `delete`. Se um método novo esquecer de usá-lo, o *code review* percebe: "por que este método chama `walletRepository.findById` direto?".

### 6.2. Recursos "filhos" herdam o dono do "pai"

Uma `Transaction` não tem dono próprio — ela pertence a quem é dono da **carteira**. Por isso o `TransactionService` reutiliza a regra do `WalletService`:

```java
@Transactional
public TransactionDTO create(TransactionForm form) {
    // Só pode lançar transação em carteira própria (ou qualquer uma, se ADMIN).
    Wallet wallet = walletService.findAccessibleWallet(form.walletId());
    ...
}

@Transactional
public TransactionDTO update(UUID id, TransactionForm form) {
    Transaction transaction = findAccessibleTransaction(id);   // dono da transação atual

    if (form.walletId() != null) {
        // Mover a transação também exige ser dono da carteira de DESTINO.
        transaction.wallet = walletService.findAccessibleWallet(form.walletId());
    }
    ...
}
```

> 🕵️ **Pegadinha clássica (pergunte ao aluno):** se validássemos só a transação **atual**, a Alice poderia mover a transação dela para a carteira do Bob, informando o `walletId` dele. Por isso validamos **origem e destino**.

### 6.3. Nunca confie no `userId` do cliente

**Antes:**
```java
public record WalletForm(
    @NotNull UUID userId,   // ← o cliente diz de quem é a carteira 😱
    @NotBlank String name
) {}
```

**Depois:**
```java
// WalletService.create
UUID ownerId = walletForm.userId() != null ? walletForm.userId() : currentUser.id();
currentUser.checkAccess(ownerId);   // usuário comum só pode criar para ele mesmo
```

- O front do usuário comum **nem envia** `userId`: o dono vem do token.
- O ADMIN pode informar `userId` para criar uma carteira em nome de outra pessoa.
- Transferir uma carteira existente para outro dono exige `requireAdmin(...)`.

### 6.4. ADMIN não pode alterar a própria role

```java
if (currentUser.id().equals(id)) {
    throw new BusinessException("You cannot change your own role.");
}
```

Sem essa trava, o único ADMIN poderia se rebaixar a USER por engano, e ninguém mais conseguiria administrar o sistema.

### 6.5. Troca de e-mail exige nova verificação

```java
if (!newEmail.equals(user.email)) {
    user.email = newEmail;
    authService.startEmailVerification(user);   // emailVerified = false + novo link
}
```

Sem isso, alguém poderia cadastrar e verificar `meu@email.com` e depois trocar para `presidente@empresa.com` — um e-mail que ele **não** controla, mas que o sistema trataria como verificado.

---

## 📋 7. Camada ③: Filtrar as Listagens

```java
// WalletService
public List<WalletDTO> getAllWallets() {
    List<Wallet> wallets = currentUser.isAdmin()
            ? walletRepository.listAll()
            : walletRepository.listByUserId(currentUser.id());
    return walletMapper.listToDTOs(wallets);
}
```

Novas consultas nos repositórios, usando a navegação de relacionamentos do Panache (Aula 4):

```java
// WalletRepository
public List<Wallet> listByUserId(UUID userId) {
    return list("users.id", userId);
}

// TransactionRepository — atravessa Transaction → Wallet → User
public List<Transaction> listByUserId(UUID userId) {
    return list("wallet.users.id", userId);
}
```

> ⚠️ **Nunca** busque tudo e filtre em Java (`listAll().stream().filter(...)`). Além de lento, um bug no filtro vaza dados. O filtro tem que estar **na consulta ao banco**.

---

## 🚨 8. Respostas 401 e 403 Padronizadas

O `GlobalExceptionHandler` da Aula 8 ganhou três novos interceptadores, mantendo o **mesmo JSON de erro** para o front:

```java
@ServerExceptionMapper
public Response handleAccessDenied(AccessDeniedException ex, @Context UriInfo uriInfo) { ... } // 403 (nossa regra de dono)

@ServerExceptionMapper
public Response handleForbidden(ForbiddenException ex, @Context UriInfo uriInfo) { ... }       // 403 (@RolesAllowed do Quarkus)

@ServerExceptionMapper({UnauthorizedException.class, AuthenticationFailedException.class})
public Response handleUnauthorized(RuntimeException ex, @Context UriInfo uriInfo) { ... }      // 401 (sem token / inválido / expirado)
```

E no `application.properties`:

```properties
quarkus.http.auth.proactive=false
```

**Por quê?** Por padrão, o Quarkus valida o token **antes** de a requisição chegar ao JAX-RS (autenticação *proativa*). Um token inválido seria rejeitado ali, com uma resposta 401 **vazia**, sem passar pelo nosso handler. Com `proactive=false`, o token é validado quando a rota realmente exige, e o erro segue o nosso padrão.

### 8.1. O 500 não pode vazar detalhes internos

**Antes:**
```java
"An unexpected internal error occurred: " + ex.getMessage()
// → "...could not execute statement [ERROR: duplicate key value violates unique constraint "uk_6dotkott2kjsp8vw4d0m25fb7"]"
```

**Depois:**
```java
LOG.errorf(ex, "Unexpected error on path %s", path);       // detalhe completo → log do servidor
... "An unexpected internal error occurred."                // mensagem genérica → cliente
```

> 💡 A mensagem interna é ótima para **você** depurar e ótima para o **atacante** mapear o banco. Ela vai para o log, que só a equipe acessa.

### 8.2. Exemplos de resposta

**Sem token (401):**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication is required. Send a valid token in the header 'Authorization: Bearer <token>'.",
  "path": "/wallets",
  "timestamp": "2026-11-06T20:23:08",
  "fieldErrors": null
}
```

**Carteira de outro usuário (403):**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to access this resource.",
  "path": "/wallets/f4390640-51ce-43bd-a52c-cf6ef7477167",
  "timestamp": "2026-11-06T20:23:50",
  "fieldErrors": null
}
```

### 8.3. 💡 Discussão: 403 ou 404 para recurso de outro usuário?

| Opção | Vantagem | Desvantagem |
| :--- | :--- | :--- |
| **403 Forbidden** (adotado) | Semântica clara, fácil de depurar e de tratar no front. | Confirma que aquele ID **existe**. |
| **404 Not Found** (GitHub faz isso em repositórios privados) | Não revela a existência do recurso. | Mais difícil de depurar. |

Como nossos IDs são **UUIDs aleatórios** (impossíveis de adivinhar), o risco de revelar a existência é baixo, e escolhemos a clareza do 403. Se os IDs fossem sequenciais (`1, 2, 3...`), o 404 seria a escolha mais segura.

---

## 🕵️ 9. Vazamentos de Informação no Fluxo de Auth

### 9.1. Enumeração de usuários

**Antes**, `POST /auth/resend-verification?email=fulano@x.com` respondia:
- `404 User not found` → o e-mail **não** tem conta
- `409 already verified` → o e-mail **tem** conta

Um atacante podia testar uma lista de e-mails e descobrir quem usa o sistema (útil para *phishing*). **Agora** a resposta é sempre a mesma:

```json
{ "message": "If this email is registered and not yet verified, a new verification link has been sent." }
```

O login já seguia essa ideia: **"Invalid email or password."** serve tanto para "e-mail não existe" quanto para "senha errada".

> 📝 O `/auth/register` ainda responde `409` quando o e-mail já existe. Isso é um *trade-off* aceito por quase todo site (o usuário precisa saber que já tem conta). A defesa nesse caso é a limitação de tentativas (*rate limiting*, ver seção 12).

### 9.2. Normalização de e-mail

```java
// UserRepository
public Optional<User> findByEmail(String email) {
    return find("email", email.trim().toLowerCase()).firstResultOptional();
}
```

Antes, `Ana@Mail.com` não era encontrado na checagem de duplicidade, tentava inserir, batia na constraint `unique` do banco e virava um **500**. Agora a normalização acontece no único ponto de busca.

---

## 👑 10. O Problema do Primeiro Administrador

Só um ADMIN pode promover alguém a ADMIN... então quem cria o primeiro? A resposta é: **quem faz o deploy**, por configuração.

```java
@ApplicationScoped
public class AdminSeeder {

    @ConfigProperty(name = "invest-tracker.admin.email")
    Optional<String> adminEmail;

    @ConfigProperty(name = "invest-tracker.admin.password")
    Optional<String> adminPassword;

    @Transactional
    void onStart(@Observes StartupEvent event) {      // roda quando a aplicação sobe
        if (userRepository.count("role", Roles.ADMIN) > 0) return;   // já existe ADMIN
        if (adminEmail.isEmpty() || adminPassword.isEmpty()) return; // nada configurado

        if (userRepository.findByEmail(email).isPresent()) {
            // NÃO promovemos conta existente (ver abaixo)
            return;
        }
        // cria o ADMIN com senha em BCrypt e e-mail já verificado
    }
}
```

```properties
# Produção: vem de variáveis de ambiente (vazio = não cria)
invest-tracker.admin.email=${ADMIN_EMAIL:}
invest-tracker.admin.password=${ADMIN_PASSWORD:}
# Dev: valores de conveniência (perfil %dev, Aula 5)
%dev.invest-tracker.admin.email=admin@invest-tracker.com
%dev.invest-tracker.admin.password=admin123
```

> 🕵️ **Por que não promover uma conta que já existe com aquele e-mail?** Porque qualquer pessoa poderia se cadastrar com `admin@invest-tracker.com` **antes** do primeiro deploy e ganharia o acesso de administrador de presente. O seeder só cria uma conta **nova**, com a senha que **nós** definimos.

---

## ⚙️ 11. Segredos e CORS por Ambiente (revisão da Aula 6)

```properties
quarkus.datasource.password=${DB_PASSWORD:postgres}
smallrye.jwt.sign.key.location=${JWT_PRIVATE_KEY_LOCATION:privateKey.pem}

quarkus.http.cors.origins=${CORS_ORIGINS:http://localhost:5173}
%dev.quarkus.http.cors.origins=/.*/
```

- A sintaxe `${VARIAVEL:padrão}` lê a variável de ambiente e, se ela não existir, usa o valor padrão (que serve **só para dev**).
- **A `privateKey.pem` commitada no repositório é apenas para desenvolvimento.** Quem tem essa chave consegue **gerar tokens de ADMIN válidos**. Em produção, gere um par novo no servidor e aponte `JWT_PRIVATE_KEY_LOCATION` para ele.
- **CORS:** em produção, só o domínio do front pode chamar a API pelo navegador. Em dev aceitamos qualquer origem para facilitar.

---

## 🧪 12. Roteiro de Testes Manuais (Swagger ou curl)

O Swagger (`/swagger-ui`) já tem o botão **Authorize**: cole o `accessToken` recebido no `/auth/login`.

| # | Cenário | Resultado esperado |
| :--- | :--- | :--- |
| 1 | `GET /wallets` sem token | **401** |
| 2 | `GET /wallets` com token inválido (`Bearer abc.def.ghi`) | **401** |
| 3 | Alice: `POST /wallets` com `{"name":"Alice wallet"}` (sem `userId`) | **201**, dona = Alice |
| 4 | Bob: `GET /wallets/{id da Alice}` | **403** |
| 5 | Bob: `GET /wallets` | **204** (Bob não tem carteiras; não vê a da Alice) |
| 6 | Bob: `POST /wallets` com `userId` da Alice | **403** |
| 7 | Alice: `PUT /wallets/{id}` com `userId` do Bob (transferir) | **403** "Only administrators..." |
| 8 | ADMIN: `GET /wallets/{id da Alice}` | **200** |
| 9 | Alice: `GET /users` | **403** |
| 10 | Alice: `GET /users/{id do Bob}` / `GET /users/{id dela}` | **403** / **200** |
| 11 | Alice: `PUT /users/{id dela}/role` com `{"role":"ADMIN"}` | **403** |
| 12 | ADMIN: `PUT /users/{id dele}/role` | **400** "You cannot change your own role." |
| 13 | Alice: `POST /assets` | **403** |
| 14 | Alice: `GET /assets` | **200/204** |
| 15 | Alice: `POST /wallet-snapshots` | **403** |
| 16 | `POST /auth/resend-verification?email=naoexiste@x.com` | **200** com mensagem genérica |

### 💡 Experimento revelador: a role "presa" no token

1. O ADMIN promove o Bob: `PUT /users/{bob}/role` com `{"role":"ADMIN"}` → 200.
2. Bob, **com o token antigo**, tenta `DELETE /users/{alice}` → **403!**
3. Bob faz login de novo e repete → **204**.

**Por quê?** A role está gravada **dentro do token** (claim `groups`), e o servidor não consulta o banco a cada requisição: confia no token assinado até ele expirar (24h). A mesma coisa acontece ao **rebaixar** ou **excluir** um usuário: o token antigo continua valendo até expirar.

É o principal *trade-off* do JWT: **performance** (não consulta o banco) × **revogação imediata**. As soluções profissionais são tokens de vida curta (ex.: 15 min) com *refresh token*, ou uma *blacklist* de tokens (por exemplo, no **Redis**, da Aula 4).

---

## 🗺️ 13. O Que Fica Para as Próximas Aulas

| Tema | Por que importa |
| :--- | :--- |
| **Testes automatizados de segurança** (`@QuarkusTest` + `@TestSecurity`) | Garantir que os cenários da seção 12 nunca regridam. |
| **Rate limiting** no `/auth/login` | Impede ataques de força bruta em senhas. |
| **Refresh token + expiração curta** | Resolve o problema da "role presa" no token. |
| **Scheduler de snapshots** | O job roda **fora** de uma requisição HTTP, ou seja, **sem JWT**! Ele deve usar os *repositories* diretamente ou métodos próprios, e não os métodos que dependem de `CurrentUser`. |

---

## 🧭 14. Roteiro de Condução da Aula (Sugestão de 4 Blocos)

1. **Ataque primeiro (20 min):** com o código antigo, peça ao aluno para "invadir" a API — apagar um usuário sem token, criar uma carteira em nome de outro trocando o `userId`. Sentir a brecha fixa o conceito.
2. **Conceitos (20 min):** AuthN × AuthZ, 401 × 403, o que tem dentro do JWT (abrir no jwt.io), por que confiar no token e não no corpo da requisição.
3. **Implementação (60 min):** `Roles` → `CurrentUser` → `AccessDeniedException` + handler → anotações nos Resources → `findAccessibleXxx` nos Services → filtros nas listagens → `deny-unannotated-endpoints`.
4. **Validação (20 min):** percorrer a tabela da seção 12 no Swagger com dois usuários e um ADMIN; terminar com o experimento da "role presa no token".

---

## 🚀 15. Conclusão

Com a Aula 9:
- **Nenhum endpoint fica aberto por acidente** (negar por padrão).
- Usuários comuns **só enxergam e alteram o que é deles**; o ADMIN administra tudo.
- A identidade vem **sempre do JWT**, nunca do corpo da requisição.
- Erros 401/403 seguem o **mesmo contrato JSON** das demais exceções, pronto para o front tratar.
- O sistema **não vaza** detalhes internos nem revela quais e-mails estão cadastrados.
- O backend está pronto para receber o **front-end** com segurança.
