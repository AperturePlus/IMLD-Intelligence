package xenosoft.imldintelligence.module.identity.internal.security;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class RoleAuthorityUtils {
    private static final String ROLE_PREFIX = "ROLE_";

    private RoleAuthorityUtils() {
    }

    public static Set<String> normalizeRoleCodes(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            String roleCode = normalizeRoleCode(value);
            if (roleCode != null) {
                normalized.add(roleCode);
            }
        }
        return normalized.isEmpty() ? Set.of() : Set.copyOf(normalized);
    }

    public static String normalizeRoleCode(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        normalized = normalized.toUpperCase(Locale.ROOT);
        if (normalized.startsWith(ROLE_PREFIX)) {
            normalized = normalized.substring(ROLE_PREFIX.length());
        }
        return normalized.isBlank() ? null : normalized;
    }

    public static Set<String> expandAuthorityNames(Collection<String> roleCodes) {
        Set<String> normalizedRoleCodes = normalizeRoleCodes(roleCodes);
        if (normalizedRoleCodes.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> authorities = new LinkedHashSet<>();
        for (String roleCode : normalizedRoleCodes) {
            authorities.add(roleCode);
            authorities.add(ROLE_PREFIX + roleCode);
        }
        return Set.copyOf(authorities);
    }
}

