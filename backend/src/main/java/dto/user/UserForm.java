package dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserForm (
        @NotBlank(message="Ticker is mandatory.")
        @Size(min=2, max=10, message="Ticker must have between 2 and 10 characters")
        String ticker,
        @NotBlank(message="Type is mandatory.")
        String type,
        @NotBlank(message="Name is mandatory.")
        String name
){}
