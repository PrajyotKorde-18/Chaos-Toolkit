package in.strikes.chaosagent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ChaosException {

    String faultId() default "";

    Class<? extends Throwable> defaultExceptionType() default RuntimeException.class;

    String defaultMessage() default "Injected by chaos-agent";
}