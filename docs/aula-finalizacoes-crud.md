# 🎓 Fechando o Ciclo do CRUD no Quarkus com Java 21

> **Projeto:** Invest-Tracker (Backend)  
> **Tecnologias:** Java 21, Quarkus 3.39+, Hibernate ORM com Panache (Repository Pattern), PostgreSQL, Jackson, Bean Validation, OpenAPI/Swagger.

---

## 🎯 Objetivo da Aula
Guiar os alunos na transição dos esqueletos iniciais do projeto para uma API REST profissional, robusta e padrão de mercado, fechando o ciclo do CRUD completo:
$$\text{Resource (Controller)} \longleftrightarrow \text{Service} \longleftrightarrow \text{Repository} \longleftrightarrow \text{Database}$$
com **tratamento centralizado de exceções**, **validações rigorosas**, **DTOs/Forms imutáveis com Records** e **persistência transacional saudável**.

---

## 📁 1. Arquitetura de Pastas: O que temos e o que falta criar?

Atualmente, o projeto possui apenas a estrutura mais elementar (`model`, `repository`, `service`, `resource`, `dto`, `mapper`). Para um projeto de nível corporativo e didaticamente exemplar, a estrutura recomendada de pastas é:

```text
backend/src/main/java/
├── config/              <-- [CRIAR] Configurações globais (OpenAPI/Swagger, CORS)
│   └── OpenApiConfig.java
├── dto/                 <-- [JÁ EXISTE] Subdividido por entidade
│   ├── asset/
│   ├── transaction/
│   ├── user/
│   │   ├── UserDTO.java
│   │   └── UserForm.java
│   └── wallet/
├── enums/               <-- [CRIAR] Tipos enumerados de domínio
│   ├── AssetType.java
│   └── OperationType.java
├── exception/           <-- [CRIAR] Tratamento profissional de erros
│   ├── custom/          <-- Exceções de negócio e domínio
│   │   ├── BusinessException.java
│   │   ├── ConflictException.java
│   │   └── ResourceNotFoundException.java
│   ├── dto/             <-- Estrutura JSON de resposta de erro
│   │   ├── ErrorResponseDTO.java
│   │   └── ValidationErrorDTO.java
│   └── handler/         <-- Mapeador global que intercepta erros
│       └── GlobalExceptionHandler.java
├── mapper/              <-- [JÁ EXISTE] Conversores Entity <-> DTO
│   └── UserMapper.java
├── model/               <-- [JÁ EXISTE] Entidades JPA/Hibernate
│   └── User.java
├── repository/          <-- [JÁ EXISTE] Acesso ao banco com Panache
│   └── UserRepository.java
├── resource/            <-- [JÁ EXISTE] Portas de entrada HTTP (REST)
│   └── UserResource.java
├── service/             <-- [JÁ EXISTE] Regras de negócio e transações
│   └── UserService.java
└── util/                <-- [CRIAR] Utilitários transversais (ex: criptografia)
    └── SecurityUtils.java
```

> **Nota sobre convenção de pacotes:**  
> Os pacotes atuais estão declarados na raiz (`package service;`, `package model;`). Em projetos corporativos, a convenção padrão é usar o domínio invertido (ex.: `package com.investtracker.service;`). Para a aula, você pode optar por manter os pacotes curtos para facilitar a digitação dos alunos, mas vale destacar essa convenção de mercado.

---

## ⚙️ 2. Configuração do Backend (`application.properties`)

**Arquivo:** `backend/src/main/resources/application.properties`

Configure a conexão com o banco e a documentação interativa:

```properties
# ==========================================
# Configuração do Banco de Dados PostgreSQL
# ==========================================
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=postgres
quarkus.datasource.password=postgres
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/invest_tracker

# Hibernate ORM & Panache
quarkus.hibernate-orm.database.generation=update
quarkus.hibernate-orm.log.sql=true
quarkus.hibernate-orm.format-sql=true

# ==========================================
# Documentação e Swagger UI
# ==========================================
quarkus.swagger-ui.always-include=true
quarkus.swagger-ui.path=/swagger-ui
quarkus.smallrye-openapi.path=/openapi

# Porta da aplicação
quarkus.http.port=8080
```

---

## 🚨 3. Tratamento de Exceções: Do Anti-pattern ao Padrão Corporativo

