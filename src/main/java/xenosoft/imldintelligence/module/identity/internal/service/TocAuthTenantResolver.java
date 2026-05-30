package xenosoft.imldintelligence.module.identity.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import xenosoft.imldintelligence.module.identity.internal.model.Tenant;
import xenosoft.imldintelligence.module.identity.internal.repository.TenantRepository;

/**
 * Resolves the global community tenant for ToC auth flows.
 */
@Component
@RequiredArgsConstructor
public class TocAuthTenantResolver {

    private final TenantRepository tenantRepository;

    @Value("${imld.community.scope:TENANT}")
    private String communityScope;

    @Value("${imld.community.global-tenant-code:}")
    private String globalTenantCode;

    private volatile Long cachedGlobalTenantId;

    public long requireGlobalTenantId() {
        if (!"GLOBAL".equalsIgnoreCase(communityScope)) {
            throw new IllegalStateException("ToC auth requires imld.community.scope=GLOBAL");
        }
        Long cached = cachedGlobalTenantId;
        if (cached != null) {
            return cached;
        }
        if (globalTenantCode == null || globalTenantCode.isBlank()) {
            throw new IllegalStateException("imld.community.global-tenant-code must be configured when scope=GLOBAL");
        }
        Tenant tenant = tenantRepository.findByTenantCode(globalTenantCode.trim())
                .filter(t -> "ACTIVE".equalsIgnoreCase(t.getStatus()))
                .orElseThrow(() -> new IllegalStateException("Global community tenant not found or inactive: " + globalTenantCode));

        cachedGlobalTenantId = tenant.getId();
        return tenant.getId();
    }
}

