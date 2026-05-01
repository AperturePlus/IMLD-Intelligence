package xenosoft.imldintelligence.module.identity.internal.service;

import xenosoft.imldintelligence.module.identity.api.toc.dto.TocAuthApiDtos;

public interface TocAuthService {
    TocAuthApiDtos.Response.TocAuthSessionResponse wechatLogin(TocAuthApiDtos.Request.WechatLoginRequest request);

    TocAuthApiDtos.Response.PhoneCodeSendResponse sendPhoneLoginCode(TocAuthApiDtos.Request.SendPhoneCodeRequest request);

    TocAuthApiDtos.Response.TocAuthSessionResponse phoneLogin(TocAuthApiDtos.Request.PhoneLoginRequest request);

    TocAuthApiDtos.Response.TocAuthSessionResponse refresh(TocAuthApiDtos.Request.RefreshRequest request);

    void logout(TocAuthApiDtos.Request.LogoutRequest request);
}

