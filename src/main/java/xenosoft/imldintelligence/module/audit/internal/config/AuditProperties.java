package xenosoft.imldintelligence.module.audit.internal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "imld.audit")
public class AuditProperties {
    private boolean enabled = true;
    private boolean queryApiEnabled = true;
    private boolean failClosed = true;
    private List<String> queryRoleAllowlist = new ArrayList<>(Arrays.asList(
            "SYSTEM_ADMIN",
            "COMPLIANCE_AUDITOR"
    ));
    private int defaultPageSize = 20;
    private int maxPageSize = 200;
    private int maxPayloadBytes = 8192;
    private boolean maskingEnabled = true;
    private List<String> maskedFields = new ArrayList<>(Arrays.asList(
            "idNo",
            "id_card",
            "mobile",
            "phone",
            "email",
            "address",
            "password",
            "token",
            "authorization",
            "patientName",
            "guardianName"
    ));
}
