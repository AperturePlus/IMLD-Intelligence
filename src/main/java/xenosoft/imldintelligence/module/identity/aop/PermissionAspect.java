package xenosoft.imldintelligence.module.identity.aop;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import xenosoft.imldintelligence.common.CheckPermission;
import xenosoft.imldintelligence.common.RequireAnyRole;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.security.CurrentUserSubjectProvider;
import xenosoft.imldintelligence.module.identity.internal.security.RoleAuthorityUtils;
import xenosoft.imldintelligence.module.identity.internal.service.PermissionService;

import java.security.Principal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 权限切面，负责拦截带权限注解的方法并执行授权校验。
 */
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {
    private final PermissionService permissionService;
    private final CurrentUserSubjectProvider currentUserSubjectProvider;

    @Before("@annotation(checkPermission)")
    public void checkPermission(JoinPoint joinPoint, CheckPermission checkPermission) {
        UserSubject currentUser = currentUserSubjectProvider.requireCurrentSubject();
        Map<String, Object> resourceAttributes = extractResourceAttributes(joinPoint);
        resourceAttributes.putIfAbsent("tenantId", currentUser.tenantId());
        resourceAttributes.putIfAbsent("userId", currentUser.userId());

        boolean allowed = permissionService.isAllowed(
                currentUser.tenantId(),
                currentUser.userId(),
                checkPermission.resource(),
                checkPermission.action(),
                resourceAttributes
        );
        if (!allowed) {
            throw new AccessDeniedException(checkPermission.message());
        }
    }

    @Before("@annotation(requireAnyRole)")
    public void requireAnyRole(RequireAnyRole requireAnyRole) {
        UserSubject currentUser = currentUserSubjectProvider.requireCurrentSubject();
        Set<String> requiredRoles = RoleAuthorityUtils.normalizeRoleCodes(Arrays.asList(requireAnyRole.value()));
        if (requiredRoles.isEmpty()) {
            throw new IllegalArgumentException("RequireAnyRole must declare at least one role");
        }

        Set<String> currentRoles = RoleAuthorityUtils.normalizeRoleCodes(currentUser.roleCodes());
        boolean allowed = currentRoles.stream().anyMatch(requiredRoles::contains);
        if (!allowed) {
            throw new AccessDeniedException(requireAnyRole.message());
        }
    }

    private Map<String, Object> extractResourceAttributes(JoinPoint joinPoint) {
        Map<String, Object> resourceAttributes = new LinkedHashMap<>();
        if (!(joinPoint.getSignature() instanceof MethodSignature methodSignature)) {
            return resourceAttributes;
        }

        Object[] args = joinPoint.getArgs();
        String[] parameterNames = methodSignature.getParameterNames();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (shouldIgnoreArgument(arg)) {
                continue;
            }
            if (arg instanceof Map<?, ?> mapArg) {
                mapArg.forEach((key, value) -> {
                    if (key instanceof String stringKey && !stringKey.isBlank()) {
                        resourceAttributes.put(stringKey, value);
                    }
                });
                continue;
            }
            String parameterName = parameterNames != null && i < parameterNames.length && parameterNames[i] != null
                    ? parameterNames[i]
                    : "arg" + i;
            resourceAttributes.putIfAbsent(parameterName, arg);
        }
        return resourceAttributes;
    }

    private boolean shouldIgnoreArgument(Object arg) {
        return arg == null
                || arg instanceof ServletRequest
                || arg instanceof ServletResponse
                || arg instanceof Principal
                || arg instanceof Authentication
                || arg instanceof BindingResult;
    }
}
