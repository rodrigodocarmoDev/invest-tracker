package service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    @ConfigProperty(name = "invest-tracker.app.url", defaultValue = "http://localhost:8080")
    String appUrl;

    public void sendVerificationEmail(String recipientEmail, String recipientName, String token) {
        String name = recipientName != null && !recipientName.isBlank() ? recipientName : "Investidor";
        String verificationLink = appUrl + "/auth/verify-email?token=" + token;

        String htmlBody = """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; color: #333; line-height: 1.6;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px;">
                    <h2 style="color: #4338ca;">Bem-vindo ao Invest-Tracker!</h2>
                    <p>Olá, <strong>%s</strong>,</p>
                    <p>Obrigado por criar sua conta. Para começar a gerenciar suas carteiras e investimentos, confirme seu endereço de e-mail clicando no botão abaixo:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #4338ca; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">Verificar Meu E-mail</a>
                    </div>
                    <p style="font-size: 13px; color: #64748b;">Ou copie e cole o link a seguir no seu navegador:<br>
                    <a href="%s">%s</a></p>
                    <p style="font-size: 12px; color: #94a3b8; margin-top: 30px; border-top: 1px solid #e2e8f0; padding-top: 15px;">Este link expira em 24 horas. Se você não solicitou este cadastro, pode ignorar esta mensagem com segurança.</p>
                </div>
            </body>
            </html>
            """.formatted(name, verificationLink, verificationLink, verificationLink);

        mailer.send(Mail.withHtml(recipientEmail, "Confirme seu e-mail - Invest-Tracker", htmlBody));
    }
}
