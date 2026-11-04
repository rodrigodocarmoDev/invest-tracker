package repository;


import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import model.User;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements  PanacheRepositoryBase<User, UUID> {

}
