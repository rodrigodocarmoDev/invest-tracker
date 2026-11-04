package mapper;

import dto.wallet.WalletDTO;
import jakarta.enterprise.context.ApplicationScoped;
import model.Wallet;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class WalletMapper {

    public WalletDTO toDTO(Wallet wallet) {
        if (wallet == null) return null;
        UUID userId = wallet.users != null ? wallet.users.id : null;
        return new WalletDTO(wallet.id, userId, wallet.name, wallet.createdAt);
    }

    public List<WalletDTO> listToDTOs(List<Wallet> wallets) {
        if (wallets == null) return List.of();
        return wallets.stream().map(this::toDTO).toList();
    }
}
