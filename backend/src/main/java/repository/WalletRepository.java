package repository;


import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.Wallet;
import java.util.UUID;

@ApplicationScoped
public class WalletRepository implements  PanacheRepositoryBase<Wallet, UUID> {

}
