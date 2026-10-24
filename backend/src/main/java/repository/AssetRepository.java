package repository;


import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.Asset;

import java.util.UUID;

@ApplicationScoped
public class AssetRepository implements  PanacheRepositoryBase<Asset, UUID> {

}
