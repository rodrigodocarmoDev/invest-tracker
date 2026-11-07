package security;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import model.User;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import repository.UserRepository;
import util.SecurityUtils;

import java.util.Optional;

/**
 * Cria o primeiro administrador do sistema na inicialização da aplicação.
 *
 * Problema do "ovo e da galinha": só um ADMIN pode promover alguém a ADMIN,
 * mas quem cria o primeiro? A resposta é a configuração do servidor (que só quem faz o deploy controla):
 *   - em dev:  valores padrão no application.properties (perfil %dev)
 *   - em prod: variáveis de ambiente ADMIN_EMAIL e ADMIN_PASSWORD (nunca commitadas no Git)
 */
@ApplicationScoped
public class AdminSeeder {

    private static final Logger LOG = Logger.getLogger(AdminSeeder.class);

    @Inject
    UserRepository userRepository;

    @ConfigProperty(name = "invest-tracker.admin.email")
    Optional<String> adminEmail;

    @ConfigProperty(name = "invest-tracker.admin.password")
    Optional<String> adminPassword;

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (userRepository.count("role", Roles.ADMIN) > 0) {
            return;
        }

        if (adminEmail.isEmpty() || adminPassword.isEmpty()) {
            LOG.warn("No ADMIN user exists and ADMIN_EMAIL/ADMIN_PASSWORD are not configured. Skipping admin creation.");
            return;
        }

        String email = adminEmail.get().trim().toLowerCase();

        // Nunca promovemos uma conta já existente: qualquer pessoa poderia ter se cadastrado
        // antes com esse e-mail e ganharia acesso de administrador "de graça".
        if (userRepository.findByEmail(email).isPresent()) {
            LOG.warnf("A user with email '%s' already exists. Admin was NOT created; choose another ADMIN_EMAIL.", email);
            return;
        }

        User admin = new User();
        admin.email = email;
        admin.name = "Administrator";
        admin.passwordHash = SecurityUtils.hashPassword(adminPassword.get());
        admin.emailVerified = true;
        admin.role = Roles.ADMIN;

        userRepository.persist(admin);
        LOG.infof("Initial ADMIN user created: %s", email);
    }
}
