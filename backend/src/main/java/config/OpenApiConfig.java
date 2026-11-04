package config;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

@OpenAPIDefinition(
    info = @Info(
        title = "Invest-Tracker API",
        version = "1.0.0",
        description = "API REST corporativa para gerenciamento de carteiras, ativos e evolução patrimonial.",
        contact = @Contact(name = "Equipe Invest-Tracker")
    ),
    security = @SecurityRequirement(name = "jwtAuth")
)
@SecurityScheme(
    securitySchemeName = "jwtAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Cole o token JWT obtido no endpoint /auth/login (ex: eyJhbGciOiJSUzI1NiIs...)"
)
public class OpenApiConfig extends Application {
}
