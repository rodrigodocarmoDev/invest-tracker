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

    public Optional<WalletSnapshot> findByWalletAndDate(UUID walletId, LocalDate snapshotDate) {
        return find("wallet.id = ?1 and snapshotDate = ?2", walletId, snapshotDate).firstResultOptional();
    }
}
