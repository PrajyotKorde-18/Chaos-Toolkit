package in.strikes.demo.inventory.model;

public record InventoryResult(
        String reservationId,
        String status,
        String itemSku,
        int quantity,
        String message
) {}
