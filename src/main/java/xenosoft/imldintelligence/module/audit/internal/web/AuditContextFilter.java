package xenosoft.imldintelligence.module.audit.internal.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import xenosoft.imldintelligence.module.audit.internal.context.AuditContext;
import xenosoft.imldintelligence.module.audit.internal.context.AuditContextHolder;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@ConditionalOnProperty(prefix = "imld.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        AuditContext context = new AuditContext();
        context.setTenantId(parseLong(request.getHeader(AuditHeaderNames.TENANT_ID)));
        context.setUserId(parseLong(request.getHeader(AuditHeaderNames.USER_ID)));
        context.setUserRole(request.getHeader(AuditHeaderNames.USER_ROLE));
        context.setAppVersion(request.getHeader(AuditHeaderNames.APP_VERSION));
        context.setDeviceInfo(request.getHeader(AuditHeaderNames.DEVICE_INFO));
        context.setIpAddress(resolveIpAddress(request));

        String traceId = request.getHeader(AuditHeaderNames.TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        context.setTraceId(traceId);

        AuditContextHolder.set(context);
        MDC.put("traceId", traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("traceId");
            AuditContextHolder.clear();
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader(AuditHeaderNames.FORWARDED_FOR);
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] segments = forwardedFor.split(",");
            if (segments.length > 0) {
                return segments[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
