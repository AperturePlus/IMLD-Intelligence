package xenosoft.imldintelligence.module.license.internal.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeploymentPropertiesTest {

    @Test
    void shouldNormalizeHybridModeAsPrivate() {
        DeploymentProperties properties = new DeploymentProperties();
        properties.setMode("hybrid");

        assertEquals("private", properties.normalizedMode());
        assertTrue(properties.isPrivateMode());
        assertFalse(properties.isSaasMode());
    }

    @Test
    void shouldDefaultToSaasWhenModeMissingOrBlank() {
        DeploymentProperties properties = new DeploymentProperties();
        properties.setMode(null);
        assertEquals("saas", properties.normalizedMode());
        assertTrue(properties.isSaasMode());

        properties.setMode("   ");
        assertEquals("saas", properties.normalizedMode());
        assertTrue(properties.isSupportedMode());
    }
}
