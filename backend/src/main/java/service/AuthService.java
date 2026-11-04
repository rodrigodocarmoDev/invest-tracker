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
        user.emailVerified = false;
        user.verificationToken = UUID.randomUUID().toString();
        user.verificationTokenExpiresAt = LocalDateTime.now().plusHours(24);
        user.role = "USER";

        userRepository.persist(user);

        // Dispara o e-mail de verificação
        emailService.sendVerificationEmail(user.email, user.name, user.verificationToken);

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

    @Transactional
    public MessageResponseDTO resendVerificationEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("Email is required.");
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.emailVerified) {
            throw new ConflictException("This email is already verified.");
        }

        user.verificationToken = UUID.randomUUID().toString();
        user.verificationTokenExpiresAt = LocalDateTime.now().plusHours(24);

        emailService.sendVerificationEmail(user.email, user.name, user.verificationToken);

        return new MessageResponseDTO("Verification email successfully resent to: " + user.email);
    }

    public AuthResponseDTO login(LoginForm form) {
        String email = form.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
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
}
