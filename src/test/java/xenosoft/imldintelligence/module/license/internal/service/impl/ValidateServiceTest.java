package xenosoft.imldintelligence.module.license.internal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import xenosoft.imldintelligence.module.license.internal.config.DeploymentProperties;
import xenosoft.imldintelligence.module.license.internal.config.LicenseProperties;
import xenosoft.imldintelligence.module.license.internal.config.UpgradeProperties;
import xenosoft.imldintelligence.module.license.internal.model.LicenseInfo;
import xenosoft.imldintelligence.module.license.internal.model.ReleaseManifest;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidateServiceTest {

    @Test
    void shouldValidatePrivateLicenseAndUpgradeWithinSupport() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        Path publicKeyPath = writePublicKeyPem(keyPair.getPublic());
        String activationCode = "ACT-2026-LOCAL";

        LicenseInfo licenseInfo = new LicenseInfo();
        licenseInfo.setLicenseId("LIC-001");
        licenseInfo.setHospitalId("HOSP-A");
        licenseInfo.setDeploymentMode("private");
        licenseInfo.setSupportStartDate(LocalDate.of(2026, 1, 1));
        licenseInfo.setSupportEndDate(LocalDate.of(2027, 1, 31));
        licenseInfo.setActivationCodeHash(new CryptoServiceImpl().sha256Hex(activationCode));

        ReleaseManifest manifest = new ReleaseManifest();
        manifest.setVersion("1.3.0");
        manifest.setReleaseDate(LocalDate.of(2026, 8, 1));
        manifest.setSecurityPatch(false);
        manifest.setChannel("lts");

        Path licensePath = writeSignedEnvelope(licenseInfo, keyPair.getPrivate());
        Path manifestPath = writeSignedEnvelope(manifest, keyPair.getPrivate());

        ValidateService validateService = new ValidateService(
                deploymentPrivate(),
                licenseProperties(activationCode, licensePath, publicKeyPath, false),
                upgradeProperties(true, manifestPath, false),
                activationStateService(licenseProperties(activationCode, licensePath, publicKeyPath, false)),
                new CryptoServiceImpl(),
                JsonMapper.builder().findAndAddModules().build()
        );

        assertDoesNotThrow(validateService::validateStartupOrThrow);
    }

    @Test
    void shouldRejectUpgradeOutsideSupportWindow() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        Path publicKeyPath = writePublicKeyPem(keyPair.getPublic());
        String activationCode = "ACT-2026-LOCAL";

        LicenseInfo licenseInfo = new LicenseInfo();
        licenseInfo.setLicenseId("LIC-002");
        licenseInfo.setHospitalId("HOSP-B");
        licenseInfo.setDeploymentMode("private");
        licenseInfo.setSupportStartDate(LocalDate.of(2026, 1, 1));
        licenseInfo.setSupportEndDate(LocalDate.of(2026, 6, 30));
        licenseInfo.setActivationCodeHash(new CryptoServiceImpl().sha256Hex(activationCode));

        ReleaseManifest manifest = new ReleaseManifest();
        manifest.setVersion("1.8.0");
        manifest.setReleaseDate(LocalDate.of(2026, 9, 1));
        manifest.setSecurityPatch(false);
        manifest.setChannel("lts");

        Path licensePath = writeSignedEnvelope(licenseInfo, keyPair.getPrivate());
        Path manifestPath = writeSignedEnvelope(manifest, keyPair.getPrivate());

        ValidateService validateService = new ValidateService(
                deploymentPrivate(),
                licenseProperties(activationCode, licensePath, publicKeyPath, false),
                upgradeProperties(true, manifestPath, false),
                activationStateService(licenseProperties(activationCode, licensePath, publicKeyPath, false)),
                new CryptoServiceImpl(),
                JsonMapper.builder().findAndAddModules().build()
        );

        assertThrows(IllegalStateException.class, validateService::validateStartupOrThrow);
    }

    @Test
    void shouldAllowSecurityPatchOutsideSupportWhenPolicyEnabled() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        Path publicKeyPath = writePublicKeyPem(keyPair.getPublic());
        String activationCode = "ACT-2026-LOCAL";

        LicenseInfo licenseInfo = new LicenseInfo();
        licenseInfo.setLicenseId("LIC-003");
        licenseInfo.setHospitalId("HOSP-C");
        licenseInfo.setDeploymentMode("private");
        licenseInfo.setSupportStartDate(LocalDate.of(2026, 1, 1));
        licenseInfo.setSupportEndDate(LocalDate.of(2026, 6, 30));
        licenseInfo.setActivationCodeHash(new CryptoServiceImpl().sha256Hex(activationCode));

        ReleaseManifest manifest = new ReleaseManifest();
        manifest.setVersion("1.8.1");
        manifest.setReleaseDate(LocalDate.of(2026, 9, 1));
        manifest.setSecurityPatch(true);
        manifest.setChannel("lts");

        Path licensePath = writeSignedEnvelope(licenseInfo, keyPair.getPrivate());
        Path manifestPath = writeSignedEnvelope(manifest, keyPair.getPrivate());

        ValidateService validateService = new ValidateService(
                deploymentPrivate(),
                licenseProperties(activationCode, licensePath, publicKeyPath, false),
                upgradeProperties(true, manifestPath, true),
                activationStateService(licenseProperties(activationCode, licensePath, publicKeyPath, false)),
                new CryptoServiceImpl(),
                JsonMapper.builder().findAndAddModules().build()
        );

        assertDoesNotThrow(validateService::validateStartupOrThrow);
    }

    @Test
    void shouldTreatHybridAsPrivateForDeploymentCompatibility() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        Path publicKeyPath = writePublicKeyPem(keyPair.getPublic());
        String activationCode = "ACT-2026-LOCAL";

        LicenseInfo licenseInfo = new LicenseInfo();
        licenseInfo.setLicenseId("LIC-004");
        licenseInfo.setHospitalId("HOSP-D");
        licenseInfo.setDeploymentMode("hybrid");
        licenseInfo.setSupportStartDate(LocalDate.of(2026, 1, 1));
        licenseInfo.setSupportEndDate(LocalDate.of(2027, 1, 31));
        licenseInfo.setActivationCodeHash(new CryptoServiceImpl().sha256Hex(activationCode));

        Path licensePath = writeSignedEnvelope(licenseInfo, keyPair.getPrivate());
        Path manifestPath = Files.createTempFile("imld-manifest-", ".json");

        ValidateService validateService = new ValidateService(
                deploymentPrivate(),
                licenseProperties(activationCode, licensePath, publicKeyPath, false),
                upgradeProperties(false, manifestPath, false),
                activationStateService(licenseProperties(activationCode, licensePath, publicKeyPath, false)),
                new CryptoServiceImpl(),
                JsonMapper.builder().findAndAddModules().build()
        );

        assertDoesNotThrow(validateService::validateStartupOrThrow);
    }

    @Test
    void shouldValidateUsingPersistedActivationHashWhenRuntimeActivationCodeIsMissing() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        Path publicKeyPath = writePublicKeyPem(keyPair.getPublic());
        String activationCode = "ACT-STATE-ONLY-2026";

        LicenseInfo licenseInfo = new LicenseInfo();
        licenseInfo.setLicenseId("LIC-005");
        licenseInfo.setHospitalId("HOSP-E");
        licenseInfo.setDeploymentMode("private");
        licenseInfo.setSupportStartDate(LocalDate.of(2026, 1, 1));
        licenseInfo.setSupportEndDate(LocalDate.of(2027, 1, 31));
        licenseInfo.setActivationCodeHash(new CryptoServiceImpl().sha256Hex(activationCode));

        Path licensePath = writeSignedEnvelope(licenseInfo, keyPair.getPrivate());
        Path manifestPath = Files.createTempFile("imld-manifest-", ".json");

        LicenseProperties properties = licenseProperties("", licensePath, publicKeyPath, false);
        Path activationStatePath = Files.createTempDirectory("imld-activation-state").resolve("activation-state.json");
        properties.getPrivateEdition().setActivationStateFilePath(activationStatePath.toString());

        ActivationStateService activationStateService = activationStateService(properties);
        activationStateService.storeActivationCode(
                activationCode,
                licenseInfo.getLicenseId(),
                "TEST-MACHINE",
                "test-case"
        );

        ValidateService validateService = new ValidateService(
                deploymentPrivate(),
                properties,
                upgradeProperties(false, manifestPath, false),
                activationStateService,
                new CryptoServiceImpl(),
                JsonMapper.builder().findAndAddModules().build()
        );

        assertDoesNotThrow(validateService::validateStartupOrThrow);
    }

    private DeploymentProperties deploymentPrivate() {
        DeploymentProperties properties = new DeploymentProperties();
        properties.setMode("private");
        properties.setTenantMode("single-tenant");
        return properties;
    }

    private LicenseProperties licenseProperties(String activationCode, Path licensePath, Path publicKeyPath, boolean machineBindingEnabled) {
        LicenseProperties properties = new LicenseProperties();
        properties.setModel("private-buyout");
        LicenseProperties.PrivateEdition privateEdition = properties.getPrivateEdition();
        privateEdition.setStartupValidationEnabled(true);
        privateEdition.setActivationRequired(true);
        privateEdition.setActivationCode(activationCode);
        privateEdition.setLicenseFilePath(licensePath.toString());
        privateEdition.setPublicKeyFilePath(publicKeyPath.toString());
        privateEdition.setMachineBindingEnabled(machineBindingEnabled);
        return properties;
    }

    private UpgradeProperties upgradeProperties(boolean checkEnabled, Path manifestPath, boolean allowSecurityPatchAfterExpiry) {
        UpgradeProperties properties = new UpgradeProperties();
        properties.setCheckEnabled(checkEnabled);
        properties.setManifestFilePath(manifestPath.toString());
        properties.getEntitlement().setEnforceSupportWindow(true);
        properties.getEntitlement().setAllowSecurityPatchAfterExpiry(allowSecurityPatchAfterExpiry);
        return properties;
    }

    private ActivationStateService activationStateService(LicenseProperties properties) {
        return new ActivationStateService(
                properties,
                new CryptoServiceImpl(),
                JsonMapper.builder().findAndAddModules().build()
        );
    }

    private KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private Path writePublicKeyPem(java.security.PublicKey publicKey) throws Exception {
        String encoded = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8)).encodeToString(publicKey.getEncoded());
        String pem = "-----BEGIN PUBLIC KEY-----\n" + encoded + "\n-----END PUBLIC KEY-----\n";
        Path path = Files.createTempFile("imld-public-key-", ".pem");
        Files.writeString(path, pem);
        return path;
    }

    private Path writeSignedEnvelope(Object payloadObject, PrivateKey privateKey) throws Exception {
        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        byte[] payloadBytes = mapper.writeValueAsBytes(payloadObject);
        String payloadCanonical = mapper.writeValueAsString(mapper.readTree(payloadBytes));

        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(payloadCanonical.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(signer.sign());

        Map<String, Object> envelope = Map.of(
                "payload", mapper.readTree(payloadBytes),
                "signature", signature
        );

        Path path = Files.createTempFile("imld-signed-", ".json");
        Files.writeString(path, mapper.writeValueAsString(envelope));
        return path;
    }
}
