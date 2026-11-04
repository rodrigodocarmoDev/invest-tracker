package repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.Asset;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AssetRepository implements PanacheRepositoryBase<Asset, UUID> {

    public Optional<Asset> findByTicker(String ticker) {
        return find("ticker", ticker).firstResultOptional();
    }
}
