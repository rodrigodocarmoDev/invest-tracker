package service;

import dto.walletsnapshot.WalletSnapshotDTO;
import dto.walletsnapshot.WalletSnapshotForm;
import exception.custom.ConflictException;
import exception.custom.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import mapper.WalletSnapshotMapper;
import model.Wallet;
import model.WalletSnapshot;
import repository.WalletRepository;
import repository.WalletSnapshotRepository;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class WalletSnapshotService {

    @Inject
    WalletSnapshotRepository walletSnapshotRepository;

    @Inject
    WalletRepository walletRepository;

    @Inject
    WalletSnapshotMapper walletSnapshotMapper;

    public List<WalletSnapshotDTO> getAllWalletSnapshots() {
        List<WalletSnapshot> walletSnapshots = walletSnapshotRepository.listAll();
        return walletSnapshotMapper.listToDTOs(walletSnapshots);
    }

    public WalletSnapshotDTO getById(UUID id) {
        WalletSnapshot walletSnapshot = walletSnapshotRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet snapshot not found for ID: " + id));
        return walletSnapshotMapper.toDTO(walletSnapshot);
    }

    @Transactional
    public WalletSnapshotDTO create(WalletSnapshotForm form) {
        Wallet wallet = walletRepository.findByIdOptional(form.walletId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for ID: " + form.walletId()));

        walletSnapshotRepository.findByWalletAndDate(form.walletId(), form.snapshotDate()).ifPresent(existing -> {
            throw new ConflictException("A snapshot for this wallet on date " + form.snapshotDate() + " already exists.");
        });

        WalletSnapshot snapshot = new WalletSnapshot();
        snapshot.wallet = wallet;
        snapshot.snapshotDate = form.snapshotDate();
        snapshot.totalValue = form.totalValue();
        snapshot.totalInvested = form.totalInvested();

        walletSnapshotRepository.persist(snapshot);
        return walletSnapshotMapper.toDTO(snapshot);
    }

    @Transactional
    public WalletSnapshotDTO update(UUID id, WalletSnapshotForm form) {
        WalletSnapshot snapshot = walletSnapshotRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet snapshot not found for ID: " + id));

        if (form.walletId() != null) {
            Wallet wallet = walletRepository.findByIdOptional(form.walletId())
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for ID: " + form.walletId()));
            snapshot.wallet = wallet;
        }

        if (form.snapshotDate() != null) {
            snapshot.snapshotDate = form.snapshotDate();
        }

        if (form.totalValue() != null) {
            snapshot.totalValue = form.totalValue();
        }

        if (form.totalInvested() != null) {
            snapshot.totalInvested = form.totalInvested();
        }

        return walletSnapshotMapper.toDTO(snapshot);
    }

    @Transactional
    public void delete(UUID id) {
        WalletSnapshot snapshot = walletSnapshotRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet snapshot not found for ID: " + id));
        walletSnapshotRepository.delete(snapshot);
    }
}
