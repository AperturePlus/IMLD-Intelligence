package xenosoft.imldintelligence.module.community.internal.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.module.community.internal.config.CommunityProperties;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;

import java.util.Objects;

/**
 * Enforces tenant boundary for community APIs.
 *
 * <p>When community is configured as GLOBAL scope, all requests must target the global community tenant,
 * and the tenant header must match authenticated subject tenant to prevent header swapping.</p>
 */
@Component
@RequiredArgsConstructor
public class CommunityTenantAccessGuard {

    private final CommunityProperties communityProperties;
    private final TenantRepository tenantRepository;

    private volatile Long cachedGlobalTenantId;

    public long requireTenantMatch(Long tenantHeaderId, Long subjectTenantId) {
        if (tenantHeaderId == null || tenantHeaderId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Tenant-Id header is required");
        }
        if (subjectTenantId == null || subjectTenantId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated tenant context is required");
        }
        if (!Objects.equals(tenantHeaderId, subjectTenantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "X-Tenant-Id header does not match authenticated tenant");
        }

        if (communityProperties.getScope() == CommunityProperties.Scope.GLOBAL) {
            long globalTenantId = resolveGlobalCommunityTenantId();
            if (!Objects.equals(tenantHeaderId, globalTenantId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant is not allowed for global community scope");
            }
        }

        return tenantHeaderId;
    }

    public long resolveGlobalCommunityTenantId() {
        if (communityProperties.getScope() != CommunityProperties.Scope.GLOBAL) {
            throw new IllegalStateException("Global community tenant is only available when scope=GLOBAL");
        }
        Long cached = cachedGlobalTenantId;
        if (cached != null) {
            return cached;
        }

        String tenantCode = communityProperties.getGlobalTenantCode();
        if (tenantCode == null || tenantCode.isBlank()) {
            throw new IllegalStateException("imld.community.globalTenantCode must be configured when scope=GLOBAL");
        }
        Tenant tenant = tenantRepository.findByTenantCode(tenantCode.trim())
                .filter(t -> "ACTIVE".equalsIgnoreCase(t.getStatus()))
                .orElseThrow(() -> new IllegalStateException("Global community tenant not found or inactive: " + tenantCode));

        cachedGlobalTenantId = tenant.getId();
        return tenant.getId();
    }
}

