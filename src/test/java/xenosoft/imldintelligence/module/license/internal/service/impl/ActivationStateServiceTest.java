package xenosoft.imldintelligence.module.license.internal.service.impl;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import xenosoft.imldintelligence.module.license.internal.config.LicenseProperties;
import xenosoft.imldintelligence.module.license.internal.model.ActivationState;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivationStateServiceTest {

    @Test
    void shouldStoreAndReadActivationStateHash() throws Exception {
        LicenseProperties properties = new LicenseProperties();
        Path statePath = Files.createTempDirectory("imld-activation-state").resolve("activation-state.json");
        properties.getPrivateEdition().setActivationStateFilePath(statePath.toString());

        ActivationStateService service = new ActivationStateService(
                properties,
                new CryptoServiceImpl(),
                JsonMapper.builder().findAndAddModules().build()
        );

        service.storeActivationCode("ACT-12345", "LIC-123", "MACHINE-HASH", "test");

        Optional<ActivationState> stateOptional = service.readState();
        assertTrue(stateOptional.isPresent());
        ActivationState state = stateOptional.orElseThrow();
        assertEquals("LIC-123", state.getLicenseId());
        assertEquals("MACHINE-HASH", state.getMachineFingerprintHash());
        assertTrue(state.getActivatedAt() != null);

        Optional<String> hash = service.loadPersistedActivationCodeHash();
        assertTrue(hash.isPresent());
        assertEquals(new CryptoServiceImpl().sha256Hex("ACT-12345"), hash.orElseThrow());
    }

    @Test
    void shouldReturnEmptyWhenStateFileMissing() {
        LicenseProperties properties = new LicenseProperties();
        properties.getPrivateEdition().setActivationStateFilePath("build-codex/non-existent/state.json");

        ActivationStateService service = new ActivationStateService(
                properties,
                new CryptoServiceImpl(),
                JsonMapper.builder().findAndAddModules().build()
        );

        assertTrue(service.readState().isEmpty());
        assertFalse(service.loadPersistedActivationCodeHash().isPresent());
    }
}
