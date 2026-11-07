package service;

import dto.auth.AuthResponseDTO;
import dto.auth.LoginForm;
import dto.auth.MessageResponseDTO;
import dto.auth.RegisterForm;
import dto.user.UserDTO;
import exception.custom.BusinessException;
import exception.custom.ConflictException;
import exception.custom.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import mapper.UserMapper;
import model.User;
import repository.UserRepository;
import security.Roles;
import util.SecurityUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class AuthService {

    @Inject
    UserRepository userRepository;

    @Inject
    UserMapper userMapper;

    @Inject
    TokenService tokenService;

    @Inject
    EmailService emailService;

    @Transactional
    public UserDTO register(RegisterForm form) {
        userRepository.findByEmail(form.email()).ifPresent(existing -> {
            throw new ConflictException("User with email '" + form.email() + "' already exists.");
        });

        User user = new User();
        user.email = form.email().trim().toLowerCase();
        user.name = form.name() != null ? form.name().trim() : null;
        user.passwordHash = SecurityUtils.hashPassword(form.password());
        // A role é sempre definida pelo servidor. Nunca aceite "role" vinda do formulário de cadastro.
        user.role = Roles.USER;

        userRepository.persist(user);
        startEmailVerification(user);

        return userMapper.toDTO(user);
    }

    @Transactional
    public MessageResponseDTO verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException("Verification token is required.");
        }

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired verification token."));

        if (user.verificationTokenExpiresAt != null && user.verificationTokenExpiresAt.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Verification token has expired. Please request a new verification email.");
        }

        user.emailVerified = true;
        user.verificationToken = null;
        user.verificationTokenExpiresAt = null;

        return new MessageResponseDTO("Email successfully verified! You can now log in.");
    }

    /**
     * A resposta é sempre a mesma, exista ou não o e-mail.
     * Se respondêssemos "usuário não encontrado", qualquer pessoa poderia descobrir
     * quais e-mails têm conta no sistema (ataque de enumeração de usuários).
     */
    @Transactional
    public MessageResponseDTO resendVerificationEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("Email is required.");
        }

        userRepository.findByEmail(email)
                .filter(user -> !user.emailVerified)
                .ifPresent(this::startEmailVerification);

        return new MessageResponseDTO("If this email is registered and not yet verified, a new verification link has been sent.");
    }

    public AuthResponseDTO login(LoginForm form) {
        // Mesma mensagem para "e-mail não existe" e "senha errada": não revela qual dos dois falhou.
        User user = userRepository.findByEmail(form.email())
                .orElseThrow(() -> new BusinessException("Invalid email or password."));

        if (!SecurityUtils.checkPassword(form.password(), user.passwordHash)) {
            throw new BusinessException("Invalid email or password.");
        }

        if (!user.emailVerified) {
            throw new BusinessException("Email not verified. Please verify your email before logging in, or request a new verification link.");
        }

        String token = tokenService.generateToken(user);
        long expiresIn = tokenService.getExpirationTimeInSeconds();

        return new AuthResponseDTO(token, expiresIn, userMapper.toDTO(user));
    }

    /**
     * Marca o e-mail como não verificado, gera um novo token de verificação (válido por 24h) e envia o e-mail.
     * Usado no cadastro, no reenvio e quando o usuário troca de e-mail.
     */
    public void startEmailVerification(User user) {
        user.emailVerified = false;
        user.verificationToken = UUID.randomUUID().toString();
        user.verificationTokenExpiresAt = LocalDateTime.now().plusHours(24);

        emailService.sendVerificationEmail(user.email, user.name, user.verificationToken);
    }
}
