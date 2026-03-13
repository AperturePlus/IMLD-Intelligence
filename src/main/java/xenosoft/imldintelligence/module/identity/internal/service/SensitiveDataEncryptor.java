package xenosoft.imldintelligence.module.identity.internal.service;

public interface SensitiveDataEncryptor {

    String encrypt(String plaintext);

    String decrypt(String ciphertext);

    String mask(String plaintext, MaskType type);

    enum MaskType {
        MOBILE,
        ID_CARD,
        GENERAL
    }
}
