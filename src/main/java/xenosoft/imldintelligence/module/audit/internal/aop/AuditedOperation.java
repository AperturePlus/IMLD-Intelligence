package xenosoft.imldintelligence.module.audit.internal.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditedOperation {
    String action();

    String resourceType();

    String resourceIdExpression() default "";

    boolean successOnly() default true;
}
