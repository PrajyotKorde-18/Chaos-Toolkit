package in.strikes.demo.order.model;

public record CircuitBreakerInfo(
        String name,
        String state,
        float failureRate,
        int bufferedCalls,
        int failedCalls,
        int successfulCalls,
        long notPermittedCalls
) {}
