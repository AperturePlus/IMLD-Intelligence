-- Identity module DB-side optimization candidates (optional but recommended)
-- Purpose: push repetitive mechanical logic (updated_at maintenance + frequently reused read model)
-- into audited SQL objects without changing privacy/authorization boundaries.

-- 1) Unified updated_at trigger for identity tables that own updated_at.
CREATE OR REPLACE FUNCTION touch_updated_at()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    -- Only auto-set updated_at when the application did not explicitly change it.
    -- This preserves intentional timestamps from backfills or app-layer assignments.
    IF NEW.updated_at IS NOT DISTINCT FROM OLD.updated_at THEN
        NEW.updated_at = now();
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_tenant_touch_updated_at ON tenant;
CREATE TRIGGER trg_tenant_touch_updated_at
BEFORE UPDATE ON tenant
FOR EACH ROW
EXECUTE FUNCTION touch_updated_at();

DROP TRIGGER IF EXISTS trg_user_account_touch_updated_at ON user_account;
CREATE TRIGGER trg_user_account_touch_updated_at
BEFORE UPDATE ON user_account
FOR EACH ROW
EXECUTE FUNCTION touch_updated_at();

DROP TRIGGER IF EXISTS trg_toc_user_touch_updated_at ON toc_user;
CREATE TRIGGER trg_toc_user_touch_updated_at
BEFORE UPDATE ON toc_user
FOR EACH ROW
EXECUTE FUNCTION touch_updated_at();

DROP TRIGGER IF EXISTS trg_patient_touch_updated_at ON patient;
CREATE TRIGGER trg_patient_touch_updated_at
BEFORE UPDATE ON patient
FOR EACH ROW
EXECUTE FUNCTION touch_updated_at();

-- 2) Reusable active user view to reduce duplicated tenant/status predicates.
CREATE OR REPLACE VIEW v_identity_active_user AS
SELECT
    ua.id,
    ua.tenant_id,
    ua.user_no,
    ua.username,
    ua.display_name,
    ua.user_type,
    ua.dept_name,
    ua.email,
    ua.last_login_at,
    ua.updated_at
FROM user_account ua
WHERE ua.status = 'ACTIVE';

COMMENT ON VIEW v_identity_active_user IS
'Identity read model: active backend users only. Tenant filtering must still be applied by application SQL.';
