package service;

import dto.user.RoleForm;
import dto.user.UserDTO;
import dto.user.UserForm;
import exception.custom.BusinessException;
import exception.custom.ConflictException;
import exception.custom.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import mapper.UserMapper;
import model.User;
import repository.UserRepository;
import security.CurrentUser;
import security.Roles;
import util.SecurityUtils;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    UserMapper userMapper;

    @Inject
    AuthService authService;

    @Inject
    CurrentUser currentUser;

    /** Apenas ADMIN (garantido por @RolesAllowed no UserResource). */
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.listAll();
        return userMapper.listToDTOs(users);
    }

    public UserDTO getById(UUID id) {
        return userMapper.toDTO(findAccessibleUser(id));
    }

    /** Apenas ADMIN. Usuários comuns se cadastram por /auth/register. */
    @Transactional
    public UserDTO create(UserForm userForm) {
        userRepository.findByEmail(userForm.email()).ifPresent(u -> {
            throw new ConflictException("A user with email '" + userForm.email() + "' already exists.");
        });

        User user = new User();
        user.email = userForm.email().trim().toLowerCase();
        user.passwordHash = SecurityUtils.hashPassword(userForm.password());
        user.emailVerified = true;
        user.role = Roles.USER;

        userRepository.persist(user);
        return userMapper.toDTO(user);
    }

    @Transactional
    public UserDTO update(UUID id, UserForm userForm) {
        User user = findAccessibleUser(id);

        if (userForm.email() != null && !userForm.email().isBlank()) {
            String newEmail = userForm.email().trim().toLowerCase();

            userRepository.findByEmail(newEmail).ifPresent(existing -> {
                if (!existing.id.equals(id)) {
                    throw new ConflictException("Email '" + userForm.email() + "' is already in use by another user.");
                }
            });

            // Trocou de e-mail? Precisa provar que o novo e-mail é dele antes de logar novamente.
            if (!newEmail.equals(user.email)) {
                user.email = newEmail;
                authService.startEmailVerification(user);
            }
        }

        if (userForm.password() != null && !userForm.password().isBlank()) {
            user.passwordHash = SecurityUtils.hashPassword(userForm.password());
        }

        return userMapper.toDTO(user);
    }

    /** Apenas ADMIN. */
    @Transactional
    public UserDTO updateRole(UUID id, RoleForm roleForm) {
        if (currentUser.id().equals(id)) {
            // Evita que o último administrador remova o próprio acesso por engano.
            throw new BusinessException("You cannot change your own role.");
        }

        User user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + id));
        user.role = roleForm.role();

        return userMapper.toDTO(user);
    }

    @Transactional
    public void delete(UUID id) {
        User user = findAccessibleUser(id);
        userRepository.delete(user);
    }

    /** Busca o usuário e garante que quem pede é ele mesmo ou um ADMIN. */
    private User findAccessibleUser(UUID id) {
        User user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + id));
        currentUser.checkAccess(user.id);
        return user;
    }
}
