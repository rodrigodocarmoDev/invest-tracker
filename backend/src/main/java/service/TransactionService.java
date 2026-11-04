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
import repository.WalletRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransactionService {

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    WalletRepository walletRepository;

    @Inject
    AssetRepository assetRepository;

    @Inject
    TransactionMapper transactionMapper;

    public List<TransactionDTO> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.listAll();
        return transactionMapper.listToDTOs(transactions);
    }

    public TransactionDTO getById(UUID id) {
        Transaction transaction = transactionRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for ID: " + id));
        return transactionMapper.toDTO(transaction);
    }

    @Transactional
    public TransactionDTO create(TransactionForm form) {
        Wallet wallet = walletRepository.findByIdOptional(form.walletId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for ID: " + form.walletId()));

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
        Transaction transaction = transactionRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for ID: " + id));

        if (form.walletId() != null) {
            Wallet wallet = walletRepository.findByIdOptional(form.walletId())
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for ID: " + form.walletId()));
            transaction.wallet = wallet;
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
        Transaction transaction = transactionRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for ID: " + id));
        transactionRepository.delete(transaction);
    }
}
