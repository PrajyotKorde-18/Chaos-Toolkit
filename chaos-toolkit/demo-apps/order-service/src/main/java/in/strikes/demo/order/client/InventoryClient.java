package in.strikes.demo.order.client;

import in.strikes.demo.order.model.InventoryResult;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class InventoryClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;

    public InventoryClient(
            @Value("${services.inventory.url:http://localhost:8082}") String inventoryUrl,
            CircuitBreakerRegistry registry) {

        this.restClient = RestClient.builder().baseUrl(inventoryUrl).build();

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50.0f)
                .slidingWindowSize(4)
                .minimumNumberOfCalls(2)
                .waitDurationInOpenState(Duration.ofSeconds(3))
                .permittedNumberOfCallsInHalfOpenState(2)
                .slowCallRateThreshold(50.0f)
                .slowCallDurationThreshold(Duration.ofMillis(800))
                .build();

        this.circuitBreaker = registry.circuitBreaker("inventoryService", config);

        this.circuitBreaker.getEventPublisher()
                .onStateTransition(event -> log.warn("[resilience4j] InventoryClient CircuitBreaker state transition: {}", event))
                .onError(event -> log.debug("[resilience4j] InventoryClient CircuitBreaker error: {}", event))
                .onCallNotPermitted(event -> log.warn("[resilience4j] InventoryClient CircuitBreaker call NOT permitted: {}", event));
    }

    public InventoryResult reserve(String itemSku, int quantity) {
        Supplier<InventoryResult> decoratedSupplier = CircuitBreaker.decorateSupplier(
                circuitBreaker,
                () -> restClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/inventory/reserve")
                                .queryParam("itemSku", itemSku)
                                .queryParam("quantity", quantity)
                                .build())
                        .retrieve()
                        .body(InventoryResult.class)
        );

        try {
            return decoratedSupplier.get();
        } catch (Exception ex) {
            log.warn("[fallback] Inventory call failed ({}), executing fallback logic", ex.getMessage());
            return inventoryFallback(itemSku, quantity, ex);
        }
    }

    private InventoryResult inventoryFallback(String itemSku, int quantity, Throwable t) {
        String fallbackRes = "fb_res_" + UUID.randomUUID().toString().substring(0, 8);
        return new InventoryResult(fallbackRes, "BACKORDERED", itemSku, quantity, "Backordered: " + t.getMessage());
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
}
