package xenosoft.imldintelligence.module.identity.internal.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.RequiredArgsConstructor;
import xenosoft.imldintelligence.module.identity.internal.config.WechatMiniappProperties;

@Component
@RequiredArgsConstructor
public class WechatMiniappClient {

    private final WechatMiniappProperties properties;
    private final WebClient.Builder webClientBuilder;

    public WechatSession code2Session(String jsCode) {
        String appid = trimToNull(properties.getAppid());
        String secret = trimToNull(properties.getSecret());
        String endpoint = trimToNull(properties.getCode2SessionEndpoint());
        String code = trimToNull(jsCode);

        if (appid == null || secret == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "WeChat miniapp is not configured"
            );
        }
        if (endpoint == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "WeChat code2session endpoint is not configured"
            );
        }
        if (code == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "jsCode must not be blank"
            );
        }

        var uri = UriComponentsBuilder.fromUriString(endpoint)
                .queryParam("appid", appid)
                .queryParam("secret", secret)
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .encode()
                .build()
                .toUri();

        WebClient client = webClientBuilder.build();

        Code2SessionResponse payload = client.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Code2SessionResponse.class)
                .timeout(properties.getTimeout())
                .block();

        if (payload == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "WeChat login failed: empty response"
            );
        }

        if (payload.errcode() != null && payload.errcode() != 0) {
            String message = trimToNull(payload.errmsg());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "WeChat login failed: " + (message == null ? payload.errcode() : message)
            );
        }

        String openid = trimToNull(payload.openid());
        String unionid = trimToNull(payload.unionid());

        if (openid == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "WeChat login failed: missing openid"
            );
        }

        return new WechatSession(openid, unionid);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Code2SessionResponse(
            Integer errcode,
            String errmsg,
            String openid,
            String unionid
    ) {
    }

    public record WechatSession(String openid, String unionid) {
    }
}
