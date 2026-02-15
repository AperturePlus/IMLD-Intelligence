package xenosoft.imldintelligence.module.license.internal.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReleaseManifest {
    private String version;
    private LocalDate releaseDate;
    private boolean securityPatch;
    private String channel;
}
