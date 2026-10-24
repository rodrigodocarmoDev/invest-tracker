package service;

import dto.user.UserDTO;
import dto.user.UserForm;
import jakarta.inject.Inject;
import mapper.UserMapper;
import model.User;
import repository.UserRepository;
import java.util.List;
import java.util.UUID;

public class UserService {

    @Inject
    UserRepository userRepository;
    @Inject
    UserMapper userMapper;

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.listAll();
        return userMapper.listToDTOs(users);
    }

    public UserDTO getById(UUID id) {
        User user = userRepository.findById(id);
        return userMapper.toDTO(user);
    }

    //@Transactional
    public UserDTO create(UserForm userForm) {
        User user = userRepository.create(userForm);
        return userMapper.toDTO(user);
    }

    //@Transactional
    public UserDTO update(UUID id, UserForm userForm) {
        User user = userRepository.findById(id);
        return userMapper.toDTO(user);
    }

    //@Transactional
    public void delete(UUID id) {
        userRepository.deleteById(id);
    }

}
