package client;

import client.dto.BrapiQuoteResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Client REST da API pública de cotações da B3: https://brapi.dev
 *
 * É o caminho inverso de um Resource:
 *   Resource (@Path)            -> alguém de fora chama a NOSSA API      (entrada)
 *   Client (@RegisterRestClient) -> a nossa API chama a API de FORA       (saída)
 *
 * Só escrevemos a interface. O Quarkus gera a implementação no build, usando a
 * URL configurada em "quarkus.rest-client.brapi.url" (configKey = "brapi").
 */
@RegisterRestClient(configKey = "brapi")
@Path("/api/quote")
@Produces(MediaType.APPLICATION_JSON)
public interface BrapiClient {

    /**
     * GET https://brapi.dev/api/quote/{ticker}
     *
     * @param ticker        ex: PETR4
     * @param authorization "Bearer <token>" ou null (sem token, a brapi só libera alguns tickers de teste)
     */
    @GET
    @Path("/{ticker}")
    BrapiQuoteResponse getQuote(@PathParam("ticker") String ticker,
                                @HeaderParam("Authorization") String authorization);
}
