package xenosoft.imldintelligence.module.license.internal.model;

import lombok.Getter;

import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Getter
public class Fingerprint {
    private final String cpuId;
    private final String macAddr;
    private final String hdId;
    private final String osInfo;

    public Fingerprint(){
        this.cpuId = getProcessorInfo();
        this.macAddr = getMacAddress();
        this.hdId = getComputerName();
        this.osInfo = getOsInfo();

    }

    private String getMacAddress() {
        try {
            StringBuilder sb = new StringBuilder();
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

            // 排序后取第一个，确保一致性
            if (!macAddresses.isEmpty()) {
                Collections.sort(macAddresses);
                return macAddresses.getFirst();
            }

            return "UNKNOWN_MAC";

        } catch (Exception e) {
            return "UNKNOWN_MAC";
        }
    }

    private String getOsInfo() {
        return System.getProperty("os.name") +
                System.getProperty("os.version") +
                System.getProperty("os.arch");
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
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();

        } catch (Exception e) {
            throw new RuntimeException("SHA-256 Hashing Failed", e);
        }
    }

    public String getFingerprintHash() {
        String combined = cpuId + macAddr + hdId + osInfo;
        return hashSHA256(combined);
    }
}
