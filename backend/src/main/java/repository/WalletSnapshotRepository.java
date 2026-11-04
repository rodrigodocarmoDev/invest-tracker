package repository;


import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.WalletSnapshot;
import java.util.UUID;

@ApplicationScoped
public class WalletSnapshotRepository implements  PanacheRepositoryBase<WalletSnapshot, UUID> {

}
