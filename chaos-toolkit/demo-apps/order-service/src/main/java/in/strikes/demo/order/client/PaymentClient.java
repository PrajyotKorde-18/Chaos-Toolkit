package in.strikes.demo.order.client;

import in.strikes.demo.order.model.PaymentResult;
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
public class PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;

    public PaymentClient(
            @Value("${services.payment.url:http://localhost:8081}") String paymentUrl,
            CircuitBreakerRegistry registry) {

        this.restClient = RestClient.builder().baseUrl(paymentUrl).build();

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50.0f)
                .slidingWindowSize(4)
                .minimumNumberOfCalls(2)
                .waitDurationInOpenState(Duration.ofSeconds(3))
                .permittedNumberOfCallsInHalfOpenState(2)
                .slowCallRateThreshold(50.0f)
                .slowCallDurationThreshold(Duration.ofMillis(800))
                .build();

        this.circuitBreaker = registry.circuitBreaker("paymentService", config);

        this.circuitBreaker.getEventPublisher()
                .onStateTransition(event -> log.warn("[resilience4j] PaymentClient CircuitBreaker state transition: {}", event))
                .onError(event -> log.debug("[resilience4j] PaymentClient CircuitBreaker error: {}", event))
                .onCallNotPermitted(event -> log.warn("[resilience4j] PaymentClient CircuitBreaker call NOT permitted: {}", event));
    }

    public PaymentResult charge(String cardToken, long amountCents) {
        Supplier<PaymentResult> decoratedSupplier = CircuitBreaker.decorateSupplier(
                circuitBreaker,
                () -> restClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/charge")
                                .queryParam("cardToken", cardToken)
                                .queryParam("amountCents", amountCents)
                                .build())
                        .retrieve()
                        .body(PaymentResult.class)
        );

        try {
            return decoratedSupplier.get();
        } catch (Exception ex) {
            log.warn("[fallback] Payment call failed ({}), executing fallback logic", ex.getMessage());
            return paymentFallback(cardToken, amountCents, ex);
        }
    }

    private PaymentResult paymentFallback(String cardToken, long amountCents, Throwable t) {
        String fallbackTxn = "fb_txn_" + UUID.randomUUID().toString().substring(0, 8);
        return new PaymentResult(fallbackTxn, "FALLBACK_QUEUED", amountCents, "Payment queued offline: " + t.getMessage());
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
}
