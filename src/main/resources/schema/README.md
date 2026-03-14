# Schema Layout

This schema is organized by module boundary instead of mixed domain buckets.

- `01-identity.sql`: tenant, account, role, permission, patient identity, consent, encounter, and identity views/indexes.
- `02-clinical.sql`: indicator dictionary, lab results, history entries, file assets, genetics, and imaging.
- `03-diagnosis-report.sql`: model registry, diagnosis sessions/results/recommendations, doctor feedback, reports, and report versions.
- `04-careplan-screening-payment.sql`: care plans, follow-up, patient reported data, alerts, questionnaires, ToC transfer, VIP plans/orders/subscriptions.
- `05-notify-integration-audit.sql`: notifications, integration jobs, audit logs, sensitive-access logs, and model invocation logs.
- `99-comments.sql`: PostgreSQL COMMENT metadata for tables, columns, views, and indexes; load last.

Design notes:

- Views stay with identity because they expose authorization state.
- Indexes are colocated with the tables they serve so schema ownership is visible in one file.
- Tenant-scoped support tables now keep tenant-aware foreign keys and tenant-leading indexes where cross-tenant leakage risk was highest.

CRUD optimization notes:

- Keep repetitive cross-table read models (especially user/role/permission aggregation) in versioned SQL views, then let MyBatis mappers query the view with tenant filters.
- For list APIs that currently do `COUNT(*)` plus paged `SELECT`, prefer a window-function variant (for example `COUNT(*) OVER()`) when profile testing proves it reduces duplicated scans.
- Use explicit, auditable triggers only for mechanical invariants (such as consistently updating `updated_at`); keep authorization and privacy policies in application services for better observability and rollback control.
