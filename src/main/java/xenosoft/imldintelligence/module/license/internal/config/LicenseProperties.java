package xenosoft.imldintelligence.module.license.internal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "imld.licensing")
public class LicenseProperties {
    private String model = "saas-subscription";
    private PrivateEdition privateEdition = new PrivateEdition();

    @Setter
    @Getter
    public static class PrivateEdition {
        private String businessModel = "buyout";
        private boolean startupValidationEnabled = true;
        private boolean activationRequired = true;
        private String activationCode = "";
        private String licenseFilePath = "";
        private String publicKeyFilePath = "";
        private boolean machineBindingEnabled = true;

    }
}
