package xenosoft.imldintelligence.module.identity.internal.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xenosoft.imldintelligence.module.identity.internal.service.SensitiveDataEncryptor;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AesSensitiveDataEncryptor implements SensitiveDataEncryptor {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final byte[] keyBytes;

    public AesSensitiveDataEncryptor(
            @Value("${imld.security.data-encryption-key:}") String dataEncryptionKey) {
        if (dataEncryptionKey == null || dataEncryptionKey.isBlank()) {
            this.keyBytes = null;
        } else {
            byte[] raw = dataEncryptionKey.getBytes(StandardCharsets.UTF_8);
            // AES only accepts 16 (AES-128), 24 (AES-192), or 32 (AES-256) byte keys.
            // Reject any other length rather than silently truncating, so a mis-sized
            // configured key fails loudly at startup instead of degrading to AES-128.
            if (raw.length != 16 && raw.length != 24 && raw.length != 32) {
                throw new IllegalStateException(
                        "imld.security.data-encryption-key must be exactly 16, 24, or 32 bytes, got "
                                + raw.length + " bytes");
            }
            this.keyBytes = raw;
        }
    }

    @Override
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }
        requireKey();
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(keyBytes, "AES"),
                    new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(IV_LENGTH + ciphertext.length);
            buffer.put(iv);
            buffer.put(ciphertext);

            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    @Override
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return ciphertext;
        }
        requireKey();
        try {
            byte[] decoded = Base64.getDecoder().decode(ciphertext);

            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(keyBytes, "AES"),
                    new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }

    @Override
    public String mask(String plaintext, MaskType type) {
        if (plaintext == null || plaintext.isEmpty()) {
            return null;
        }
        return switch (type) {
            case MOBILE -> maskMobile(plaintext);
            case ID_CARD -> maskIdCard(plaintext);
            case GENERAL -> maskGeneral(plaintext);
        };
    }

    private String maskMobile(String value) {
        // 138****1234
        if (value.length() <= 4) {
            return "*".repeat(value.length());
        }
        if (value.length() < 7) {
            return value.substring(0, 2) + "*".repeat(value.length() - 4) + value.substring(value.length() - 2);
        }
        return value.substring(0, 3) + "*".repeat(value.length() - 7) + value.substring(value.length() - 4);
    }

    private String maskIdCard(String value) {
        // 110***********1234
        if (value.length() <= 4) {
            return "*".repeat(value.length());
        }
        int prefix = Math.min(3, value.length() - 4);
        return value.substring(0, prefix) + "*".repeat(value.length() - prefix - 4) + value.substring(value.length() - 4);
    }

    private String maskGeneral(String value) {
        int length = value.length();
        if (length <= 4) {
            return "*".repeat(length);
        }
        int prefix = Math.min(3, Math.max(1, length / 4));
        int suffix = Math.min(2, Math.max(1, length / 5));
        int maskedLength = Math.max(1, length - prefix - suffix);
        return value.substring(0, prefix) + "*".repeat(maskedLength) + value.substring(length - suffix);
    }

    private void requireKey() {
        if (keyBytes == null) {
            throw new IllegalStateException(
                    "imld.security.data-encryption-key is not configured");
        }
    }
}
