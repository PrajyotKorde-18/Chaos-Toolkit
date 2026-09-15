package in.strikes.chaosagent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ChaosLatency {

    String faultId() default "";

    long defaultMinMs() default 100;

    long defaultMaxMs() default 500;
}