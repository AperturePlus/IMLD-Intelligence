package xenosoft.imldintelligence.module.audit.internal.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.module.audit.internal.config.AuditProperties;
import xenosoft.imldintelligence.module.audit.internal.web.AuditHeaderNames;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuditQueryAccessGuard {
    private final AuditProperties properties;

    public AuditQueryAccessGuard(AuditProperties properties) {
        this.properties = properties;
    }

    public void assertAllowed(HttpServletRequest request) {
        String roleHeader = request.getHeader(AuditHeaderNames.USER_ROLE);
        if (roleHeader == null || roleHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role is required");
        }

        Set<String> requestRoles = Arrays.stream(roleHeader.split(","))
                .map(value -> value == null ? "" : value.trim())
                .filter(value -> !value.isEmpty())
                .map(value -> value.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());

        Set<String> allowlist = properties.getQueryRoleAllowlist().stream()
                .map(value -> value == null ? "" : value.trim())
                .filter(value -> !value.isEmpty())
                .map(value -> value.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());

        boolean allowed = requestRoles.stream().anyMatch(allowlist::contains);
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role is not allowed to query audit logs");
        }
    }
}
