# 📝 Roteiro Rápido — Aula 9: Autenticação e Autorização

> Lembrete pessoal. O gabarito está na branch `gabarito`; a explicação completa está em `aula-9.md`.
> Ponto de partida do aluno: a `main` (CRUD + exceções, **sem nada de auth**).

---

## 0. Abertura (10 min)
- Com a API aberta, **"invadir"**: `DELETE /users/{id}` sem token, criar carteira trocando o `userId`.
- Conceitos: **autenticação** (quem é você? → 401) × **autorização** (pode fazer isso? → 403).
- Analogia do prédio: cadastro → crachá (JWT) → catraca (roles) → porta da sala (dono).

---

## PARTE 1 — Autenticação (login e JWT)

### 1. `pom.xml` — adicionar as extensões
- `quarkus-smallrye-jwt` → **valida** tokens
- `quarkus-smallrye-jwt-build` → **gera/assina** tokens
- `quarkus-elytron-security-common` → `BcryptUtil` (hash de senha)
- `quarkus-mailer` → envio de e-mail
- 💬 "Cada extensão sabe ler um conjunto de anotações."

### 2. Gerar as chaves RSA → `src/main/resources/`
```bash
openssl genrsa -out rsaPrivateKey.pem 2048
openssl rsa -pubout -in rsaPrivateKey.pem -out publicKey.pem
openssl pkcs8 -topk8 -nocrypt -inform pem -in rsaPrivateKey.pem -outform pem -out privateKey.pem
```
- 💬 Privada **assina**, pública **confere**. Quem tem a privada fabrica token de ADMIN.

### 3. `application.properties` — seções 4 (JWT) e 5 (Mailer)
- `mp.jwt.verify.issuer`, `smallrye.jwt.sign.key.location`, `mp.jwt.verify.publickey.location`
- `quarkus.mailer.mock=true` (e-mail sai no console) e `invest-tracker.app.url`
- 💬 Explicar `@ConfigProperty(name = ...)` e a ordem: `-D` > env var > properties > default.

### 4. `model/User.java` — novos campos
- `name`, `emailVerified`, `verificationToken`, `verificationTokenExpiresAt`, `role = "USER"`

### 5. `util/SecurityUtils.java`
- `hashPassword` / `checkPassword` com BCrypt.
- 💬 Hash é de mão única, tem sal aleatório e é lento de propósito.

### 6. DTOs em `dto/auth/`
- `RegisterForm`, `LoginForm` (com `@NotBlank`, `@Email`, `@Size`)
- `AuthResponseDTO` (`accessToken`, `tokenType = "Bearer"`, `expiresInSeconds`, `user`)
- `MessageResponseDTO`

### 7. `repository/UserRepository.java`
- `findByEmail` (com `trim().toLowerCase()`!) e `findByVerificationToken`

### 8. `service/TokenService.java`
- `Jwt.issuer().upn().subject(id).groups(role).claim(...).expiresIn(24h).sign()`
- 💬 `sub` = quem é, `groups` = roles. Abrir um token no **jwt.io**: é assinado, **não** criptografado.

### 9. `service/EmailService.java`
- Monta o link `/auth/verify-email?token=...` e envia com `Mailer`.

### 10. `service/AuthService.java`
- `register` → hash da senha, role `USER` definida pelo servidor, `startEmailVerification`
- `verifyEmail` → valida o token e a expiração, marca como verificado (💬 *dirty checking*, sem `persist`)
- `resendVerificationEmail` → **sempre a mesma resposta** (💬 enumeração de usuários)
- `login` → mesma mensagem para e-mail ou senha errados, bloqueia e-mail não verificado, gera o token
- `startEmailVerification(user)` → reaproveitado em 3 lugares

### 11. `resource/AuthResource.java`
- `/register`, `/login`, `/verify-email`, `/resend-verification` → `@PermitAll`
- `/me` → `@Authenticated` (fazer depois do `CurrentUser`, passo 13)

### ✅ Checkpoint 1 (Swagger)
Cadastrar → copiar o link do console → verificar → logar → copiar o token → **Authorize** no Swagger.

---

## PARTE 2 — Autorização (quem pode o quê)

### 12. `security/Roles.java`
- Constantes `USER` e `ADMIN`. 💬 Por que não enum? Anotação só aceita constante de compilação.

