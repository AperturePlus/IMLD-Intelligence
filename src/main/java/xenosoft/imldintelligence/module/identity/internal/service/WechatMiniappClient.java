package xenosoft.imldintelligence.module.identity.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import xenosoft.imldintelligence.module.identity.internal.config.WechatMiniappProperties;

import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class WechatMiniappClient {

    private final WechatMiniappProperties properties;
    private final WebClient.Builder webClientBuilder;

    public WechatSession code2Session(String jsCode) {
        if (!hasText(properties.getAppid()) || !hasText(properties.getSecret())) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "WeChat miniapp is not configured");
        }
        String endpoint = properties.getCode2SessionEndpoint();
        if (!hasText(endpoint)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "WeChat code2session endpoint is not configured");
        }
        if (!hasText(jsCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jsCode must not be blank");
        }

        WebClient client = webClientBuilder.build();
        String url = UriComponentsBuilder.fromHttpUrl(endpoint.trim())
                .queryParam("appid", properties.getAppid().trim())
                .queryParam("secret", properties.getSecret().trim())
                .queryParam("js_code", jsCode.trim())
                .queryParam("grant_type", "authorization_code")
                .build(true)
                .toUriString();
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = client.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(properties.getTimeout())
                .block();

        String errcode = payload == null ? null : Objects.toString(payload.get("errcode"), null);
        if (errcode != null && !"0".equals(errcode)) {
            String errmsg = payload == null ? null : Objects.toString(payload.get("errmsg"), null);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "WeChat login failed: " + (errmsg == null ? errcode : errmsg));
        }

        String openid = payload == null ? null : Objects.toString(payload.get("openid"), null);
        String unionid = payload == null ? null : Objects.toString(payload.get("unionid"), null);
        if (!hasText(openid)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "WeChat login failed: missing openid");
        }
        return new WechatSession(openid.trim(), hasText(unionid) ? unionid.trim() : null);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public record WechatSession(String openid, String unionid) {
    }
}
