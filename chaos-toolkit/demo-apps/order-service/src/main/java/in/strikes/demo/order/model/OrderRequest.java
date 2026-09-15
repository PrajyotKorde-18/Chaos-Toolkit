package in.strikes.demo.order.model;

public record OrderRequest(
        String itemSku,
        int quantity,
        long amountCents,
        String cardToken
) {}
