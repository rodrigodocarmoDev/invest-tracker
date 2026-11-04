package dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterForm(
    String name,

    @NotBlank(message = "Email is mandatory.")
    @Email(message = "Invalid email format.")
    String email,

    @NotBlank(message = "Password is mandatory.")
    @Size(min = 6, message = "Password must have at least 6 characters.")
    String password
) {}
