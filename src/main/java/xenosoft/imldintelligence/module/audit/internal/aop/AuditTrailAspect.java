package xenosoft.imldintelligence.module.audit.internal.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import xenosoft.imldintelligence.module.audit.internal.service.AuditTrailService;
import xenosoft.imldintelligence.module.audit.internal.service.command.AuditRecordCommand;
import xenosoft.imldintelligence.module.audit.internal.service.command.SensitiveAccessRecordCommand;

import java.lang.reflect.Method;

/**
 * Intercepts audit annotations and persists audit events around business method execution.
 *
 * <p>The aspect intentionally captures both successful and failed executions so security-relevant
 * traces are preserved even when business logic throws.</p>
 */
@Aspect
@Component
@ConditionalOnProperty(prefix = "imld.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditTrailAspect {
    private final AuditTrailService auditTrailService;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public AuditTrailAspect(AuditTrailService auditTrailService) {
        this.auditTrailService = auditTrailService;
    }

    /**
     * Records operation-level audit logs declared with {@link AuditedOperation}.
     */
    @Around("@annotation(auditedOperation)")
    public Object aroundAuditedOperation(ProceedingJoinPoint joinPoint, AuditedOperation auditedOperation) throws Throwable {
        Object result = null;
        Throwable businessError = null;

        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            businessError = ex;
        }

        if (businessError == null || !auditedOperation.successOnly()) {
            Method method = resolveMethod(joinPoint);
            String resourceId = evaluateExpression(auditedOperation.resourceIdExpression(), method, joinPoint.getArgs(), result, businessError);

            AuditRecordCommand command = new AuditRecordCommand();
            command.setAction(auditedOperation.action());
            command.setResourceType(auditedOperation.resourceType());
            command.setResourceId(resourceId);
            command.setTraceId(null);

            try {
                auditTrailService.recordAudit(command);
            } catch (RuntimeException auditEx) {
                if (businessError == null) {
                    throw auditEx;
                }
                businessError.addSuppressed(auditEx);
            }
        }

        if (businessError != null) {
            throw businessError;
        }
        return result;
    }

    /**
     * Records sensitive-data access logs declared with {@link SensitiveAccessed}.
     */
    @Around("@annotation(sensitiveAccessed)")
    public Object aroundSensitiveAccess(ProceedingJoinPoint joinPoint, SensitiveAccessed sensitiveAccessed) throws Throwable {
        Object result = null;
        Throwable businessError = null;

        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            businessError = ex;
        }

        Method method = resolveMethod(joinPoint);
        String resourceId = evaluateExpression(sensitiveAccessed.resourceIdExpression(), method, joinPoint.getArgs(), result, businessError);
        String reason = evaluateExpression(sensitiveAccessed.reasonExpression(), method, joinPoint.getArgs(), result, businessError);

        SensitiveAccessRecordCommand command = new SensitiveAccessRecordCommand();
        command.setSensitiveType(sensitiveAccessed.sensitiveType());
        command.setResourceType(sensitiveAccessed.resourceType());
        command.setResourceId(resourceId);
        command.setAccessReason(reason);
        command.setAccessResult(businessError == null ? "ALLOW" : "DENY");

        try {
            auditTrailService.recordSensitiveAccess(command);
        } catch (RuntimeException auditEx) {
            if (businessError == null) {
                throw auditEx;
            }
            businessError.addSuppressed(auditEx);
        }

        if (businessError != null) {
            throw businessError;
        }
        return result;
    }

    /**
     * Evaluates a SpEL expression against method arguments and execution result/error variables.
     */
    private String evaluateExpression(String expression, Method method, Object[] args, Object result, Throwable error) {
        if (expression == null || expression.isBlank()) {
            return null;
        }

        org.springframework.expression.EvaluationContext context =
                new org.springframework.context.expression.MethodBasedEvaluationContext(
                        null,
                        method,
                        args,
                        parameterNameDiscoverer
                );
        context.setVariable("result", result);
        context.setVariable("error", error);

        Object value = expressionParser.parseExpression(expression).getValue(context);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * Resolves the concrete method on the target class so parameter names and annotations are stable.
     */
    private Method resolveMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = joinPoint.getTarget() == null ? method.getDeclaringClass() : joinPoint.getTarget().getClass();
        return AopUtils.getMostSpecificMethod(method, targetClass);
    }
}
