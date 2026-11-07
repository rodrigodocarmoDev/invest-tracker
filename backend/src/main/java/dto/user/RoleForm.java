package dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RoleForm(
    @NotBlank(message = "Role is mandatory.")
    @Pattern(regexp = "^(USER|ADMIN)$", message = "Role must be USER or ADMIN.")
    String role
) {}
