package service;

import dto.wallet.WalletDTO;
import dto.wallet.WalletForm;
import exception.custom.ConflictException;
import exception.custom.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import mapper.WalletMapper;
import model.User;
import model.Wallet;
import repository.TransactionRepository;
import repository.UserRepository;
import repository.WalletRepository;
import repository.WalletSnapshotRepository;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class WalletService {

    @Inject
    WalletRepository walletRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    WalletSnapshotRepository walletSnapshotRepository;

    @Inject
    WalletMapper walletMapper;

    public List<WalletDTO> getAllWallets() {
        List<Wallet> wallets = walletRepository.listAll();
        return walletMapper.listToDTOs(wallets);
    }

    public WalletDTO getById(UUID id) {
        Wallet wallet = walletRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for ID: " + id));
        return walletMapper.toDTO(wallet);
    }

    @Transactional
    public WalletDTO create(WalletForm walletForm) {
        User user = userRepository.findByIdOptional(walletForm.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + walletForm.userId()));

        Wallet wallet = new Wallet();
        wallet.name = walletForm.name();
        wallet.users = user;

        walletRepository.persist(wallet);
        return walletMapper.toDTO(wallet);
    }

    @Transactional
    public WalletDTO update(UUID id, WalletForm walletForm) {
        Wallet wallet = walletRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for ID: " + id));

        if (walletForm.name() != null && !walletForm.name().isBlank()) {
            wallet.name = walletForm.name();
        }

        if (walletForm.userId() != null) {
            User user = userRepository.findByIdOptional(walletForm.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + walletForm.userId()));
            wallet.users = user;
        }

        return walletMapper.toDTO(wallet);
    }

    @Transactional
    public void delete(UUID id) {
        Wallet wallet = walletRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for ID: " + id));

        if (!transactionRepository.listByWalletId(id).isEmpty()) {
            throw new ConflictException("Not possible to delete wallet with registered transactions.");
        }

        if (!walletSnapshotRepository.listByWalletId(id).isEmpty()) {
            throw new ConflictException("Not possible to delete wallet with registered snapshots.");
        }

        walletRepository.delete(wallet);
    }
}
