package xenosoft.imldintelligence.module.license.internal.service;

import java.nio.file.Path;
import java.security.PublicKey;

public interface CryptoService {
    PublicKey loadRsaPublicKey(Path publicKeyPath);

    boolean verifySha256WithRsa(String payload, String signatureBase64, PublicKey publicKey);

    String sha256Hex(String input);
}
