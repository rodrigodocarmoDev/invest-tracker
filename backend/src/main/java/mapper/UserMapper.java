package mapper;

import dto.user.UserDTO;
import dto.user.UserForm;
import jakarta.enterprise.context.ApplicationScoped;
import model.User;

import java.util.List;

@ApplicationScoped
public class UserMapper {

    public UserDTO toDTO(User user) {
        if (user == null) return null;
        return new UserDTO(user.id, user.email, user.createdAt);
    }

    public List<UserDTO> listToDTOs(List<User> users) {
        if (users == null) return List.of();
        return users.stream().map(this::toDTO).toList();
    }

    public User formToModel(UserForm form) {
        if (form == null) return null;
        User user = new User();
        user.email = form.email();
        user.passwordHash = form.password(); // em prod aplicaria hash
        return user;
    }
}
