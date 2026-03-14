package xenosoft.imldintelligence.module.identity.internal.security;

import org.junit.jupiter.api.Test;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.util.JwtUtil;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = JwtSecurityIntegrationTest.TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisabledInAotMode
@TestPropertySource(properties = {
        "imld.security.enabled=true",
        "imld.security.public-paths[0]=/test/public",
        "imld.security.public-paths[1]=/error",
        "imld.security.jwt.issuer=imld-test",
        "imld.security.jwt.secret=01234567890123456789012345678901",
        "imld.security.jwt.access-token-ttl=15m",
        "imld.security.jwt.refresh-token-ttl=7d",
        "imld.security.jwt.clock-skew=0s"
})
class JwtSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void shouldAllowPublicEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/test/public"))
                .andExpect(status().isOk())
                .andExpect(content().string("public"));
    }

    @Test
    void shouldRejectProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/test/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("unauthorized"));
    }

    @Test
    void shouldAllowProtectedEndpointWithValidToken() throws Exception {
        String token = jwtUtil.generateAccessToken(new UserSubject(12L, 34L, "doctor", "ICU", Set.of("doctor")));

        mockMvc.perform(get("/test/protected")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("12:34"));
    }

    @Test
    void shouldApplyModuleSpecificAuthorizationRule() throws Exception {
        String token = jwtUtil.generateAccessToken(new UserSubject(12L, 34L, "doctor", "ICU", Set.of("doctor")));

        mockMvc.perform(get("/test/module-protected")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("module:12"));
    }

    @Test
    void shouldRejectWhenModuleSpecificAuthorizationRuleDoesNotMatch() throws Exception {
        String token = jwtUtil.generateAccessToken(new UserSubject(12L, 34L, "nurse", "Ward", Set.of("nurse")));

        mockMvc.perform(get("/test/module-protected")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("forbidden"));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            JdbcTemplateAutoConfiguration.class,
            MybatisPlusAutoConfiguration.class,
            RedisAutoConfiguration.class,
            RedisReactiveAutoConfiguration.class,
            RedisRepositoriesAutoConfiguration.class,
            KafkaAutoConfiguration.class
    })
    @Import({IdentitySecurityConfiguration.class, JwtUtil.class, IdentityRequestAuthorizationCustomizer.class, TestController.class})
    static class TestApplication {
        @Bean
        ModuleRequestAuthorizationCustomizer testModuleRequestAuthorizationCustomizer() {
            return requests -> requests.requestMatchers("/test/module-protected")
                    .hasAnyAuthority("DOCTOR", "ROLE_DOCTOR");
        }
    }

    @RestController
    static class TestController {
        @GetMapping("/test/public")
        String publicEndpoint() {
            return "public";
        }

        @GetMapping("/test/protected")
        String protectedEndpoint(@AuthenticationPrincipal UserSubject userSubject) {
            return userSubject.userId() + ":" + userSubject.tenantId();
        }

        @GetMapping("/test/module-protected")
        String moduleProtectedEndpoint(@AuthenticationPrincipal UserSubject userSubject) {
            return "module:" + userSubject.userId();
        }
    }
}
