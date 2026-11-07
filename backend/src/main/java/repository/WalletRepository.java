package repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.Wallet;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class WalletRepository implements PanacheRepositoryBase<Wallet, UUID> {

    public List<Wallet> listByUserId(UUID userId) {
        return list("users.id", userId);
    }
}
