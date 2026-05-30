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

    @Test
    void shouldTreatDevAliasAsDevelopMode() {
        DeploymentProperties properties = new DeploymentProperties();
        properties.setMode("dev");

        assertEquals("dev", properties.normalizedMode());
        assertTrue(properties.isDevelopMode());
        assertTrue(properties.isSupportedMode());
    }

    @Test
    void shouldTreatDevelopModeAsSupported() {
        DeploymentProperties properties = new DeploymentProperties();
        properties.setMode("develop");

        assertEquals("develop", properties.normalizedMode());
        assertTrue(properties.isDevelopMode());
        assertTrue(properties.isSupportedMode());
    }

    @Test
    void shouldExposeSaasAndPrivatePredicates() {
        DeploymentProperties properties = new DeploymentProperties();

        properties.setMode("saas");
        assertTrue(properties.isSaasMode());
        assertFalse(properties.isPrivateMode());

        properties.setMode("private");
        assertTrue(properties.isPrivateMode());
        assertFalse(properties.isSaasMode());
    }
}
