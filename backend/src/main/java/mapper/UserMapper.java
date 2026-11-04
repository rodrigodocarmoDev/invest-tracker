package mapper;

import dto.user.UserDTO;
import dto.user.UserForm;
import model.User;

import java.util.List;

public class UserMapper {

    private final ModelMapper modelMapper = new ModelMapper();

    public UserDTO toDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    public List<UserDTO> listToDTOs (List<User> users) {
        return users.stream().map((User user) -> toDTO(user)).toList();
    }

    public User formToModel(UserForm userForm) {
        return modelMapper.map(userForm, User.class);
    }
}
