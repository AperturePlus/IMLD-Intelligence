package xenosoft.imldintelligence.module.license.internal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import xenosoft.imldintelligence.module.license.api.dto.LicenseApiDtos;
import xenosoft.imldintelligence.module.license.internal.config.DeploymentProperties;
import xenosoft.imldintelligence.module.license.internal.config.LicenseProperties;
import xenosoft.imldintelligence.module.license.internal.config.UpgradeProperties;
import xenosoft.imldintelligence.module.license.internal.model.Fingerprint;
import xenosoft.imldintelligence.module.license.internal.model.LicenseInfo;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LicenseManagementServiceTest {

    @Test
    void shouldActivateAndPersistHash() throws Exception {
        String activationCode = "ACT-API-001";
        KeyPair keyPair = generateRsaKeyPair();
        Path publicKeyPath = writePublicKeyPem(keyPair.getPublic());

        LicenseInfo licenseInfo = new LicenseInfo();
        licenseInfo.setLicenseId("LIC-API-001");
        licenseInfo.setHospitalId("HOSP-API-1");
        licenseInfo.setDeploymentMode("private");
        licenseInfo.setSupportStartDate(LocalDate.of(2026, 1, 1));
        licenseInfo.setSupportEndDate(LocalDate.of(2026, 12, 31));
        licenseInfo.setActivationCodeHash(new CryptoServiceImpl().sha256Hex(activationCode));

        Path licensePath = writeSignedEnvelope(licenseInfo, keyPair.getPrivate());
        Path activationStatePath = Files.createTempDirectory("imld-activation-state").resolve("activation-state.json");

        LicenseManagementService service = newService(licensePath, publicKeyPath, activationStatePath, true);
        LicenseApiDtos.Request.ActivateLicenseRequest request = new LicenseApiDtos.Request.ActivateLicenseRequest(
                activationCode,
                new Fingerprint().getFingerprintHash()
        );

        LicenseApiDtos.Response.LicenseValidationResponse response = service.activateLicense(request);

        assertTrue(response.valid());
        assertEquals("activation-success", response.reason());

        ActivationStateService activationStateService = new ActivationStateService(
                licenseProperties(licensePath, publicKeyPath, activationStatePath, true),
                new CryptoServiceImpl(),
                JsonMapper.builder().findAndAddModules().build()
        );
        assertTrue(activationStateService.loadPersistedActivationCodeHash().isPresent());
    }

    @Test
    void shouldReturnInvalidWhenActivationCodeMismatch() throws Exception {
        String activationCode = "ACT-API-001";
        KeyPair keyPair = generateRsaKeyPair();
        Path publicKeyPath = writePublicKeyPem(keyPair.getPublic());

        LicenseInfo licenseInfo = new LicenseInfo();
        licenseInfo.setLicenseId("LIC-API-002");
        licenseInfo.setHospitalId("HOSP-API-2");
        licenseInfo.setDeploymentMode("private");
        licenseInfo.setSupportStartDate(LocalDate.of(2026, 1, 1));
        licenseInfo.setSupportEndDate(LocalDate.of(2026, 12, 31));
        licenseInfo.setActivationCodeHash(new CryptoServiceImpl().sha256Hex(activationCode));

        Path licensePath = writeSignedEnvelope(licenseInfo, keyPair.getPrivate());
        Path activationStatePath = Files.createTempDirectory("imld-activation-state").resolve("activation-state.json");

        LicenseManagementService service = newService(licensePath, publicKeyPath, activationStatePath, true);
        LicenseApiDtos.Request.ActivateLicenseRequest request = new LicenseApiDtos.Request.ActivateLicenseRequest(
                "WRONG-CODE",
                new Fingerprint().getFingerprintHash()
        );

        LicenseApiDtos.Response.LicenseValidationResponse response = service.activateLicense(request);
        assertFalse(response.valid());
    }

    private LicenseManagementService newService(Path licensePath,
                                                Path publicKeyPath,
                                                Path activationStatePath,
                                                boolean activationRequired) {
        DeploymentProperties deploymentProperties = new DeploymentProperties();
        deploymentProperties.setMode("private");

        LicenseProperties licenseProperties = licenseProperties(licensePath, publicKeyPath, activationStatePath, activationRequired);
        UpgradeProperties upgradeProperties = new UpgradeProperties();

        ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();
        CryptoServiceImpl cryptoService = new CryptoServiceImpl();
        LicenseCliService cliService = new LicenseCliService(cryptoService, mapper);
        ActivationStateService activationStateService = new ActivationStateService(licenseProperties, cryptoService, mapper);

        return new LicenseManagementService(
                deploymentProperties,
                licenseProperties,
                upgradeProperties,
                cliService,
                activationStateService,
                cryptoService,
                mapper
        );
    }

    private LicenseProperties licenseProperties(Path licensePath,
                                                Path publicKeyPath,
                                                Path activationStatePath,
                                                boolean activationRequired) {
        LicenseProperties properties = new LicenseProperties();
        LicenseProperties.PrivateEdition privateEdition = properties.getPrivateEdition();
        privateEdition.setActivationRequired(activationRequired);
        privateEdition.setMachineBindingEnabled(false);
        privateEdition.setLicenseFilePath(licensePath.toString());
        privateEdition.setPublicKeyFilePath(publicKeyPath.toString());
        privateEdition.setActivationStateFilePath(activationStatePath.toString());
        return properties;
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
