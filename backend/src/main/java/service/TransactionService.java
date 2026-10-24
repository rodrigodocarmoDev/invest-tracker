package service;

import dto.transaction.TransactionDTO;
import dto.transaction.TransactionForm;
import jakarta.inject.Inject;
import mapper.TransactionMapper;
import model.Transaction;
import repository.TransactionRepository;
import java.util.List;
import java.util.UUID;

public class TransactionService {

    @Inject
    TransactionRepository transactionRepository;
    @Inject
    TransactionMapper transactionMapper;

    public List<TransactionDTO> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.listAll();
        return transactionMapper.listToDTOs(transactions);
    }

    public TransactionDTO getById(UUID id) {
        Transaction transaction = transactionRepository.findById(id);
        return transactionMapper.toDTO(transaction);
    }

    //@Transactional
    public TransactionDTO create(TransactionForm transactionForm) {
        Transaction transaction = transactionRepository.create(transactionForm);
        return transactionMapper.toDTO(transaction);
    }

    //@Transactional
    public TransactionDTO update(UUID id, TransactionForm transactionForm) {
        Transaction transaction = transactionRepository.findById(id);
        return transactionMapper.toDTO(transaction);
    }

    //@Transactional
    public void delete(UUID id) {
        transactionRepository.deleteById(id);
    }

}
