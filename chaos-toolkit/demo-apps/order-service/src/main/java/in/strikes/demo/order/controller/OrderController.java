package in.strikes.demo.order.controller;

import in.strikes.demo.order.client.InventoryClient;
import in.strikes.demo.order.client.PaymentClient;
import in.strikes.demo.order.model.CircuitBreakerInfo;
import in.strikes.demo.order.model.OrderRequest;
import in.strikes.demo.order.model.OrderResult;
import in.strikes.demo.order.service.OrderService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final PaymentClient paymentClient;
    private final InventoryClient inventoryClient;

    public OrderController(OrderService orderService, PaymentClient paymentClient, InventoryClient inventoryClient) {
        this.orderService = orderService;
        this.paymentClient = paymentClient;
        this.inventoryClient = inventoryClient;
    }

    @PostMapping("/orders/create")
    public ResponseEntity<OrderResult> createOrder(
            @RequestParam(defaultValue = "SKU-TEST-001") String itemSku,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(defaultValue = "5000") long amountCents,
            @RequestParam(defaultValue = "tok_test_card") String cardToken) {

        OrderRequest request = new OrderRequest(itemSku, quantity, amountCents, cardToken);
        log.info("Received create order request: {}", request);
        OrderResult result = orderService.createOrder(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/orders/circuit-breakers")
    public ResponseEntity<List<CircuitBreakerInfo>> getCircuitBreakers() {
        return ResponseEntity.ok(List.of(
                toInfo(paymentClient.getCircuitBreaker()),
                toInfo(inventoryClient.getCircuitBreaker())
        ));
    }

    @PostMapping("/orders/circuit-breakers/reset")
    public ResponseEntity<String> resetCircuitBreakers() {
        paymentClient.getCircuitBreaker().reset();
        inventoryClient.getCircuitBreaker().reset();
        log.info("Circuit breakers manually reset to CLOSED");
        return ResponseEntity.ok("RESET_OK");
    }

    private CircuitBreakerInfo toInfo(CircuitBreaker cb) {
        CircuitBreaker.Metrics metrics = cb.getMetrics();
        return new CircuitBreakerInfo(
                cb.getName(),
                cb.getState().name(),
                metrics.getFailureRate(),
                metrics.getNumberOfBufferedCalls(),
                metrics.getNumberOfFailedCalls(),
                metrics.getNumberOfSuccessfulCalls(),
                metrics.getNumberOfNotPermittedCalls()
        );
    }
}
