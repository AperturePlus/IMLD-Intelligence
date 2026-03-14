package xenosoft.imldintelligence.module.license.internal.model;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import lombok.Getter;

/**
 * Represents a lightweight machine fingerprint used for offline license binding.
 *
 * <p>The fingerprint intentionally combines stable and broadly available runtime signals instead of
 * hardware-vendor specific identifiers so private deployments can validate licenses without extra
 * native dependencies.</p>
 */
@Getter
public class Fingerprint {
    private final String cpuId;
    private final String macAddr;
    private final String hdId;
    private final String osInfo;

    public Fingerprint() {
        this.cpuId = getProcessorInfo();
        this.macAddr = getMacAddress();
        this.hdId = getComputerName();
        this.osInfo = getOsInfo();
    }

    /**
     * Collects physical MAC addresses and selects the lexicographically smallest value for stability.
     */
    private String getMacAddress() {
        try {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            List<String> macAddresses = new ArrayList<>();
            while (networks.hasMoreElements()) {
                NetworkInterface network = networks.nextElement();
                byte[] mac = network.getHardwareAddress();

                if (mac != null && mac.length > 0 && !network.isLoopback() && !network.isVirtual()) {
                    StringBuilder macBuilder = new StringBuilder();
                    for (byte b : mac) {
                        macBuilder.append(String.format("%02X", b));
                    }
                    macAddresses.add(macBuilder.toString());
                }
            }

            if (!macAddresses.isEmpty()) {
                Collections.sort(macAddresses);
                return macAddresses.getFirst();
            }
            return "UNKNOWN_MAC";
        } catch (SocketException e) {
            return "UNKNOWN_MAC";
        }
    }

    private String getOsInfo() {
        return System.getProperty("os.name")
                + System.getProperty("os.version")
                + System.getProperty("os.arch");
    }

    private String getProcessorInfo() {
        return String.valueOf(Runtime.getRuntime().availableProcessors());
    }

    private String getComputerName() {
        try {
            String name = System.getenv("COMPUTERNAME");
            if (name == null) {
                name = System.getenv("HOSTNAME");
            }
            return name != null ? name : "UNKNOWN_HOST";
        } catch (Exception e) {
            return "UNKNOWN_HOST";
        }
    }

    private String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 Hashing Failed", e);
        }
    }

    /**
     * Returns the final machine binding digest used in license validation.
     */
    public String getFingerprintHash() {
        String combined = cpuId + macAddr + hdId + osInfo;
        return hashSHA256(combined);
    }
}
