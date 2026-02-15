package xenosoft.imldintelligence.module.license.internal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "imld.licensing")
public class LicenseProperties {
    private String model = "saas-subscription";
    private PrivateEdition privateEdition = new PrivateEdition();

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public PrivateEdition getPrivateEdition() {
        return privateEdition;
    }

    public void setPrivateEdition(PrivateEdition privateEdition) {
        this.privateEdition = privateEdition;
    }

    public static class PrivateEdition {
        private String businessModel = "buyout";
        private boolean startupValidationEnabled = true;
        private boolean activationRequired = true;
        private String activationCode = "";
        private String licenseFilePath = "";
        private String publicKeyFilePath = "";
        private boolean machineBindingEnabled = true;

        public String getBusinessModel() {
            return businessModel;
        }

        public void setBusinessModel(String businessModel) {
            this.businessModel = businessModel;
        }

        public boolean isStartupValidationEnabled() {
            return startupValidationEnabled;
        }

        public void setStartupValidationEnabled(boolean startupValidationEnabled) {
            this.startupValidationEnabled = startupValidationEnabled;
        }

        public boolean isActivationRequired() {
            return activationRequired;
        }

        public void setActivationRequired(boolean activationRequired) {
            this.activationRequired = activationRequired;
        }

        public String getActivationCode() {
            return activationCode;
        }

        public void setActivationCode(String activationCode) {
            this.activationCode = activationCode;
        }

        public String getLicenseFilePath() {
            return licenseFilePath;
        }

        public void setLicenseFilePath(String licenseFilePath) {
            this.licenseFilePath = licenseFilePath;
        }

        public String getPublicKeyFilePath() {
            return publicKeyFilePath;
        }

        public void setPublicKeyFilePath(String publicKeyFilePath) {
            this.publicKeyFilePath = publicKeyFilePath;
        }

        public boolean isMachineBindingEnabled() {
            return machineBindingEnabled;
        }

        public void setMachineBindingEnabled(boolean machineBindingEnabled) {
            this.machineBindingEnabled = machineBindingEnabled;
        }
    }
}
