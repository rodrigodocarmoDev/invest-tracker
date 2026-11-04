package repository;


import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.Transaction;
import java.util.UUID;

@ApplicationScoped
public class TransactionRepository implements  PanacheRepositoryBase<Transaction, UUID> {

}
