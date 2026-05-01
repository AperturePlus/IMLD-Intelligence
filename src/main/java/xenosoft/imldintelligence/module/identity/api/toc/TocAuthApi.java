package xenosoft.imldintelligence.module.identity.api.toc;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import xenosoft.imldintelligence.common.dto.ApiResponse;
import xenosoft.imldintelligence.module.identity.api.toc.dto.TocAuthApiDtos;

/**
 * ToC (App) authentication APIs.
 *
 * <p>Separated from {@code IdentityApi} to avoid mixing hospital/staff login semantics.</p>
 */
@Validated
@RequestMapping("/api/v1/app/toc/auth")
public interface TocAuthApi {

    @PostMapping("/wechat/login")
    ApiResponse<TocAuthApiDtos.Response.TocAuthSessionResponse> wechatLogin(
            @Valid @RequestBody TocAuthApiDtos.Request.WechatLoginRequest request
    );

    @PostMapping("/phone/send-code")
    ApiResponse<TocAuthApiDtos.Response.PhoneCodeSendResponse> sendPhoneLoginCode(
            @Valid @RequestBody TocAuthApiDtos.Request.SendPhoneCodeRequest request
    );

    @PostMapping("/phone/login")
    ApiResponse<TocAuthApiDtos.Response.TocAuthSessionResponse> phoneLogin(
            @Valid @RequestBody TocAuthApiDtos.Request.PhoneLoginRequest request
    );

    @PostMapping("/refresh")
    ApiResponse<TocAuthApiDtos.Response.TocAuthSessionResponse> refresh(
            @Valid @RequestBody TocAuthApiDtos.Request.RefreshRequest request
    );

    @PostMapping("/logout")
    ApiResponse<Void> logout(
            @Valid @RequestBody TocAuthApiDtos.Request.LogoutRequest request
    );
}

