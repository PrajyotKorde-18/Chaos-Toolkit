package in.strikes.chaostoolkit.Controller;

import in.strikes.chaostoolkit.model.FaultConfig;
import in.strikes.chaostoolkit.service.FaultStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FaultQueryController {

    private final FaultStore faultStore;

    public FaultQueryController(FaultStore faultStore) {
        this.faultStore = faultStore;
    }

    @GetMapping("/api/v1/faults/active")
    public List<FaultConfig> getActiveFaults(@RequestParam String service) {
        return faultStore.getActiveFaultsFor(service);
    }
}