### ❌ O Problema Atual no Código
Atualmente, métodos dos Resources estão cheios de blocos repetitivos como este:
```java
try {
    return Response.ok(userService.getById(id)).build();
} catch (Exception e) {
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                   .entity("Error retrieving user").build();
}
```
**Por que isso é ruim?**
1. **Disfarça erros:** Se um usuário não existe, o sistema devolve `500 Internal Server Error` (erro de servidor) em vez de `404 Not Found`.
2. **Poluição de código:** Todo método vira uma repetição de `try/catch`.
3. **Respostas inconsistentes:** Mensagens de erro em texto puro, sem JSON padronizado com data, status ou campo inválido.

---

### ✅ A Solução: Arquitetura de Exceções em 3 Camadas

Vamos criar a pasta `backend/src/main/java/exception/` dividida em:
1. `dto/` (O formato JSON que o cliente receberá em caso de erro)
2. `custom/` (As exceções de negócio lançadas pelos Services)
3. `handler/` (O interceptador global do Quarkus)

#### Passo 3.1: DTOs de Erro (`exception/dto/`)

**Arquivo:** `exception/dto/ErrorResponseDTO.java`
Padroniza toda e qualquer resposta de erro da API:
```java
package exception.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponseDTO(
    int status,
    String error,
    String message,
    String path,
    LocalDateTime timestamp,
    List<ValidationErrorDTO> fieldErrors
) {
    // Construtor auxiliar para erros sem lista de campos
    public ErrorResponseDTO(int status, String error, String message, String path) {
        this(status, error, message, path, LocalDateTime.now(), null);
    }
}
```

**Arquivo:** `exception/dto/ValidationErrorDTO.java`
Detalha quais campos do formulário falharam no Bean Validation:
```java
package exception.dto;

public record ValidationErrorDTO(
    String field,
    String message
) {}
```

---

#### Passo 3.2: Exceções Customizadas (`exception/custom/`)

São exceções não-checadas (`RuntimeException`) com significado semântico:

**Arquivo:** `exception/custom/ResourceNotFoundException.java` *(HTTP 404)*
```java
package exception.custom;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

**Arquivo:** `exception/custom/ConflictException.java` *(HTTP 409 - Ex: e-mail ou ticker duplicado)*
```java
package exception.custom;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
```

**Arquivo:** `exception/custom/BusinessException.java` *(HTTP 400 ou 422 - Regra de negócio violada)*
```java
package exception.custom;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
```

---

#### Passo 3.3: Interceptador Global (`exception/handler/`)

No Quarkus REST, podemos usar a anotação moderna `@ServerExceptionMapper` para capturar exceções antes que cheguem ao cliente:

**Arquivo:** `exception/handler/GlobalExceptionHandler.java`
```java
package exception.handler;

