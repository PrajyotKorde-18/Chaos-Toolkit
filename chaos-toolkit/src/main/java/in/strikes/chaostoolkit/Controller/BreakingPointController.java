package in.strikes.chaostoolkit.Controller;

import in.strikes.chaostoolkit.model.breakingpoint.BreakingPointReport;
import in.strikes.chaostoolkit.model.breakingpoint.BreakingPointRequest;
import in.strikes.chaostoolkit.service.breakingpoint.BreakingPointService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/breaking-point")
@CrossOrigin(origins = "*")
public class BreakingPointController {

    private final BreakingPointService breakingPointService;

    public BreakingPointController(BreakingPointService breakingPointService) {
        this.breakingPointService = breakingPointService;
    }

    @RequestMapping(value = "/find", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<BreakingPointReport> findBreakingPoint(
            @RequestParam(defaultValue = "payment-service") String targetService,
            @RequestParam(defaultValue = "http://localhost:8080/orders/create") String callerEndpointUrl,
            @RequestParam(defaultValue = "200") Integer startLatencyMs,
            @RequestParam(defaultValue = "400") Integer stepLatencyMs,
            @RequestParam(defaultValue = "2600") Integer maxLatencyMs,
            @RequestParam(defaultValue = "1200") Integer maxPermissibleLatencyMs,
            @RequestParam(defaultValue = "0.05") Double maxPermissibleErrorRate,
            @RequestParam(defaultValue = "4") Integer requestsPerStep,
            @RequestBody(required = false) BreakingPointRequest body
    ) {
        BreakingPointRequest req;
        if (body != null) {
            req = body;
        } else {
            req = new BreakingPointRequest(
                    targetService,
                    targetService + ".chargeCard",
                    startLatencyMs,
                    stepLatencyMs,
                    maxLatencyMs,
                    maxPermissibleLatencyMs,
                    maxPermissibleErrorRate,
                    callerEndpointUrl,
                    requestsPerStep
            );
        }
        BreakingPointReport report = breakingPointService.findBreakingPoint(req);
        return ResponseEntity.ok(report);
    }
}
