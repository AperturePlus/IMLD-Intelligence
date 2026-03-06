package xenosoft.imldintelligence.module.identity.internal.model;

import java.util.Set;

public record UserSubject (    Long userId,
        Long tenantId,
        String userType, // DOCTOR, NURSE, PATIENT, ADMIN
        String deptName,
        Set<String> roleCodes
        ){
}
