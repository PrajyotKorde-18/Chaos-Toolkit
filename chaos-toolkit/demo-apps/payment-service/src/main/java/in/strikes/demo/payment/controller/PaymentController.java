package in.strikes.demo.payment.controller;

import in.strikes.demo.payment.model.PaymentResult;
import in.strikes.demo.payment.service.PaymentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentGateway paymentGateway;

    public PaymentController(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    @PostMapping("/charge")
    public ResponseEntity<PaymentResult> charge(
            @RequestParam(defaultValue = "tok_default") String cardToken,
            @RequestParam(defaultValue = "1000") long amountCents) {
        log.info("Received /charge request: cardToken={}, amountCents={}", cardToken, amountCents);
        PaymentResult result = paymentGateway.chargeCard(cardToken, amountCents);
        return ResponseEntity.ok(result);
    }
}
