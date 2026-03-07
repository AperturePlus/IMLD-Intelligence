package xenosoft.imldintelligence.module.identity.internal.model;

import java.util.Set;

/**
 * 用户主体对象，表示当前授权判定所需的最小用户身份信息。
 */
public record UserSubject (    Long userId,
        Long tenantId,
        String userType, // DOCTOR, NURSE, PATIENT, ADMIN
        String deptName,
        Set<String> roleCodes
        ){
}
