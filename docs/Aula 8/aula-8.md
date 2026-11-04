# 🎓 Aula 8: Profissionalizando o Tratamento de Exceções e Refatoração da Arquitetura

> **Projeto:** Invest-Tracker (Backend)  
> **Tecnologias:** Java 21, Quarkus 3.39+, Hibernate Panache, Bean Validation, `@ServerExceptionMapper`.  
> **Arquivo PDF:** [`Aula 8 - Profissionalizando o Tratamento de Excecoes e Refatoracao da Arquitetura.pdf`](file:///C:/Users/lucca/projects/invest-tracker/docs/Aula%208/Aula%208%20-%20Profissionalizando%20o%20Tratamento%20de%20Excecoes%20e%20Refatoracao%20da%20Arquitetura.pdf)

---

## 🎯 1. Objetivo da Aula

Consolidar a transição da aplicação **Invest-Tracker** para o padrão corporativo de mercado:
1. **Eliminar os blocos `try/catch` manuais** nos Resources (Controllers).
2. **Criar a arquitetura de exceções em 3 camadas** (`custom/`, `dto/`, `handler/`).
3. **Adotar DTOs e Forms imutáveis com Records do Java 21** e validações rigorosas com Bean Validation.
4. **Padronizar e proteger as regras de negócio nos Services** com transações (`@Transactional`) e CDI (`@ApplicationScoped`).
5. **Garantir a compilação com 100% de sucesso** via Maven.

---

## 🔍 2. O Diagnóstico: O que havia de errado no código anterior?

| Problema Identificado | Impacto no Sistema | Solução Corporativa Adotada |
| :--- | :--- | :--- |
| **`try/catch` genérico em todos os Resources** | Capturava `Exception` e retornava `500 Internal Server Error` mesmo para recursos inexistentes (404) ou conflitos de unicidade (409). | **Remoção total dos try/catch**. As exceções agora sobem livremente até o interceptador global. |
| **Anotações de validação incorretas nos Forms** | Uso de `@NotBlank` em `LocalDateTime`, `BigDecimal` e classes de modelo (que só aceitam Strings), quebrando o Bean Validation. | Uso correto de `@NotNull`, `@Positive`, `@PositiveOrZero` e `UUID`. |
| **Falta de CDI (`@ApplicationScoped`) nos Services** | Os serviços não eram gerenciados pelo container Quarkus Arc, impedindo a injeção via `@Inject`. | Adicionada a anotação `@ApplicationScoped` a todos os serviços e mappers. |
| **Dependência fantasma de `ModelMapper`** | Tentativa de instanciar `new ModelMapper()` sem a dependência no `pom.xml`, impedindo o Maven de compilar. | Mapeamento direto de alta performance usando construtores e Java 21 Records. |

---

## 🏛️ 3. A Nova Arquitetura de Exceções em 3 Camadas

A estrutura foi organizada sob o pacote `backend/src/main/java/exception/`:

```text
backend/src/main/java/exception/
├── custom/                 <-- 1. Exceções Semânticas
│   ├── ResourceNotFoundException.java (HTTP 404)
│   ├── ConflictException.java         (HTTP 409)
│   └── BusinessException.java         (HTTP 400)
├── dto/                    <-- 2. Contratos Padronizados
│   ├── ErrorResponseDTO.java
│   └── ValidationErrorDTO.java
└── handler/                <-- 3. Interceptador Reativo
    └── GlobalExceptionHandler.java
```

### 3.1. As Exceções Customizadas (`custom/`)
- **`ResourceNotFoundException`** (HTTP 404): Disparada quando um recurso buscado por ID não é encontrado.
- **`ConflictException`** (HTTP 409): Disparada para violações de chave única (e-mail, ticker ou cotação na mesma data) ou tentativa de exclusão de ativo/carteira com histórico associado.
- **`BusinessException`** (HTTP 400): Disparada quando uma regra de negócio financeira é violada.

### 3.2. Os DTOs de Erro (`dto/`)
- **`ErrorResponseDTO`**: Objeto imutável contendo `status`, `error`, `message`, `path`, `timestamp` e `fieldErrors`.
- **`ValidationErrorDTO`**: Objeto que mapeia o campo inválido e a mensagem da regra de validação que falhou.

### 3.3. O Interceptador Global com `@ServerExceptionMapper` (`handler/`)
No Quarkus REST, o `GlobalExceptionHandler` captura automaticamente as exceções antes que cheguem ao cliente, convertendo-as em JSON e no status HTTP exato.

### 3.4. 💡 Dúvida Crucial: Se o `persist()` falhar no banco, quem dá o throw? O Papel do Hibernate e do `Throwable`
Em métodos como `AssetService.create()`, temos:
```java
Asset asset = assetMapper.formToModel(assetForm);
assetRepository.persist(asset);
return assetMapper.toDTO(asset);
```
*Se não há nenhum `throw` escrito após o `persist`, como isso cai no `GlobalExceptionHandler`?*

1. **Caminho Feliz (Sucesso):** Não há `throw` algum! O comando SQL é enviado ao PostgreSQL, a execução passa normalmente para o `return assetMapper.toDTO(asset)`, o Quarkus confirma o commit da transação `@Transactional` e o Controller responde **HTTP 201 Created**.
2. **Caminho de Falha de Infraestrutura (Quem dá o throw?):** Caso o banco PostgreSQL esteja fora do ar, ocorra timeout de conexão, estouro de caracteres ou violação de integridade no banco, **o desenvolvedor não precisa escrever throw com as próprias mãos**. O próprio ecossistema do **Hibernate, Panache e Driver JDBC** lança internamente uma exceção de runtime (como `PersistenceException`, `SQLException` ou `DataException`).
3. **A Rede de Proteção do `Throwable`:** No Java, `Throwable` é a classe-mãe raiz de todas as exceções e erros da JVM (`Throwable ➜ Exception ➜ RuntimeException`). Ao declarar `handleGeneric(Throwable ex)` com `@ServerExceptionMapper`, criamos uma **rede de segurança universal (fallback)**. Qualquer erro imprevisto disparado pelo Hibernate ou pelo sistema que não seja uma exceção customizada de negócio cai nessa rede e é devolvido como **HTTP 500 Internal Server Error** em JSON padronizado, impedindo a aplicação de derrubar a JVM ou expor stacktraces sensíveis.

---

## 🛠️ 4. Como Ficou o Código Refatorado

### 4.1. Exemplo de Resource Limpo (`AssetResource.java`)
```java
@Path("/assets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssetResource {

    @Inject
    AssetService assetService;

    @GET
    public Response findAll() {
        var assets = assetService.getAllAssets();
        return assets.isEmpty() ? Response.noContent().build() : Response.ok(assets).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") UUID id) {
        return Response.ok(assetService.getById(id)).build();
    }

    @POST
    public Response create(@Valid AssetForm assetForm) {
        return Response.status(Response.Status.CREATED).entity(assetService.create(assetForm)).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") UUID id, @Valid AssetForm assetForm) {
        return Response.ok(assetService.update(id, assetForm)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        assetService.delete(id);
        return Response.noContent().build();
    }
}
```

### 4.2. Exemplo de Service Protegido (`AssetService.java`)
```java
@ApplicationScoped
public class AssetService {

    @Inject AssetRepository assetRepository;
    @Inject TransactionRepository transactionRepository;
    @Inject DailyQuoteRepository dailyQuoteRepository;
    @Inject AssetMapper assetMapper;

    public AssetDTO getById(UUID id) {
        Asset asset = assetRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found for ID: " + id));
        return assetMapper.toDTO(asset);
    }

    @Transactional
    public AssetDTO create(AssetForm assetForm) {
        assetRepository.findByTicker(assetForm.ticker()).ifPresent(existing -> {
            throw new ConflictException("Asset with ticker '" + assetForm.ticker() + "' already exists.");
        });

        Asset asset = assetMapper.formToModel(assetForm);
        assetRepository.persist(asset);
        return assetMapper.toDTO(asset);
    }

    @Transactional
    public void delete(UUID id) {
        Asset asset = assetRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found for ID: " + id));

        if (!transactionRepository.listByAssetId(id).isEmpty()) {
            throw new ConflictException("Not possible to delete asset with registered transactions.");
        }

        if (!dailyQuoteRepository.listByAssetId(id).isEmpty()) {
            throw new ConflictException("Not possible to delete asset with registered daily quotes.");
        }

        assetRepository.delete(asset);
    }
}
```

---

## 📊 5. Payloads de Resposta Recebidos pelo Cliente

### Recurso Não Encontrado (HTTP 404):
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Asset not found for ID: 9c8e1e7f-71b3-4f9e-a0e2-b7b51bfa70d1",
  "path": "/assets/9c8e1e7f-71b3-4f9e-a0e2-b7b51bfa70d1",
  "timestamp": "2026-11-04T17:16:00",
  "fieldErrors": null
}
```

### Validação de Campos Rejeitados (HTTP 400):
```json
{
  "status": 400,
  "error": "Validation Error",
  "message": "Validation error on submitted fields",
  "path": "/users",
  "timestamp": "2026-11-04T17:16:00",
  "fieldErrors": [
    {
      "field": "email",
      "message": "Invalid email format."
    },
    {
      "field": "password",
      "message": "Password must have at least 6 characters."
    }
  ]
}
```

### Conflito de Regra ou Integridade (HTTP 409):
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Not possible to delete asset with registered transactions.",
  "path": "/assets/8d7c2a11-5463-4411-9a99-b132890ef011",
  "timestamp": "2026-11-04T17:16:00",
  "fieldErrors": null
}
```

---

## 🚀 6. Conclusão

Com a conclusão da Aula 8:
- O backend compila sem erros via Maven (`BUILD SUCCESS`).
- O código está pronto para receber documentação OpenAPI / Swagger interativa.
- A aplicação oferece feedback seguro e semântico para qualquer cliente HTTP.
