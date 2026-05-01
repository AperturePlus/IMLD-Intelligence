package xenosoft.imldintelligence.module.identity.api.toc;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.module.identity.api.toc.dto.TocAuthApiDtos;
import xenosoft.imldintelligence.module.identity.internal.service.TocAuthService;

@RestController
@RequiredArgsConstructor
public class TocAuthController implements TocAuthControllerContract {

    private final TocAuthService tocAuthService;

    @Override
    public ApiResponse<TocAuthApiDtos.Response.TocAuthSessionResponse> wechatLogin(
            TocAuthApiDtos.Request.WechatLoginRequest request) {
        return ApiResponse.success(tocAuthService.wechatLogin(request));
    }

    @Override
    public ApiResponse<TocAuthApiDtos.Response.PhoneCodeSendResponse> sendPhoneLoginCode(
            TocAuthApiDtos.Request.SendPhoneCodeRequest request) {
        return ApiResponse.success(tocAuthService.sendPhoneLoginCode(request));
    }

    @Override
    public ApiResponse<TocAuthApiDtos.Response.TocAuthSessionResponse> phoneLogin(
            TocAuthApiDtos.Request.PhoneLoginRequest request) {
        return ApiResponse.success(tocAuthService.phoneLogin(request));
    }

    @Override
    public ApiResponse<TocAuthApiDtos.Response.TocAuthSessionResponse> refresh(
            TocAuthApiDtos.Request.RefreshRequest request) {
        return ApiResponse.success(tocAuthService.refresh(request));
    }

    @Override
    public ApiResponse<Void> logout(TocAuthApiDtos.Request.LogoutRequest request) {
        tocAuthService.logout(request);
        return ApiResponse.success();
    }
}

