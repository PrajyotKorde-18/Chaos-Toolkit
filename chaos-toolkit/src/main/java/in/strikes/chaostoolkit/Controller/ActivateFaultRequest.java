package in.strikes.chaostoolkit.Controller;

import in.strikes.chaostoolkit.model.FaultType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ActivateFaultRequest {

    @NotBlank
    public String serviceName;

    @NotBlank
    public String faultId;

    @NotNull
    public FaultType type;

    @Min(0)
    @Max(100)
    public int blastRadiusPercent = 100;

    public long minMs;
    public long maxMs;

    public String exceptionClassName;
    public String exceptionMessage;

    public long durationSeconds;
}
