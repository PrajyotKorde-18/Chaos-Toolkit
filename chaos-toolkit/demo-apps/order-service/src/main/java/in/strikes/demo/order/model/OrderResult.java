package in.strikes.demo.order.model;

public record OrderResult(
        String orderId,
        String status,
        String paymentStatus,
        String inventoryStatus,
        String message
) {}
