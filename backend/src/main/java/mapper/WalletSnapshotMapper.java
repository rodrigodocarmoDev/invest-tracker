package mapper;

import dto.wallet.WalletDTO;
import dto.wallet.WalletForm;
import dto.walletsnapshot.WalletSnapshotDTO;
import dto.walletsnapshot.WalletSnapshotForm;
import model.Wallet;
import model.WalletSnapshot;

import java.util.List;

public class WalletSnapshotMapper {
    private final ModelMapper modelMapper = new ModelMapper();

    public WalletSnapshotDTO toDTO(WalletSnapshot walletSnapshot) {
        return modelMapper.map(walletSnapshot, WalletSnapshotDTO.class);
    }

    public List<WalletSnapshotDTO> listToDTOs (List<WalletSnapshot> walletSnapshots) {
        return walletSnapshots.stream().map((WalletSnapshot walletSnapshot) -> toDTO(walletSnapshot)).toList();
    }

    public WalletSnapshot formToModel(WalletSnapshotForm walletSnapshotForm) {
        return modelMapper.map(walletSnapshotForm, WalletSnapshot.class);
    }
}
