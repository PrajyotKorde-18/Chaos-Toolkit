package in.strikes.demo.payment.model;

public record PaymentResult(
        String transactionId,
        String status,
        long amountCents,
        String message
) {}
