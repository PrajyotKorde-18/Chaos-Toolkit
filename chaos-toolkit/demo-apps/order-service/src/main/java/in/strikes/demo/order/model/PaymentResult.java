package in.strikes.demo.order.model;

public record PaymentResult(
        String transactionId,
        String status,
        long amountCents,
        String message
) {}
