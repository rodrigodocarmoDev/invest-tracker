package mapper;

import dto.walletsnapshot.WalletSnapshotDTO;
import jakarta.enterprise.context.ApplicationScoped;
import model.WalletSnapshot;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class WalletSnapshotMapper {

    public WalletSnapshotDTO toDTO(WalletSnapshot snapshot) {
        if (snapshot == null) return null;
        UUID walletId = snapshot.wallet != null ? snapshot.wallet.id : null;
        String walletName = snapshot.wallet != null ? snapshot.wallet.name : null;

        return new WalletSnapshotDTO(
            snapshot.id,
            walletId,
            walletName,
            snapshot.snapshotDate,
            snapshot.totalValue,
            snapshot.totalInvested
        );
    }

    public List<WalletSnapshotDTO> listToDTOs(List<WalletSnapshot> snapshots) {
        if (snapshots == null) return List.of();
        return snapshots.stream().map(this::toDTO).toList();
    }
}
