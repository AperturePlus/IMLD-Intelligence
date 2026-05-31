package xenosoft.imldintelligence.module.identity.internal.service;

import xenosoft.imldintelligence.module.identity.api.dto.IdentityApiDtos;

public interface AccountSettingsService {
    IdentityApiDtos.Response.AccountProfileResponse getCurrentProfile(Long tenantId);

    IdentityApiDtos.Response.AccountProfileResponse updateCurrentProfile(
            Long tenantId,
            IdentityApiDtos.Request.UpdateAccountProfileCommand request
    );

    void changeCurrentPassword(
            Long tenantId,
            IdentityApiDtos.Request.ChangePasswordCommand request,
            String authorizationHeader
    );
}
