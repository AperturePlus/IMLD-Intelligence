package xenosoft.imldintelligence.module.identity.internal.service.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import xenosoft.imldintelligence.module.identity.internal.service.SensitiveDataEncryptor;

class AesSensitiveDataEncryptorTest {

    private static final String VALID_KEY = "01234567890123456789012345678901"; // 32 bytes

    @Test
    void encryptAndDecryptRoundTrip() {
        AesSensitiveDataEncryptor encryptor = new AesSensitiveDataEncryptor(VALID_KEY);

        String plaintext = "13800138000";
        String encrypted = encryptor.encrypt(plaintext);

        assertThat(encrypted).isNotEqualTo(plaintext);
        assertThat(encrypted).isNotBlank();

        String decrypted = encryptor.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    void encryptAndDecryptIdCard() {
        AesSensitiveDataEncryptor encryptor = new AesSensitiveDataEncryptor(VALID_KEY);

        String idCard = "310101199001011234";
        String encrypted = encryptor.encrypt(idCard);
        String decrypted = encryptor.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(idCard);
    }

    @Test
    void encryptProducesDifferentCiphertextEachTime() {
        AesSensitiveDataEncryptor encryptor = new AesSensitiveDataEncryptor(VALID_KEY);

        String plaintext = "test-data";
        String encrypted1 = encryptor.encrypt(plaintext);
        String encrypted2 = encryptor.encrypt(plaintext);

        // Different IVs should produce different ciphertexts
        assertThat(encrypted1).isNotEqualTo(encrypted2);
        // But both should decrypt to the same value
        assertThat(encryptor.decrypt(encrypted1)).isEqualTo(plaintext);
        assertThat(encryptor.decrypt(encrypted2)).isEqualTo(plaintext);
    }

    @Test
    void encryptReturnsNullForNull() {
        AesSensitiveDataEncryptor encryptor = new AesSensitiveDataEncryptor(VALID_KEY);
        assertThat(encryptor.encrypt(null)).isNull();
        assertThat(encryptor.encrypt("")).isEmpty();
    }

    @Test
    void decryptReturnsNullForNull() {
        AesSensitiveDataEncryptor encryptor = new AesSensitiveDataEncryptor(VALID_KEY);
        assertThat(encryptor.decrypt(null)).isNull();
        assertThat(encryptor.decrypt("")).isEmpty();
    }

    @Test
    void maskMobileHidesMiddleDigits() {
        AesSensitiveDataEncryptor encryptor = new AesSensitiveDataEncryptor(VALID_KEY);

        String masked = encryptor.mask("13800138000", SensitiveDataEncryptor.MaskType.MOBILE);
        assertThat(masked).startsWith("138");
        assertThat(masked).endsWith("8000");
        assertThat(masked).contains("*");
        assertThat(masked).hasSize(11);
    }

    @Test
    void maskIdCardHidesMiddle() {
        AesSensitiveDataEncryptor encryptor = new AesSensitiveDataEncryptor(VALID_KEY);

        String masked = encryptor.mask("310101199001011234", SensitiveDataEncryptor.MaskType.ID_CARD);
        assertThat(masked).startsWith("310");
        assertThat(masked).endsWith("1234");
        assertThat(masked).contains("*");
    }

    @Test
    void maskGeneralHidesMiddle() {
        AesSensitiveDataEncryptor encryptor = new AesSensitiveDataEncryptor(VALID_KEY);

        String masked = encryptor.mask("EMPI-12345678", SensitiveDataEncryptor.MaskType.GENERAL);
        assertThat(masked).isNotNull();
        assertThat(masked).contains("*");
    }

    @Test
    void maskReturnsNullForNull() {
        AesSensitiveDataEncryptor encryptor = new AesSensitiveDataEncryptor(VALID_KEY);
        assertThat(encryptor.mask(null, SensitiveDataEncryptor.MaskType.MOBILE)).isNull();
        assertThat(encryptor.mask("", SensitiveDataEncryptor.MaskType.MOBILE)).isNull();
    }

    @Test
    void throwsWhenKeyNotConfigured() {
        AesSensitiveDataEncryptor encryptor = new AesSensitiveDataEncryptor("");

        assertThatThrownBy(() -> encryptor.encrypt("test"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not configured");
    }

    @Test
    void throwsWhenKeyTooShort() {
        assertThatThrownBy(() -> new AesSensitiveDataEncryptor("short"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 16 bytes");
    }

    @Test
    void worksWithSixteenByteKey() {
        AesSensitiveDataEncryptor encryptor = new AesSensitiveDataEncryptor("0123456789012345");

        String encrypted = encryptor.encrypt("hello");
        String decrypted = encryptor.decrypt(encrypted);
        assertThat(decrypted).isEqualTo("hello");
    }
}
