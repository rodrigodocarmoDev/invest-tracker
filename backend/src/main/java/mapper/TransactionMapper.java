package mapper;

import dto.transaction.TransactionDTO;
import dto.transaction.TransactionForm;
import model.Transaction;
import java.util.List;

public class TransactionMapper {

    private final ModelMapper modelMapper = new ModelMapper();

    public TransactionDTO toDTO(Transaction transaction) {
        return modelMapper.map(transaction, TransactionDTO.class);
    }

    public List<TransactionDTO> listToDTOs (List<Transaction> transactions) {
        return transactions.stream().map((Transaction transaction) -> toDTO(transaction)).toList();
    }

    public Transaction formToModel(TransactionForm transactionForm) {
        return modelMapper.map(transactionForm, Transaction.class);
    }
}
