package mapper;

import dto.transaction.TransactionDTO;
import jakarta.enterprise.context.ApplicationScoped;
import model.Transaction;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransactionMapper {

    public TransactionDTO toDTO(Transaction transaction) {
        if (transaction == null) return null;
        UUID walletId = transaction.wallet != null ? transaction.wallet.id : null;
        String walletName = transaction.wallet != null ? transaction.wallet.name : null;
        UUID assetId = transaction.asset != null ? transaction.asset.id : null;
        String assetTicker = transaction.asset != null ? transaction.asset.ticker : null;

        return new TransactionDTO(
            transaction.id,
            walletId,
            walletName,
            assetId,
            assetTicker,
            transaction.operationType,
            transaction.quantity,
            transaction.unitPrice,
            transaction.fees
        );
    }

    public List<TransactionDTO> listToDTOs(List<Transaction> transactions) {
        if (transactions == null) return List.of();
        return transactions.stream().map(this::toDTO).toList();
    }
}
