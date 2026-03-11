# Audit Logging and Traceability

## Purpose

This module provides append-only audit and traceability capabilities for the medical platform with secure defaults:

- `audit_log`
- `sensitive_data_access_log`
- `model_invocation_log`

It is designed to support SaaS and private deployment using the same codebase and does not add any outbound data flow.

## Data Dictionary

## `audit_log`

- `tenant_id`: tenant scope for strict multi-tenant isolation.
- `user_id`: operator account id (nullable for system-level operations).
- `user_role`: operator role at operation time.
- `action`: operation type, for example `CREATE/READ/UPDATE/DELETE/SIGN/EXPORT`.
- `resource_type`: business resource type, for example `PATIENT/REPORT`.
- `resource_id`: target resource identifier.
- `before_data` and `after_data`: sanitized change snapshots.
- `ip_address`, `device_info`, `app_version`, `trace_id`: traceability fields.
- `created_at`: write timestamp.

## `sensitive_data_access_log`

- `tenant_id`, `user_id`: tenant and operator.
- `sensitive_type`: sensitive category such as `GENETIC/ID_CARD/MEDICAL_RECORD`.
- `resource_type`, `resource_id`: target resource.
- `access_reason`: business reason.
- `access_result`: `ALLOW` or `DENY`.
- `ip_address`, `created_at`.

## `model_invocation_log`

- `tenant_id`: tenant scope.
- `session_id`, `model_registry_id`: diagnosis/model reference.
- `provider`, `request_id`: invocation source and id.
- `input_digest`, `output_digest`: digest instead of plaintext payload.
- `latency_ms`, `token_in`, `token_out`, `cost_amount`.
- `status`, `error_message`, `created_at`.

## Sanitization and Payload Limits

`before_data` and `after_data` are sanitized before persistence:

- Recursive field-name based masking (case-insensitive normalization).
- Default masked fields:
  - `idNo`, `id_card`, `mobile`, `phone`, `email`, `address`
  - `password`, `token`, `authorization`
  - `patientName`, `guardianName`
- If payload size exceeds `imld.audit.max-payload-bytes` (default `8192`), the payload is replaced by a summary object:
  - `_truncated=true`
  - `_originalSizeBytes`
  - `_maxPayloadBytes`
  - `_sha256`

## Request Context and Traceability

`AuditContextFilter` reads context from headers:

- `X-Tenant-Id`
- `X-User-Id`
- `X-User-Role`
- `X-Trace-Id`
- `X-App-Version`
- `X-Device-Info`
- `X-Forwarded-For` (first IP used)

If `X-Trace-Id` is missing, a UUID is generated. `traceId` is also put into MDC for request-scope log correlation.

## Query API

## Endpoints

- `GET /api/v1/audit/logs`
- `GET /api/v1/audit/sensitive-access-logs`
- `GET /api/v1/audit/model-invocation-logs`

## Required headers

- `X-Tenant-Id`
- `X-User-Role`

## Authorization

Role allowlist is configured by `imld.audit.query-role-allowlist`.
Default allowlist:

- `SYSTEM_ADMIN`
- `COMPLIANCE_AUDITOR`

Any non-allowlisted role is denied with `403`.

When Spring Security is enabled, audit query authorization now prefers authorities from `SecurityContext` and only falls back to `X-User-Role` for non-authenticated compatibility paths. This avoids forged role headers bypassing the allowlist under JWT-based authentication.

## Pagination

- Request params: `page`, `size`
- Defaults: `page=0`, `size=imld.audit.default-page-size` (`20`)
- Max size: `imld.audit.max-page-size` (`200`), requests above max are capped.

## Integration Guide

## Manual recording

Use `AuditTrailService` in business services:

- `recordAudit(...)`
- `recordSensitiveAccess(...)`
- `recordModelInvocation(...)`

## Annotation-based recording

Use AOP annotations for low-friction integration:

- `@AuditedOperation(action, resourceType, resourceIdExpression, successOnly)`
- `@SensitiveAccessed(sensitiveType, resourceType, resourceIdExpression, reasonExpression)`

SpEL can reference method args (`#p0`) and method result (`#result`).

## Security and Compliance Notes

- Audit writes are fail-closed by default (`imld.audit.fail-closed=true`): write failure throws and can roll back transaction.
- The implementation is local persistence only; no cloud egress is introduced by this module.
- Plaintext PHI should not be logged through application logs; only sanitized payloads are persisted in audit tables.

## Contract Alignment

The audit module now also declares `AuditQueryApi` as the contract-first HTTP boundary and groups query parameters in `AuditQueryDtos.Query`.
This keeps the existing runtime controller unchanged while making the audit module consistent with the DTO categorization introduced for the other modules.

## Error Responses

Audit query success payloads keep their existing paged response structure.
When request validation or access checks fail, `GlobalExceptionHandler` now converts errors into the shared `ApiResponse<Void>` envelope while preserving the original HTTP status code.
