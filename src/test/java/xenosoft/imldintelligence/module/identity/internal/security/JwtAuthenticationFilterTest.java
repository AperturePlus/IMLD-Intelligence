package xenosoft.imldintelligence.module.identity.internal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.util.JwtUtil;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {
    private JwtAuthenticationFilter filter;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        IdentitySecurityProperties properties = new IdentitySecurityProperties();
        properties.getJwt().setIssuer("imld-test");
        properties.getJwt().setSecret("01234567890123456789012345678901");
        properties.getJwt().setAccessTokenTtl(Duration.ofMinutes(15));
        properties.getJwt().setRefreshTokenTtl(Duration.ofDays(7));
        properties.getJwt().setClockSkew(Duration.ZERO);

        jwtUtil = new JwtUtil(properties, Clock.fixed(Instant.parse("2026-03-07T00:00:00Z"), ZoneOffset.UTC));
        filter = new JwtAuthenticationFilter(jwtUtil, new JwtAuthenticationEntryPoint(new ObjectMapper()));
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldPopulateSecurityContextForValidBearerToken() throws Exception {
        String token = jwtUtil.generateAccessToken(new UserSubject(88L, 66L, "DOCTOR", "Neurology", Set.of("SYSTEM_ADMIN")));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain(new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                assertThat(authentication).isNotNull();
                assertThat(authentication.getPrincipal()).isInstanceOf(UserSubject.class);
                UserSubject principal = (UserSubject) authentication.getPrincipal();
                assertThat(principal.userId()).isEqualTo(88L);
                assertThat(principal.tenantId()).isEqualTo(66L);
                assertThat(principal.userType()).isEqualTo("DOCTOR");
                assertThat(authentication.getAuthorities())
                        .extracting(authority -> authority.getAuthority())
                        .containsExactlyInAnyOrder("SYSTEM_ADMIN", "ROLE_SYSTEM_ADMIN");
            }
        });

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldIgnoreRequestWithoutBearerToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain(new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) {
                assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            }
        });

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldRejectInvalidBearerToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"error\":\"unauthorized\"");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
