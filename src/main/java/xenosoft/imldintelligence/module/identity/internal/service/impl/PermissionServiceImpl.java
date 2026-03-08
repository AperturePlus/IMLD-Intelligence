package xenosoft.imldintelligence.module.identity.internal.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xenosoft.imldintelligence.module.identity.internal.model.AbacPolicy;
import xenosoft.imldintelligence.module.identity.internal.model.Role;
import xenosoft.imldintelligence.module.identity.internal.model.UserAccount;
import xenosoft.imldintelligence.module.identity.internal.model.UserRoleRel;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.repository.AbacPolicyRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.RoleRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserAccountRepository;
import xenosoft.imldintelligence.module.identity.internal.repository.UserRoleRelRepository;
import xenosoft.imldintelligence.module.identity.internal.security.RoleAuthorityUtils;
import xenosoft.imldintelligence.module.identity.internal.service.PermissionService;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 权限服务实现类，负责基于角色与策略完成授权判定。
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private static final Set<String> SUPER_ROLES = Set.of("SYSTEM_ADMIN");
    private static final int DEFAULT_POLICY_PRIORITY = 100;

    private final UserAccountRepository userAccountRepository;
    private final UserRoleRelRepository userRoleRelRepository;
    private final RoleRepository roleRepository;
    private final AbacPolicyRepository abacPolicyRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAllowed(Long tenantId, Long userId, String res, String action, Map<String, Object> resAttr) {
        if (!isPositive(tenantId) || !isPositive(userId) || !hasText(res) || !hasText(action)) {
            return false;
        }

        UserSubject subject = loadSubject(tenantId, userId);
        if (subject == null) {
            return false;
        }
        if (subject.roleCodes().stream().anyMatch(SUPER_ROLES::contains)) {
            return true;
        }

        String normalizedResource = normalizeText(res);
        String normalizedAction = normalizeText(action);
        Map<String, Object> normalizedResourceAttributes = normalizeResourceAttributes(resAttr);

        List<AbacPolicy> policies = abacPolicyRepository.listByTenantId(tenantId).stream()
                .filter(this::isActivePolicy)
                .sorted(Comparator.comparingInt(policy -> policy.getPriority() == null ? DEFAULT_POLICY_PRIORITY : policy.getPriority()))
                .toList();

        for (AbacPolicy policy : policies) {
            if (matchesPolicy(policy, subject, normalizedResource, normalizedAction, normalizedResourceAttributes)) {
                return "ALLOW".equalsIgnoreCase(trimToNull(policy.getEffect()));
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getEffectiveRoleCodes(Long tenantId, Long userId) {
        if (!isPositive(tenantId) || !isPositive(userId)) {
            return Set.of();
        }

        LinkedHashSet<Long> roleIds = userRoleRelRepository.listByUserId(tenantId, userId).stream()
                .map(UserRoleRel::getRoleId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (roleIds.isEmpty()) {
            return Set.of();
        }

        List<String> roleCodes = roleIds.stream()
                .map(roleId -> roleRepository.findById(tenantId, roleId))
                .flatMap(java.util.Optional::stream)
                .filter(this::isActiveRole)
                .map(Role::getRoleCode)
                .toList();
        return RoleAuthorityUtils.normalizeRoleCodes(roleCodes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserSubject loadSubject(Long tenantId, Long userId) {
        return userAccountRepository.findById(tenantId, userId)
                .filter(this::isActiveUser)
                .map(userAccount -> new UserSubject(
                        userAccount.getId(),
                        userAccount.getTenantId(),
                        normalizeText(userAccount.getUserType()),
                        trimToNull(userAccount.getDeptName()),
                        getEffectiveRoleCodes(tenantId, userId)
                ))
                .orElse(null);
    }

    private boolean matchesPolicy(AbacPolicy policy,
                                  UserSubject subject,
                                  String resource,
                                  String action,
                                  Map<String, Object> resourceAttributes) {
        return matchesSubjectExpr(policy.getSubjectExpr(), subject)
                && matchesActionExpr(policy.getActionExpr(), action, subject)
                && matchesResourceExpr(policy.getResourceExpr(), resource, resourceAttributes, subject);
    }

    private boolean matchesSubjectExpr(JsonNode subjectExpr, UserSubject subject) {
        if (isEmptyNode(subjectExpr)) {
            return true;
        }
        JsonNode roleNode = firstNonNull(subjectExpr.get("roleCodes"), subjectExpr.get("roles"), subjectExpr.get("roleCode"), subjectExpr.get("role"));
        return matchesCondition(roleNode, subject.roleCodes(), subject)
                && matchesCondition(subjectExpr.get("userType"), subject.userType(), subject)
                && matchesCondition(subjectExpr.get("deptName"), subject.deptName(), subject)
                && matchesCondition(subjectExpr.get("tenantId"), subject.tenantId(), subject)
                && matchesCondition(subjectExpr.get("userId"), subject.userId(), subject);
    }

    private boolean matchesActionExpr(JsonNode actionExpr, String action, UserSubject subject) {
        if (isEmptyNode(actionExpr)) {
            return true;
        }
        JsonNode actionNode = firstNonNull(actionExpr.get("actions"), actionExpr.get("action"));
        return matchesCondition(actionNode, action, subject);
    }

    private boolean matchesResourceExpr(JsonNode resourceExpr,
                                        String resource,
                                        Map<String, Object> resourceAttributes,
                                        UserSubject subject) {
        if (isEmptyNode(resourceExpr)) {
            return true;
        }
        if (!matchesCondition(firstNonNull(resourceExpr.get("resourceType"), resourceExpr.get("resource")), resource, subject)) {
            return false;
        }
        java.util.Iterator<Map.Entry<String, JsonNode>> fields = resourceExpr.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String field = entry.getKey();
            if ("resourceType".equals(field) || "resource".equals(field)) {
                continue;
            }
            if (!matchesCondition(entry.getValue(), resourceAttributes.get(field), subject)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesCondition(JsonNode condition, Object actual, UserSubject subject) {
        if (condition == null || condition.isMissingNode()) {
            return true;
        }
        if (condition.isNull()) {
            return actual == null;
        }
        if (condition.isObject()) {
            if (condition.has("eq")) {
                return matchesCondition(condition.get("eq"), actual, subject);
            }
            if (condition.has("anyOf")) {
                return matchesAny(condition.get("anyOf"), actual, subject);
            }
            if (condition.has("in")) {
                return matchesAny(condition.get("in"), actual, subject);
            }
            if (condition.has("exists")) {
                return condition.get("exists").asBoolean() == (actual != null);
            }
            return false;
        }
        if (condition.isArray()) {
            return matchesAny(condition, actual, subject);
        }
        Object expected = resolveExpectedValue(condition, subject);
        return matchesValue(expected, actual);
    }

    private boolean matchesAny(JsonNode values, Object actual, UserSubject subject) {
        if (values == null || values.isMissingNode() || !values.isArray()) {
            return false;
        }
        for (JsonNode candidate : values) {
            if (matchesCondition(candidate, actual, subject)) {
                return true;
            }
        }
        return false;
    }

    private Object resolveExpectedValue(JsonNode condition, UserSubject subject) {
        if (condition.isTextual()) {
            String textValue = trimToNull(condition.asText());
            if (textValue != null && textValue.startsWith("$subject.")) {
                return resolveSubjectValue(subject, textValue.substring("$subject.".length()));
            }
            return textValue;
        }
        if (condition.isNumber()) {
            return condition.numberValue();
        }
        if (condition.isBoolean()) {
            return condition.booleanValue();
        }
        return condition.toString();
    }

    private Object resolveSubjectValue(UserSubject subject, String fieldName) {
        return switch (fieldName) {
            case "userId" -> subject.userId();
            case "tenantId" -> subject.tenantId();
            case "userType" -> subject.userType();
            case "deptName" -> subject.deptName();
            case "roleCodes" -> subject.roleCodes();
            default -> null;
        };
    }

    private boolean matchesValue(Object expected, Object actual) {
        if (actual instanceof Collection<?> collection) {
            return collection.stream().anyMatch(item -> matchesValue(expected, item));
        }
        if (actual != null && actual.getClass().isArray()) {
            int length = Array.getLength(actual);
            for (int i = 0; i < length; i++) {
                if (matchesValue(expected, Array.get(actual, i))) {
                    return true;
                }
            }
            return false;
        }
        if (expected == null) {
            return actual == null;
        }
        if (actual == null) {
            return false;
        }
        if (expected instanceof Number || actual instanceof Number) {
            return normalizeScalar(expected).equals(normalizeScalar(actual));
        }
        if (expected instanceof Boolean || actual instanceof Boolean) {
            return Boolean.parseBoolean(String.valueOf(expected)) == Boolean.parseBoolean(String.valueOf(actual));
        }
        return normalizeText(String.valueOf(expected)).equals(normalizeText(String.valueOf(actual)));
    }

    private Object normalizeScalar(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.toString();
        }
        return trimToNull(String.valueOf(value));
    }

    private Map<String, Object> normalizeResourceAttributes(Map<String, Object> resourceAttributes) {
        if (resourceAttributes == null || resourceAttributes.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        resourceAttributes.forEach((key, value) -> {
            String normalizedKey = trimToNull(key);
            if (normalizedKey != null) {
                normalized.put(normalizedKey, value);
            }
        });
        return normalized;
    }

    private JsonNode firstNonNull(JsonNode... nodes) {
        for (JsonNode node : nodes) {
            if (node != null && !node.isMissingNode()) {
                return node;
            }
        }
        return null;
    }

    private boolean isActivePolicy(AbacPolicy policy) {
        return policy != null && "ACTIVE".equalsIgnoreCase(trimToNull(policy.getStatus()));
    }

    private boolean isActiveRole(Role role) {
        return role != null && "ACTIVE".equalsIgnoreCase(trimToNull(role.getStatus()));
    }

    private boolean isActiveUser(UserAccount userAccount) {
        return userAccount != null && "ACTIVE".equalsIgnoreCase(trimToNull(userAccount.getStatus()));
    }

    private boolean isEmptyNode(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() || (node.isObject() && node.isEmpty());
    }

    private boolean isPositive(Long value) {
        return value != null && value > 0;
    }

    private boolean hasText(String value) {
        return trimToNull(value) != null;
    }

    private String normalizeText(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalized.toUpperCase(java.util.Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