import exception.custom.BusinessException;
import exception.custom.ConflictException;
import exception.custom.ResourceNotFoundException;
import exception.dto.ErrorResponseDTO;
import exception.dto.ValidationErrorDTO;
import io.quarkus.resteasy.reactive.server.ServerExceptionMapper;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class GlobalExceptionHandler {

    @ServerExceptionMapper
    public Response handleNotFound(ResourceNotFoundException ex, @Context UriInfo uriInfo) {
        ErrorResponseDTO error = new ErrorResponseDTO(
            Response.Status.NOT_FOUND.getStatusCode(),
            "Not Found",
            ex.getMessage(),
            uriInfo.getPath()
        );
        return Response.status(Response.Status.NOT_FOUND).entity(error).build();
    }

    @ServerExceptionMapper
    public Response handleConflict(ConflictException ex, @Context UriInfo uriInfo) {
        ErrorResponseDTO error = new ErrorResponseDTO(
            Response.Status.CONFLICT.getStatusCode(),
            "Conflict",
            ex.getMessage(),
            uriInfo.getPath()
        );
        return Response.status(Response.Status.CONFLICT).entity(error).build();
    }

    @ServerExceptionMapper
    public Response handleBusiness(BusinessException ex, @Context UriInfo uriInfo) {
        ErrorResponseDTO error = new ErrorResponseDTO(
            Response.Status.BAD_REQUEST.getStatusCode(),
            "Bad Request",
            ex.getMessage(),
            uriInfo.getPath()
        );
        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }

    // Captura erros de Bean Validation (@NotBlank, @Email, etc.)
    @ServerExceptionMapper
    public Response handleValidation(ConstraintViolationException ex, @Context UriInfo uriInfo) {
        List<ValidationErrorDTO> fieldErrors = ex.getConstraintViolations().stream()
            .map(cv -> {
                String fullPath = cv.getPropertyPath().toString();
                String field = fullPath.substring(fullPath.lastIndexOf('.') + 1);
                return new ValidationErrorDTO(field, cv.getMessage());
            })
            .collect(Collectors.toList());

        ErrorResponseDTO error = new ErrorResponseDTO(
            Response.Status.BAD_REQUEST.getStatusCode(),
            "Validation Error",
            "Erro de validação nos campos informados",
            uriInfo.getPath(),
            LocalDateTime.now(),
            fieldErrors
        );
        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }

    // Captura qualquer outro erro inesperado (Fallback 500)
    @ServerExceptionMapper
    public Response handleGeneric(Throwable ex, @Context UriInfo uriInfo) {
        ErrorResponseDTO error = new ErrorResponseDTO(
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "Internal Server Error",
            "Ocorreu um erro interno no servidor",
            uriInfo.getPath()
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }
}
```

> **Efeito prático no Resource:**  
> O código do Controller fica **100% livre de blocos `try/catch`**. O Service lança a exceção e o Quarkus entrega o JSON perfeito para o cliente com o código HTTP exato.

---

## 💾 4. Persistência Saudável no Panache: "Só chamo o persist e já era?"

### A Dúvida Crucial
Nos serviços atuais, existe o código:
```java
// UserService.java
public UserDTO create(UserForm userForm) {
    User user = userRepository.create(userForm); // <- NÃO EXISTE NO PANACHE!
    return userMapper.toDTO(user);
}
```

### Por que `repository.create(form)` estava errado?
1. **Violação de Camadas:** Repositório é responsável exclusivamente pela persistência e consulta de Entidades (`User`, `Wallet`). Ele **não deve** conhecer DTOs ou Forms da camada web.
2. **Métodos do Panache:** No Panache, a interface `PanacheRepositoryBase` fornece métodos como `persist()`, `findById()`, `listAll()`, `delete()`, `deleteById()`. Não há método `create()`.

### "É só chamar `repository.persist(entity)` e já era?"
**Não!** Se você apenas chamar `persist` sem validar, o banco de dados pode estourar um erro SQL feio (como violação de chave única ou constraint de chave estrangeira), gerando um `500 Internal Server Error`.

### 🛡️ O Ciclo dos 5 Passos de uma Persistência Saudável

Uma rotina de cadastro (`create`) profissional deve seguir rigidamente:

$$\text{1. Validar Regras} \longrightarrow \text{2. Mapear Form } \to \text{ Entity} \longrightarrow \text{3. Enriquecer Dados} \longrightarrow \text{4. Persistir} \longrightarrow \text{5. Retornar DTO}$$

```java
@Transactional
public UserDTO create(UserForm form) {
    // 1. Validação de Regra de Negócio (ex: e-mail único)
    userRepository.findByEmail(form.email()).ifPresent(user -> {
        throw new ConflictException("Já existe um usuário cadastrado com o e-mail: " + form.email());
    });

    // 2. Mapeamento de Entrada (Form -> Entity)
    User user = userMapper.toEntity(form);

    // 3. Enriquecimento / Regras de Domínio
    // (Ex: Criptografia de senha, datas, status padrão)
    user.passwordHash = SecurityUtils.hashPassword(form.password());

    // 4. Persistência Transacional no Banco
    userRepository.persist(user);

    // 5. Mapeamento de Saída (Entity -> DTO seguro)
    return userMapper.toDTO(user);
}
```

---

### 🔄 Como Fazer um `update` Saudável
No Panache/Hibernate, atualizar uma entidade **não é criar um novo objeto**. O Hibernate rastreia entidades gerenciadas (*Dirty Checking*):

```java
@Transactional
public UserDTO update(UUID id, UserForm form) {
    // 1. Buscar ou lançar 404
    User user = userRepository.findByIdOptional(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado para o ID: " + id));

    // 2. Validar se o novo e-mail já não pertence a OUTRO usuário
    userRepository.findByEmail(form.email()).ifPresent(existingUser -> {
        if (!existingUser.id.equals(id)) {
            throw new ConflictException("O e-mail informado já está em uso por outro usuário");
        }
    });

    // 3. Atualizar apenas os campos mutáveis na entidade existente
    user.email = form.email();
    if (form.password() != null && !form.password().isBlank()) {
        user.passwordHash = SecurityUtils.hashPassword(form.password());
    }

    // O Panache/Hibernate sincroniza automaticamente ao término do método @Transactional!
    return userMapper.toDTO(user);
}
```

---

### 🗑️ Como Fazer um `delete` Saudável
```java
@Transactional
public void delete(UUID id) {
    // Verifica se o registro existe antes de tentar deletar
    boolean deleted = userRepository.deleteById(id);
    if (!deleted) {
        throw new ResourceNotFoundException("Usuário não encontrado para o ID: " + id);
    }
}
```

---

## 🏗️ 5. Implementação Completa de Gabarito: Módulo `User`

### 5.1. Repositório: `repository/UserRepository.java`
```java
package repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.User;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, UUID> {

    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }
}
```

---

### 5.2. DTOs Imutáveis com Records (Java 21): `dto/user/`

**`dto/user/UserForm.java`**:
```java
package dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserForm(
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    String email,

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, message = "A senha deve conter no mínimo 6 caracteres")
    String password
) {}
```

**`dto/user/UserDTO.java`**:
```java
package dto.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDTO(
    UUID id,
    String email,
    LocalDateTime createdAt
) {}
```

---

### 5.3. Mapper: `mapper/UserMapper.java`
```java
package mapper;

