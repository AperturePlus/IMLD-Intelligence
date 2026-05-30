package xenosoft.imldintelligence.module.identity.internal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "imld.toc.wechat.miniapp")
public class WechatMiniappProperties {
    /**
     * WeChat miniapp appid.
     */
    private String appid = "";

    /**
     * WeChat miniapp secret.
     */
    private String secret = "";

    /**
     * code2session endpoint.
     */
    private String code2SessionEndpoint = "https://api.weixin.qq.com/sns/jscode2session";

    private Duration timeout = Duration.ofSeconds(3);
}

