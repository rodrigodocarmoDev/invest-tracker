package repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.Transaction;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransactionRepository implements PanacheRepositoryBase<Transaction, UUID> {

    public List<Transaction> listByAssetId(UUID assetId) {
        return list("asset.id", assetId);
    }

    public List<Transaction> listByWalletId(UUID walletId) {
        return list("wallet.id", walletId);
    }

    /** Todas as transações das carteiras que pertencem ao usuário. */
    public List<Transaction> listByUserId(UUID userId) {
        return list("wallet.users.id", userId);
    }
}
