package service;

import dto.walletsnapshot.WalletSnapshotDTO;
import dto.walletsnapshot.WalletSnapshotForm;
import jakarta.inject.Inject;
import mapper.WalletSnapshotMapper;
import model.WalletSnapshot;
import repository.WalletSnapshotRepository;
import java.util.List;
import java.util.UUID;

public class WalletSnapshotService {

    @Inject
    WalletSnapshotRepository walletSnapshotRepository;
    @Inject
    WalletSnapshotMapper walletSnapshotMapper;

    public List<WalletSnapshotDTO> getAllWalletSnapshots() {
        List<WalletSnapshot> walletSnapshots = walletSnapshotRepository.listAll();
        return walletSnapshotMapper.listToDTOs(walletSnapshots);
    }

    public WalletSnapshotDTO getById(UUID id) {
        WalletSnapshot walletSnapshot = walletSnapshotRepository.findById(id);
        return walletSnapshotMapper.toDTO(walletSnapshot);
    }

    //@Transactional
    public WalletSnapshotDTO create(WalletSnapshotForm walletSnapshotForm) {
        WalletSnapshot walletSnapshot = walletSnapshotRepository.create(walletSnapshotForm);
        return walletSnapshotMapper.toDTO(walletSnapshot);
    }

    //@Transactional
    public WalletSnapshotDTO update(UUID id, WalletSnapshotForm walletSnapshotForm) {
        WalletSnapshot walletSnapshot = walletSnapshotRepository.findById(id);
        return walletSnapshotMapper.toDTO(walletSnapshot);
    }

    //@Transactional
    public void delete(UUID id) {
        walletSnapshotRepository.deleteById(id);
    }

}
