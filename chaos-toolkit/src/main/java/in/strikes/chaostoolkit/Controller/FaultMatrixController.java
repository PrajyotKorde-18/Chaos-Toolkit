package in.strikes.chaostoolkit.Controller;

import in.strikes.chaostoolkit.model.matrix.FaultMatrixReport;
import in.strikes.chaostoolkit.service.matrix.FaultMatrixService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fault-matrix")
@CrossOrigin(origins = "*")
public class FaultMatrixController {

    private final FaultMatrixService faultMatrixService;

    public FaultMatrixController(FaultMatrixService faultMatrixService) {
        this.faultMatrixService = faultMatrixService;
    }

    @PostMapping("/run")
    public ResponseEntity<FaultMatrixReport> runFaultMatrix(
            @RequestParam(defaultValue = "payment-service") String targetService,
            @RequestParam(defaultValue = "http://localhost:8080/orders/create") String callerEndpointUrl
    ) {
        FaultMatrixReport report = faultMatrixService.runFaultMatrix(targetService, callerEndpointUrl);
        return ResponseEntity.ok(report);
    }
}
