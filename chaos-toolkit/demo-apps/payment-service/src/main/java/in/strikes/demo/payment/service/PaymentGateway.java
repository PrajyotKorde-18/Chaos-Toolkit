package in.strikes.demo.payment.service;

import in.strikes.chaosagent.annotation.ChaosException;
import in.strikes.chaosagent.annotation.ChaosLatency;
import in.strikes.demo.payment.model.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(PaymentGateway.class);

    @ChaosLatency(faultId = "payment-service.chargeCard", defaultMinMs = 100, defaultMaxMs = 400)
    @ChaosException(faultId = "payment-service.chargeCard", defaultMessage = "Simulated payment gateway failure")
    public PaymentResult chargeCard(String cardToken, long amountCents) {
        log.info("Processing card charge: cardToken={}, amountCents={}", cardToken, amountCents);
        String txnId = "txn_" + UUID.randomUUID().toString().substring(0, 8);
        return new PaymentResult(txnId, "SUCCESS", amountCents, "Charge successful");
    }
}
