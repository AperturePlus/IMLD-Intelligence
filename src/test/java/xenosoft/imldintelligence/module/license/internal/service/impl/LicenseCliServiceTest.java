package xenosoft.imldintelligence.module.license.internal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LicenseCliServiceTest {

    @Test
    void shouldImportVerifiedLicenseFile() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        Path publicKeyPath = writePublicKeyPem(keyPair.getPublic());

        LicenseInfo licenseInfo = new LicenseInfo();
        licenseInfo.setLicenseId("LIC-CLI-001");
        licenseInfo.setHospitalId("HOSP-CLI-A");
        licenseInfo.setDeploymentMode("private");
        licenseInfo.setSupportEndDate(LocalDate.of(2026, 12, 31));

        Path sourceLicensePath = writeSignedEnvelope(licenseInfo, keyPair.getPrivate());
        Path targetLicensePath = Files.createTempDirectory("imld-license-target").resolve("license.json");

        LicenseCliService service = new LicenseCliService(new CryptoServiceImpl(), JsonMapper.builder().findAndAddModules().build());
        LicenseInfo verified = service.readAndVerifyLicense(sourceLicensePath, publicKeyPath);
        Path imported = service.importLicenseFile(sourceLicensePath, targetLicensePath);

        assertEquals("LIC-CLI-001", verified.getLicenseId());
        assertTrue(Files.exists(imported));
    }

    @Test
    void shouldReturnDeniedDecisionWhenUpgradeOutsideSupport() {
        LicenseInfo licenseInfo = new LicenseInfo();
        licenseInfo.setSupportEndDate(LocalDate.of(2026, 6, 30));

        ReleaseManifest releaseManifest = new ReleaseManifest();
        releaseManifest.setReleaseDate(LocalDate.of(2026, 9, 1));
        releaseManifest.setSecurityPatch(false);

        LicenseCliService service = new LicenseCliService(new CryptoServiceImpl(), JsonMapper.builder().findAndAddModules().build());
        LicenseCliService.UpgradeDecision decision = service.evaluateUpgrade(licenseInfo, releaseManifest, true, true);

        assertFalse(decision.allowed());
        assertEquals("outside-support-window", decision.reason());
    }

    @Test
    void shouldReturnAllowedDecisionForSecurityPatchWhenPolicyEnabled() {
        LicenseInfo licenseInfo = new LicenseInfo();
        licenseInfo.setSupportEndDate(LocalDate.of(2026, 6, 30));

        ReleaseManifest releaseManifest = new ReleaseManifest();
        releaseManifest.setReleaseDate(LocalDate.of(2026, 9, 1));
        releaseManifest.setSecurityPatch(true);

        LicenseCliService service = new LicenseCliService(new CryptoServiceImpl(), JsonMapper.builder().findAndAddModules().build());
        LicenseCliService.UpgradeDecision decision = service.evaluateUpgrade(licenseInfo, releaseManifest, true, true);

        assertTrue(decision.allowed());
        assertEquals("security-patch-allowed-after-expiry", decision.reason());
    }

    @Test
    void shouldReadAndVerifyManifest() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        Path publicKeyPath = writePublicKeyPem(keyPair.getPublic());

        ReleaseManifest releaseManifest = new ReleaseManifest();
        releaseManifest.setVersion("2.0.0");
        releaseManifest.setReleaseDate(LocalDate.of(2026, 11, 1));
        releaseManifest.setChannel("lts");

        Path manifestPath = writeSignedEnvelope(releaseManifest, keyPair.getPrivate());
        LicenseCliService service = new LicenseCliService(new CryptoServiceImpl(), JsonMapper.builder().findAndAddModules().build());
        ReleaseManifest verifiedManifest = service.readAndVerifyManifest(manifestPath, publicKeyPath);

        assertNotNull(verifiedManifest);
        assertEquals("2.0.0", verifiedManifest.getVersion());
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
