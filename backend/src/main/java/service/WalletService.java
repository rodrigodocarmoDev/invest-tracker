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
import security.CurrentUser;

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

    @Inject
    CurrentUser currentUser;

    /** ADMIN vê todas as carteiras; usuário comum vê apenas as suas. */
    public List<WalletDTO> getAllWallets() {
        List<Wallet> wallets = currentUser.isAdmin()
                ? walletRepository.listAll()
                : walletRepository.listByUserId(currentUser.id());
        return walletMapper.listToDTOs(wallets);
    }

    public WalletDTO getById(UUID id) {
        return walletMapper.toDTO(findAccessibleWallet(id));
    }

    @Transactional
    public WalletDTO create(WalletForm walletForm) {
        // O dono padrão é quem está logado. Informar outro userId só é permitido para ADMIN.
        UUID ownerId = walletForm.userId() != null ? walletForm.userId() : currentUser.id();
        currentUser.checkAccess(ownerId);

        User user = userRepository.findByIdOptional(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + ownerId));

        Wallet wallet = new Wallet();
        wallet.name = walletForm.name();
        wallet.users = user;

        walletRepository.persist(wallet);
        return walletMapper.toDTO(wallet);
    }

    @Transactional
    public WalletDTO update(UUID id, WalletForm walletForm) {
        Wallet wallet = findAccessibleWallet(id);

        if (walletForm.name() != null && !walletForm.name().isBlank()) {
            wallet.name = walletForm.name();
        }

        if (walletForm.userId() != null && !walletForm.userId().equals(wallet.users.id)) {
            currentUser.requireAdmin("Only administrators can transfer a wallet to another user.");
            User user = userRepository.findByIdOptional(walletForm.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + walletForm.userId()));
            wallet.users = user;
        }

        return walletMapper.toDTO(wallet);
    }

    @Transactional
    public void delete(UUID id) {
        Wallet wallet = findAccessibleWallet(id);

        if (!transactionRepository.listByWalletId(id).isEmpty()) {
            throw new ConflictException("Not possible to delete wallet with registered transactions.");
        }

        if (!walletSnapshotRepository.listByWalletId(id).isEmpty()) {
            throw new ConflictException("Not possible to delete wallet with registered snapshots.");
        }

        walletRepository.delete(wallet);
    }

    /**
     * Busca a carteira e garante que o usuário logado é o dono (ou ADMIN).
     * Público porque Transaction e WalletSnapshot também dependem do dono da carteira:
     * a regra de acesso fica escrita em UM lugar só.
     */
    public Wallet findAccessibleWallet(UUID id) {
        Wallet wallet = walletRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for ID: " + id));
        currentUser.checkAccess(wallet.users.id);
        return wallet;
    }
}
