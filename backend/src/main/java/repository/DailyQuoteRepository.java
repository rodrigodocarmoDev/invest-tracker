package repository;


import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.DailyQuote;
import java.util.UUID;

@ApplicationScoped
public class DailyQuoteRepository implements  PanacheRepositoryBase<DailyQuote, UUID> {

}
