package scheduler;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import service.QuoteSyncService;

/**
 * Gatilhos AUTOMÁTICOS da sincronização de cotações:
 *
 * 1. Agendado (cron): todo dia útil após o fechamento da B3.
 *    - cron: lido da configuração "invest-tracker.quotes.sync-cron" (no perfil de teste vale "off" = desligado)
 *    - timeZone: o horário é o de Brasília, não o do servidor
 *    - concurrentExecution = SKIP: se a execução anterior ainda estiver rodando, esta é pulada
 *
 * 2. Na inicialização da aplicação, apenas se QUOTES_SYNC_ON_STARTUP=true.
 *
 * (O gatilho MANUAL é o endpoint POST /daily-quotes/sync.)
 */
@ApplicationScoped
public class QuoteSyncJob {

    private static final Logger LOG = Logger.getLogger(QuoteSyncJob.class);

    @Inject
    QuoteSyncService quoteSyncService;

    @ConfigProperty(name = "invest-tracker.quotes.sync-on-startup")
    boolean syncOnStartup;

    @Scheduled(
        identity = "quote-sync",
        cron = "{invest-tracker.quotes.sync-cron}",
        timeZone = "America/Sao_Paulo",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP
    )
    void syncQuotes() {
        quoteSyncService.syncAll();
    }

    /**
     * Roda a sincronização logo após a aplicação subir, se estiver habilitada.
     *
     * Por que numa thread separada? Chamar a brapi para cada ativo pode levar vários segundos
     * (ou até o timeout, se ela estiver fora do ar). Se rodasse aqui direto, a aplicação só
     * começaria a atender requisições depois disso. Numa thread à parte, a API sobe na hora
     * e as cotações chegam em segundo plano.
     */
    void onStart(@Observes StartupEvent event) {
        if (!syncOnStartup) {
            return;
        }

        LOG.info("QUOTES_SYNC_ON_STARTUP is enabled: starting quote sync in background.");
        Thread.ofVirtual().name("quote-sync-startup").start(() -> {
            try {
                quoteSyncService.syncAll();
            } catch (RuntimeException ex) {
                LOG.error("Quote sync on startup failed.", ex);
            }
        });
    }
}
