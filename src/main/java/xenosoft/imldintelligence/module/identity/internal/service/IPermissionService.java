package xenosoft.imldintelligence.module.identity.internal.service;

import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;

import java.util.Map;
import java.util.Set;

public interface IPermissionService {

    boolean isAllowed(Long tenantId, Long userId, String res, String action,
                             Map<String, Object> resAttr);

    Set<String> getEffectiveRoleCodes(Long tenantId, Long userId);

    UserSubject loadSubject(Long tenantId, Long userId);

}