import dto.user.UserDTO;
import dto.user.UserForm;
import jakarta.enterprise.context.ApplicationScoped;
import model.User;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserMapper {

    public User toEntity(UserForm form) {
        if (form == null) return null;
        User user = new User();
        user.email = form.email();
        return user;
    }

    public UserDTO toDTO(User user) {
        if (user == null) return null;
        return new UserDTO(user.id, user.email, user.createdAt);
    }

    public List<UserDTO> listToDTOs(List<User> users) {
        return users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
```

---

### 5.4. Resource Limpo (Zero `try/catch`): `resource/UserResource.java`
```java
package resource;

import dto.user.UserDTO;
import dto.user.UserForm;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import service.UserService;

import java.util.List;
import java.util.UUID;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Usuários", description = "Endpoints de gerenciamento de usuários")
public class UserResource {

    @Inject
    UserService userService;

    @GET
    @Operation(summary = "Lista todos os usuários")
    public Response findAll() {
        List<UserDTO> users = userService.getAllUsers();
        return Response.ok(users).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca usuário por ID")
    public Response findById(@PathParam("id") UUID id) {
        UserDTO user = userService.getById(id);
        return Response.ok(user).build();
    }

    @POST
    @Operation(summary = "Cadastra um novo usuário")
    public Response create(@Valid UserForm userForm) {
        UserDTO created = userService.create(userForm);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualiza os dados de um usuário")
    public Response update(@PathParam("id") UUID id, @Valid UserForm userForm) {
        UserDTO updated = userService.update(id, userForm);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Exclui um usuário")
    public Response delete(@PathParam("id") UUID id) {
        userService.delete(id);
        return Response.noContent().build();
    }
}
```

---

## 🧭 6. Roteiro de Condução da Aula (Sugestão de 4 Blocos)

| Bloco | Duração | Tema | Atividade Prática |
| :--- | :--- | :--- | :--- |
| **Bloco 1** | 20 min | **A Arquitetura e os Erros Atuais** | Abrir o projeto, mostrar os `try/catch` no Resource, o `repository.create()` inválido e discutir o porquê de cada problema. Criar as novas pastas (`exception`, `enums`, `config`). |
| **Bloco 2** | 30 min | **O Módulo de Exceções Perfeito** | Criar `ErrorResponseDTO`, as 3 exceções customizadas (`NotFound`, `Conflict`, `Business`) e o `GlobalExceptionHandler`. |
| **Bloco 3** | 45 min | **O Ciclo do CRUD de Usuários** | Implementar `UserRepository`, `UserForm`, `UserDTO`, `UserMapper`, `UserService` e `UserResource`. Rodar `./mvnw quarkus:dev` e abrir o Swagger UI. |
| **Bloco 4** | Restante | **Desafio Prático dos Alunos** | Alunos replicam a arquitetura para o CRUD de **Ativos (`Asset`)** ou **Carteiras (`Wallet`)**. |