### 13. `security/CurrentUser.java` (`@RequestScoped`)
- `id()` (do `sub`), `isAdmin()` (dos `groups`), `checkAccess(ownerId)`, `requireAdmin(msg)`
- 💬 Por que `@RequestScoped`? (uma instância por requisição; o proxy do CDI)
- 💬 **A identidade vem do token, nunca do JSON.**

### 14. Exceção + handler
- `exception/custom/AccessDeniedException.java` → 403
- `GlobalExceptionHandler`: `handleAccessDenied` (403), `handleForbidden` (403 do `@RolesAllowed`), `handleUnauthorized` (401 + header `WWW-Authenticate`)
- Corrigir o **500**: logar `ex` e devolver uma mensagem genérica (💬 não vazar SQL)

### 15. Anotar os Resources
| Resource | Classe | Métodos |
|---|---|---|
| `UserResource` | `@Authenticated` | `findAll`, `create`, `updateRole` → `@RolesAllowed(ADMIN)` |
| `WalletResource`, `TransactionResource` | `@Authenticated` | — (o dono é checado no Service) |
| `WalletSnapshotResource`, `AssetResource`, `DailyQuoteResource` | `@Authenticated` | POST/PUT/DELETE → `@RolesAllowed(ADMIN)` |
- 💬 A anotação do método vence a da classe.

### 16. Regra de dono nos Services
- `WalletService.findAccessibleWallet(id)` → busca e chama `checkAccess(wallet.users.id)` (público, reaproveitado)
- `TransactionService` → usa `findAccessibleWallet` no create e no update (💬 validar a carteira de **destino** também!)
- `WalletSnapshotService.getById` → `checkAccess`
- `UserService` → `findAccessibleUser` em get/update/delete; `updateRole` (não pode alterar a própria role); troca de e-mail chama `startEmailVerification`

### 17. Filtrar listagens
- Repositórios: `WalletRepository.listByUserId` (`"users.id"`), `TransactionRepository` e `WalletSnapshotRepository.listByUserId` (`"wallet.users.id"`)
- Services: `isAdmin() ? listAll() : listByUserId(currentUser.id())`
- 💬 Nunca usar `listAll().stream().filter(...)`.

### 18. `WalletForm` sem `userId` obrigatório
- Tirar o `@NotNull`. No `create`: `userId != null ? userId : currentUser.id()` + `checkAccess`.
- Transferir para outro dono no `update` → `requireAdmin`.

### 19. `dto/user/RoleForm.java` + `PUT /users/{id}/role`
- `@Pattern("^(USER|ADMIN)$")`

### 20. `security/AdminSeeder.java`
- `@Observes StartupEvent` + `@Transactional` + `@ConfigProperty Optional<String>`
- Cria só se não houver ADMIN e **nunca promove conta existente** (💬 por quê?)

### 21. `application.properties` — seção 8 + segredos
- `quarkus.security.jaxrs.deny-unannotated-endpoints=true` (💬 negar por padrão)
- `quarkus.http.auth.proactive=false` (💬 401 passa pelo nosso handler)
- `invest-tracker.admin.email/password` com `${ADMIN_EMAIL:}` + valores `%dev.`
- `${DB_PASSWORD:postgres}`, `${JWT_PRIVATE_KEY_LOCATION:...}`, CORS `${CORS_ORIGINS:...}` + `%dev...=/.*/`

---

## ✅ Checkpoint 2 — Testes no Swagger (Alice, Bob e Admin)
1. Sem token → **401** · token inválido → **401**
2. Alice cria carteira sem `userId` → **201**
3. Bob acessa a carteira da Alice → **403** · Bob lista → só as dele
4. Bob cria carteira com o `userId` da Alice → **403** · Alice transfere para o Bob → **403**
5. Admin vê/edita a carteira da Alice → **200**
6. Alice: `GET /users` → **403** · `POST /assets` → **403** · `GET /assets` → **200**
7. Admin altera a própria role → **400**
8. 🎯 **Experimento final:** promover o Bob a ADMIN → com o token antigo ainda dá **403** → novo login → funciona.
   💬 A role fica dentro do token até ele vencer (gancho para refresh token).

---

## Fechamento (5 min)
- Recapitular as 3 camadas: **anotação** (andar) → **Service** (sala) → **consulta filtrada** (lista).
- Lição de casa: explicar com as próprias palavras a diferença entre 401 e 403, e por que o `userId` não vem do JSON.
