package in.strikes.demo.inventory.service;

import in.strikes.chaosagent.annotation.ChaosException;
import in.strikes.chaosagent.annotation.ChaosLatency;
import in.strikes.demo.inventory.model.InventoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InventoryGateway {

    private static final Logger log = LoggerFactory.getLogger(InventoryGateway.class);

    @ChaosLatency(faultId = "inventory-service.reserve", defaultMinMs = 100, defaultMaxMs = 400)
    @ChaosException(faultId = "inventory-service.reserve", defaultMessage = "Simulated inventory service failure")
    public InventoryResult reserveItem(String itemSku, int quantity) {
        log.info("Processing inventory reservation: sku={}, quantity={}", itemSku, quantity);
        String resId = "res_" + UUID.randomUUID().toString().substring(0, 8);
        return new InventoryResult(resId, "RESERVED", itemSku, quantity, "Item successfully reserved");
    }
}
