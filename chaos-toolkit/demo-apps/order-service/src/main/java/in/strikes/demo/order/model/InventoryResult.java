package in.strikes.demo.order.model;

public record InventoryResult(
        String reservationId,
        String status,
        String itemSku,
        int quantity,
        String message
) {}
