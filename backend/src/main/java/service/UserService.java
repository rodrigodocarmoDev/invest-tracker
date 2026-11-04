package service;

import dto.user.UserDTO;
import dto.user.UserForm;
import exception.custom.ConflictException;
import exception.custom.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import mapper.UserMapper;
import model.User;
import repository.UserRepository;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
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
        User user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + id));
        return userMapper.toDTO(user);
    }

    @Transactional
    public UserDTO create(UserForm userForm) {
        userRepository.findByEmail(userForm.email()).ifPresent(u -> {
            throw new ConflictException("A user with email '" + userForm.email() + "' already exists.");
        });

        User user = userMapper.formToModel(userForm);
        userRepository.persist(user);
        return userMapper.toDTO(user);
    }

    @Transactional
    public UserDTO update(UUID id, UserForm userForm) {
        User user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + id));

        if (userForm.email() != null && !userForm.email().isBlank()) {
            userRepository.findByEmail(userForm.email()).ifPresent(existing -> {
                if (!existing.id.equals(id)) {
                    throw new ConflictException("Email '" + userForm.email() + "' is already in use by another user.");
                }
            });
            user.email = userForm.email();
        }

        if (userForm.password() != null && !userForm.password().isBlank()) {
            user.passwordHash = userForm.password();
        }

        return userMapper.toDTO(user);
    }

    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + id));
        userRepository.delete(user);
    }
}
