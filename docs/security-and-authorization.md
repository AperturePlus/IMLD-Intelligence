# Security and Authorization

## Purpose

This document describes the current inbound security and authorization implementation for the modular monolith.
It applies to both SaaS and private deployment from the same codebase and keeps authorization enforcement local to the deployed environment.

## Current Structure

The implementation is split into two layers:

- Global security foundation in the identity module
- Module-owned authorization rules and service-level permission checks

### Global security foundation

Core components:

- `IdentitySecurityConfiguration`: one stateless `SecurityFilterChain`
- `JwtAuthenticationFilter`: parses `Bearer` access tokens and creates `UserSubject`
- `JwtUtil`: signs and validates `access` and `refresh` tokens
- `JwtAuthenticationEntryPoint` and `JsonAccessDeniedHandler`: JSON `401/403`
- `ModuleRequestAuthorizationCustomizer`: extension point for modules to register URL authorization rules

This means the project does not create one `SecurityFilterChain` per module.
Authentication is unified once, then individual modules only contribute their own authorization rules.

### Module-owned request authorization

Examples:

- `IdentityRequestAuthorizationCustomizer`: registers public paths such as `/api/v1/auth/**`
- `AuditRequestAuthorizationCustomizer`: protects `/api/v1/audit/**` using the audit allowlist

This keeps module rules close to the module itself while avoiding duplicated JWT filters, exception handlers, and filter ordering issues.

## JWT Flow

When `imld.security.enabled=true`:

1. `JwtAuthenticationFilter` reads `Authorization: Bearer <token>`
2. `JwtUtil` validates signature, issuer, expiration, and token type
3. A normalized `UserSubject` is placed into `SecurityContext`
4. Request-level rules from every `ModuleRequestAuthorizationCustomizer` are applied
5. Controller or service methods can perform deeper authorization through annotations such as `@CheckPermission`

Default configuration keys:

- `imld.security.enabled`
- `imld.security.public-paths`
- `imld.security.jwt.issuer`
- `imld.security.jwt.secret`
- `imld.security.jwt.access-token-ttl`
- `imld.security.jwt.refresh-token-ttl`
- `imld.security.jwt.clock-skew`

`JWT` support requires a secret of at least `32` bytes.

## Current User Context

`CurrentUserSubjectProvider` is the bridge between Spring Security and business authorization.
It reads the authenticated `UserSubject` from `SecurityContext` and normalizes role codes.

If no authenticated subject exists, permission annotations fail closed.

## Authorization Annotations

### `@RequireAnyRole`

Use for direct role checks when the rule is simple.

Example:

```java
@RequireAnyRole({"SYSTEM_ADMIN", "COMPLIANCE_AUDITOR"})
public void querySensitiveAuditLogs() {
    ...
}
```

Behavior:

- Reads current roles from `SecurityContext`
- Normalizes role codes such as `doctor`, `DOCTOR`, and `ROLE_DOCTOR` into the same logical role
- Throws `403` when no declared role matches

### `@CheckPermission`

Use for resource and action checks that should delegate to the permission service.

Example:

```java
@CheckPermission(resource = "PATIENT", action = "READ", message = "Patient access denied")
public PatientDto getPatient(Map<String, Object> resourceAttributes) {
    ...
}
```

Behavior:

- Requires an authenticated `UserSubject`
- Builds a resource-attribute map from method arguments
- Injects current `tenantId` and `userId` into that map
- Delegates the final decision to `IPermissionService`
- Throws `403` with the annotation message when denied

### Resource attribute extraction

`PermissionAspect` currently extracts resource attributes as follows:

- `Map<String, Object>` parameters are merged directly into the attribute map
- Other business parameters are added by parameter name when available
- Servlet request/response, `Principal`, `Authentication`, and `BindingResult` are ignored

This keeps the annotation usable on both controller and service methods without coupling the permission engine to web-only types.

## Permission Service Behavior

`PermissionSerivce` currently implements a minimal fail-closed authorization model:

1. Load active `UserAccount`
2. Load effective roles from `user_role_rel` and `role`
3. Allow immediately for `SYSTEM_ADMIN`
4. Evaluate active tenant-scoped `abac_policy` rows by ascending priority
5. Return the first matched policy effect
6. If no policy matches, deny by default

This avoids accidental privilege from missing data.

## Supported ABAC Policy Shape

The current evaluator supports a practical subset of JSON expressions.

### `subject_expr`

Supported keys:

- `role`, `roleCode`, `roles`, `roleCodes`
- `userType`
- `deptName`
- `tenantId`
- `userId`

Example:

```json
{
  "role": ["DOCTOR"],
  "deptName": "ICU"
}
```

### `resource_expr`

Supported behavior:

- `resource` or `resourceType` matches the declared resource
- Any other field is matched against the resource-attribute map from the intercepted method call
- Text values may reference the current subject via `$subject.<field>`

Example:

```json
{
  "resource": "PATIENT",
  "ownerUserId": "$subject.userId"
}
```

### `action_expr`

Supported keys:

- `action`
- `actions`

Example:

```json
{
  "actions": ["READ", "UPDATE"]
}
```

### Field-level operators

For a single field condition, the evaluator supports:

- scalar equality
- array any-match
- `{"eq": ...}`
- `{"anyOf": [...]}`
- `{"in": [...]}`
- `{"exists": true|false}`

## Audit Module Integration

The audit module now prefers authorities from `SecurityContext` for query access checks.
Only when Spring Security is not active does it fall back to request headers.
This prevents a client from bypassing audit authorization by forging `X-User-Role` while JWT security is enabled.

## What Is Still Not Implemented

The following are intentionally still outside the current scope:

- login service and token issuance business flow
- token revocation / blacklist
- method-level SpEL permission expressions
- a full policy DSL with nested boolean logic
- distributed permission cache or invalidation

## Testing

Current automated coverage includes:

- JWT utility tests
- JWT filter and security-chain integration tests
- module request-authorization customizer tests
- audit access-guard tests
- permission service policy-evaluation tests
- annotation aspect tests for `@CheckPermission` and `@RequireAnyRole`
