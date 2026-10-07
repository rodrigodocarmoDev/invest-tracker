package net.weg.capture.config;

import io.smallrye.common.annotation.Identifier;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class KafkaGlobalConfig {

    private static final Logger LOG = Logger.getLogger(KafkaGlobalConfig.class.getName());

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @ConfigProperty(name = "weg-event-driven-platform.kafka.bootstrap.servers")
    String bootStrapServers;

    @ConfigProperty(name = "weg-event-driven-platform.kafka.security.protocol")
    String securityProtocol;

    @ConfigProperty(name = "weg-event-driven-platform.kafka.group.id")
    String groupId;

    @ConfigProperty(name = "weg-event-driven-platform.kafka.oauth.token.endpoint.uri")
    String tokenEndpoint;

    @ConfigProperty(name = "quarkus.oidc.client-id")
    String clientId;

    @ConfigProperty(name = "quarkus.oidc.credentials.secret")
    String clientSecret;

    @ConfigProperty(name = "weg-event-driven-platform.kafka.cert.url")
    String certUrl;

    @ConfigProperty(name = "weg-event-driven-platform.kafka.schema-registry.url")
    private String schemaRegistryUrl;

    @Inject
    ObjectMapper objectMapper;

    private volatile String kafkaPemCertificate;

    @PostConstruct
    void init() {
        if (isTestMode()) {
            LOG.info("Skipping Kafka certificate fetch for test profile.");
            kafkaPemCertificate = "dummy-cert";
            return;
        }
        try {
            kafkaPemCertificate = fetchKafkaCertificate();
            LOG.info("Kafka PEM certificate fetched successfully from remote endpoint.");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch Kafka certificate at startup", e);
        }
    }

    private String fetchKafkaCertificate() throws IOException, InterruptedException {
        String accessToken = fetchKeycloakToken();

        HttpRequest certRequest = HttpRequest.newBuilder().uri(URI.create(certUrl))
                .header("Authorization", "Bearer " + accessToken).GET().build();

        HttpResponse<String> certResponse = httpClient.send(certRequest, HttpResponse.BodyHandlers.ofString());

        if (certResponse.statusCode() != 200) {
            throw new IOException(
                    "Failed to fetch Kafka certificate. HTTP " + certResponse.statusCode() + " from: " + certUrl);
        }

        String cert = certResponse.body();
        if (cert == null || cert.isBlank()) {
            throw new IOException("Kafka certificate response body is empty from: " + certUrl);
        }

        return cert.trim();
    }

    private String fetchKeycloakToken() throws IOException, InterruptedException {
        String formBody = "grant_type=" + URLEncoder.encode("client_credentials", StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) + "&client_secret="
                + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8);

        HttpRequest tokenRequest = HttpRequest.newBuilder().uri(URI.create(tokenEndpoint))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody)).build();

        HttpResponse<String> tokenResponse = httpClient.send(tokenRequest, HttpResponse.BodyHandlers.ofString());

        if (tokenResponse.statusCode() != 200) {
            throw new IOException(
                    "Failed to obtain Keycloak token. HTTP " + tokenResponse.statusCode() + " from: " + tokenEndpoint);
        }

        JsonNode tokenJson = objectMapper.readTree(tokenResponse.body());
        String token = tokenJson.path("access_token").asText(null);
        if (token == null || token.isBlank()) {
            throw new IOException("access_token missing in Keycloak response.");
        }

        return token;
    }

    protected boolean isTestMode() {
        return io.quarkus.runtime.LaunchMode.TEST.equals(io.quarkus.runtime.LaunchMode.current());
    }

    @Produces
    @Identifier("default-kafka-broker")
    @ApplicationScoped
    public Map<String, Object> kafkaConfig() {
        Map<String, Object> config = new HashMap<>();

        String jaasConfig = String
                .format("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required "
                        + "oauth.token.endpoint.uri=\"%s\" " + "oauth.client.id=\"%s\" "
                        + "oauth.client.secret=\"%s\" ;", tokenEndpoint, clientId, clientSecret);

        config.put("bootstrap.servers", bootStrapServers);
        config.put("group.id", groupId);

        config.put("security.protocol", securityProtocol);
        config.put("sasl.mechanism", "OAUTHBEARER");
        config.put("sasl.jaas.config", jaasConfig);
        config.put("sasl.login.callback.handler.class",
                "io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler");

        config.put("ssl.truststore.type", "PEM");
        config.put("ssl.truststore.certificates", kafkaPemCertificate);
        config.put("ssl.endpoint.identification.algorithm", "");

        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer");
        config.put("specific.avro.reader", true);
        config.put("auto.register.schemas", false);
        config.put("use.latest.version", true);
        config.put("schema.registry.url", schemaRegistryUrl);

        return config;
    }
}
