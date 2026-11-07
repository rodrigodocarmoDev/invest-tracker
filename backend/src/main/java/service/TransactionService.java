package service;

import dto.transaction.TransactionDTO;
import dto.transaction.TransactionForm;
import exception.custom.BusinessException;
import exception.custom.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import mapper.TransactionMapper;
import model.Asset;
import model.Transaction;
import model.Wallet;
import repository.AssetRepository;
import repository.TransactionRepository;
import security.CurrentUser;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransactionService {

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    WalletService walletService;

    @Inject
    AssetRepository assetRepository;

    @Inject
    TransactionMapper transactionMapper;

    @Inject
    CurrentUser currentUser;

    /** ADMIN vê todas; usuário comum vê apenas as transações das suas carteiras. */
    public List<TransactionDTO> getAllTransactions() {
        List<Transaction> transactions = currentUser.isAdmin()
                ? transactionRepository.listAll()
                : transactionRepository.listByUserId(currentUser.id());
        return transactionMapper.listToDTOs(transactions);
    }

    public TransactionDTO getById(UUID id) {
        return transactionMapper.toDTO(findAccessibleTransaction(id));
    }

    @Transactional
    public TransactionDTO create(TransactionForm form) {
        // Só pode lançar transação em carteira própria (ou qualquer uma, se ADMIN).
        Wallet wallet = walletService.findAccessibleWallet(form.walletId());

        Asset asset = assetRepository.findByIdOptional(form.assetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found for ID: " + form.assetId()));

        String op = form.operationType().toUpperCase();
        if (!op.equals("BUY") && !op.equals("SELL")) {
            throw new BusinessException("Invalid operation type: " + form.operationType() + ". Expected BUY or SELL.");
        }

        Transaction transaction = new Transaction();
        transaction.wallet = wallet;
        transaction.asset = asset;
        transaction.operationType = op;
        transaction.quantity = form.quantity();
        transaction.unitPrice = form.unitPrice();
        transaction.fees = form.fees() != null ? form.fees() : BigDecimal.ZERO;

        transactionRepository.persist(transaction);
        return transactionMapper.toDTO(transaction);
    }

    @Transactional
    public TransactionDTO update(UUID id, TransactionForm form) {
        Transaction transaction = findAccessibleTransaction(id);

        if (form.walletId() != null) {
            // Mover a transação também exige ser dono da carteira de DESTINO.
            transaction.wallet = walletService.findAccessibleWallet(form.walletId());
        }

        if (form.assetId() != null) {
            Asset asset = assetRepository.findByIdOptional(form.assetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Asset not found for ID: " + form.assetId()));
            transaction.asset = asset;
        }

        if (form.operationType() != null) {
            String op = form.operationType().toUpperCase();
            if (!op.equals("BUY") && !op.equals("SELL")) {
                throw new BusinessException("Invalid operation type: " + form.operationType() + ". Expected BUY or SELL.");
            }
            transaction.operationType = op;
        }

        if (form.quantity() != null) {
            transaction.quantity = form.quantity();
        }

        if (form.unitPrice() != null) {
            transaction.unitPrice = form.unitPrice();
        }

        if (form.fees() != null) {
            transaction.fees = form.fees();
        }

        return transactionMapper.toDTO(transaction);
    }

    @Transactional
    public void delete(UUID id) {
        Transaction transaction = findAccessibleTransaction(id);
        transactionRepository.delete(transaction);
    }

    /** Uma transação pertence a quem é dono da carteira dela. */
    private Transaction findAccessibleTransaction(UUID id) {
        Transaction transaction = transactionRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for ID: " + id));
        currentUser.checkAccess(transaction.wallet.users.id);
        return transaction;
    }
}
