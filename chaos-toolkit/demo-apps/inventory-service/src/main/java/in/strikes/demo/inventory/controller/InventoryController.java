package in.strikes.demo.inventory.controller;

import in.strikes.demo.inventory.model.InventoryResult;
import in.strikes.demo.inventory.service.InventoryGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InventoryController {

    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);

    private final InventoryGateway inventoryGateway;

    public InventoryController(InventoryGateway inventoryGateway) {
        this.inventoryGateway = inventoryGateway;
    }

    @PostMapping("/inventory/reserve")
    public ResponseEntity<InventoryResult> reserve(
            @RequestParam(defaultValue = "SKU-TEST-001") String itemSku,
            @RequestParam(defaultValue = "1") int quantity) {
        log.info("Received /inventory/reserve request: sku={}, quantity={}", itemSku, quantity);
        InventoryResult result = inventoryGateway.reserveItem(itemSku, quantity);
        return ResponseEntity.ok(result);
    }
}
