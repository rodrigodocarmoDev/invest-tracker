package dto.wallet;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * @param userId Opcional. Se omitido, a carteira pertence ao usuário logado (vem do JWT).
 *               Apenas ADMIN pode informar o ID de outro usuário.
 */
public record WalletForm(
    UUID userId,

    @NotBlank(message = "Wallet name is mandatory.")
    String name
) {}
