package in.strikes.chaosagent.core;

import in.strikes.chaosagent.annotation.ChaosException;
import in.strikes.chaosagent.annotation.ChaosLatency;
import in.strikes.chaosagent.model.FaultConfig;
import in.strikes.chaosagent.model.FaultType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.concurrent.ThreadLocalRandom;

@Aspect
public class ChaosInjectionAspect {

    private static final Logger log = LoggerFactory.getLogger(ChaosInjectionAspect.class);

    private final ChaosFaultRegistry registry;

    public ChaosInjectionAspect(ChaosFaultRegistry registry) {
        this.registry = registry;
    }

    @Around("@annotation(chaosLatency)")
    public Object handleLatency(ProceedingJoinPoint pjp, ChaosLatency chaosLatency) throws Throwable {
        String faultId = resolveFaultId(pjp, chaosLatency.faultId());
        FaultConfig fault = registry.getActiveFault(faultId);

        if (fault.getType() == FaultType.LATENCY && shouldFire(fault.getBlastRadiusPercent())) {
            long minMs = fault.getMinMs() > 0 ? fault.getMinMs() : chaosLatency.defaultMinMs();
            long maxMs = fault.getMaxMs() > 0 ? fault.getMaxMs() : chaosLatency.defaultMaxMs();
            if (maxMs < minMs) {
                maxMs = minMs;
            }

            long sleepMs = minMs == maxMs
                    ? minMs
                    : ThreadLocalRandom.current().nextLong(minMs, maxMs + 1);

            log.info("[chaos] injecting {}ms latency into '{}'", sleepMs, faultId);
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return pjp.proceed();
    }

    @Around("@annotation(chaosException)")
    public Object handleException(ProceedingJoinPoint pjp, ChaosException chaosException) throws Throwable {
        String faultId = resolveFaultId(pjp, chaosException.faultId());
        FaultConfig fault = registry.getActiveFault(faultId);

        if (fault.getType() == FaultType.EXCEPTION && shouldFire(fault.getBlastRadiusPercent())) {
            String message = (fault.getExceptionMessage() != null && !fault.getExceptionMessage().isBlank())
                    ? fault.getExceptionMessage()
                    : chaosException.defaultMessage();

            log.info("[chaos] injecting exception into '{}'", faultId);

            Throwable toThrow = createException(fault.getExceptionClassName(), chaosException.defaultExceptionType(), message);
            throw toThrow;
        }

        return pjp.proceed();
    }

    private String resolveFaultId(ProceedingJoinPoint pjp, String configuredFaultId) {
        if (configuredFaultId != null && !configuredFaultId.isBlank()) {
            return configuredFaultId;
        }
        if (pjp.getSignature() instanceof MethodSignature signature) {
            return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        }
        return pjp.getSignature().toShortString();
    }

    private boolean shouldFire(int blastRadiusPercent) {
        if (blastRadiusPercent <= 0) {
            return false;
        }
        if (blastRadiusPercent >= 100) {
            return true;
        }
        return ThreadLocalRandom.current().nextInt(100) < blastRadiusPercent;
    }

    private Throwable createException(String customClassName, Class<? extends Throwable> defaultType, String message) {
        if (customClassName != null && !customClassName.isBlank()) {
            try {
                Class<?> clazz = Class.forName(customClassName);
                if (Throwable.class.isAssignableFrom(clazz)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Throwable> exClass = (Class<? extends Throwable>) clazz;
                    return instantiateException(exClass, message);
                }
            } catch (Exception e) {
                log.debug("[chaos] Could not instantiate custom exception '{}', falling back: {}",
                        customClassName, e.getMessage());
            }
        }

        try {
            return instantiateException(defaultType, message);
        } catch (Exception e) {
            return new RuntimeException(message);
        }
    }

    private Throwable instantiateException(Class<? extends Throwable> exClass, String message) throws Exception {
        try {
            Constructor<? extends Throwable> ctor = exClass.getConstructor(String.class);
            return ctor.newInstance(message);
        } catch (NoSuchMethodException e) {
            Constructor<? extends Throwable> ctor = exClass.getConstructor();
            return ctor.newInstance();
        }
    }
}
