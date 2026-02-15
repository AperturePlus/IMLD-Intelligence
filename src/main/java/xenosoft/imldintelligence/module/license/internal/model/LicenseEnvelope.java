package xenosoft.imldintelligence.module.license.internal.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class LicenseEnvelope {
    private JsonNode payload;
    private String signature;
}
