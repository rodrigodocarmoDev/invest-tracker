package repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.DailyQuote;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class DailyQuoteRepository implements PanacheRepositoryBase<DailyQuote, UUID> {

    public List<DailyQuote> listByAssetId(UUID assetId) {
        return list("asset.id", assetId);
    }

    public Optional<DailyQuote> findByAssetAndDate(UUID assetId, LocalDate quoteDate) {
        return find("asset.id = ?1 and quoteDate = ?2", assetId, quoteDate).firstResultOptional();
    }
}
