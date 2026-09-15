package in.strikes.demo.order.service;

import in.strikes.chaosagent.annotation.ChaosException;
import in.strikes.chaosagent.annotation.ChaosLatency;
import in.strikes.demo.order.client.InventoryClient;
import in.strikes.demo.order.client.PaymentClient;
import in.strikes.demo.order.model.InventoryResult;
import in.strikes.demo.order.model.OrderRequest;
import in.strikes.demo.order.model.OrderResult;
import in.strikes.demo.order.model.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;

    public OrderService(InventoryClient inventoryClient, PaymentClient paymentClient) {
        this.inventoryClient = inventoryClient;
        this.paymentClient = paymentClient;
    }

    @ChaosLatency(faultId = "order-service.processOrder", defaultMinMs = 50, defaultMaxMs = 200)
    @ChaosException(faultId = "order-service.processOrder", defaultMessage = "Simulated order service internal failure")
    public OrderResult createOrder(OrderRequest request) {
        String orderId = "ord_" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Processing order {}: item={}, qty={}, amount={}", orderId, request.itemSku(), request.quantity(), request.amountCents());

        // Step 1: Reserve inventory
        InventoryResult invResult = inventoryClient.reserve(request.itemSku(), request.quantity());

        // Step 2: Charge payment
        PaymentResult payResult = paymentClient.charge(request.cardToken(), request.amountCents());

        String overallStatus = ("RESERVED".equals(invResult.status()) && "SUCCESS".equals(payResult.status()))
                ? "COMPLETED"
                : "PARTIALLY_DEGRADED";

        String message = String.format("Order %s processed. Inventory: %s, Payment: %s",
                orderId, invResult.message(), payResult.message());

        return new OrderResult(orderId, overallStatus, payResult.status(), invResult.status(), message);
    }
}
