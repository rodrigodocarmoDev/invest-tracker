package repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.WalletSnapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class WalletSnapshotRepository implements PanacheRepositoryBase<WalletSnapshot, UUID> {

    public List<WalletSnapshot> listByWalletId(UUID walletId) {
        return list("wallet.id", walletId);
    }

    /** Todos os snapshots das carteiras que pertencem ao usuário. */
    public List<WalletSnapshot> listByUserId(UUID userId) {
        return list("wallet.users.id", userId);
    }

    public Optional<WalletSnapshot> findByWalletAndDate(UUID walletId, LocalDate snapshotDate) {
        return find("wallet.id = ?1 and snapshotDate = ?2", walletId, snapshotDate).firstResultOptional();
    }
}
