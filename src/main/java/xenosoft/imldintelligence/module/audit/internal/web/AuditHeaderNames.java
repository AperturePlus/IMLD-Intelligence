package xenosoft.imldintelligence.module.audit.internal.web;

public final class AuditHeaderNames {
    public static final String TENANT_ID = "X-Tenant-Id";
    public static final String USER_ID = "X-User-Id";
    public static final String USER_ROLE = "X-User-Role";
    public static final String TRACE_ID = "X-Trace-Id";
    public static final String APP_VERSION = "X-App-Version";
    public static final String DEVICE_INFO = "X-Device-Info";
    public static final String FORWARDED_FOR = "X-Forwarded-For";

    private AuditHeaderNames() {
    }
}
