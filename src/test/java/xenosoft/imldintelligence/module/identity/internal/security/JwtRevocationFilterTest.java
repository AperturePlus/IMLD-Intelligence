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
import org.springframework.security.core.context.SecurityContextHolder;
import xenosoft.imldintelligence.module.identity.internal.model.UserSubject;
import xenosoft.imldintelligence.module.identity.internal.service.TokenBlacklistService;
import xenosoft.imldintelligence.module.identity.internal.util.JwtUtil;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JwtRevocationFilterTest {
    private JwtUtil jwtUtil;
    private StubBlacklist blacklist;
    private JwtRevocationFilter filter;

    @BeforeEach
    void setUp() {
        IdentitySecurityProperties properties = new IdentitySecurityProperties();
        properties.getJwt().setIssuer("imld-test");
        properties.getJwt().setSecret("01234567890123456789012345678901");
        properties.getJwt().setAccessTokenTtl(Duration.ofMinutes(15));
        properties.getJwt().setRefreshTokenTtl(Duration.ofDays(7));
        properties.getJwt().setClockSkew(Duration.ZERO);

        jwtUtil = new JwtUtil(properties, Clock.fixed(Instant.parse("2026-03-07T00:00:00Z"), ZoneOffset.UTC));
        blacklist = new StubBlacklist();
        filter = new JwtRevocationFilter(jwtUtil, blacklist, new JwtAuthenticationEntryPoint(new ObjectMapper()));
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsBlacklistedAccessToken() throws Exception {
        String token = jwtUtil.generateAccessToken(new UserSubject(88L, 66L, "DOCTOR", "Neurology", Set.of("DOCTOR")));
        String jti = jwtUtil.extractJti(token);
        blacklist.blacklisted.add(jti);

        // Simulate JwtAuthenticationFilter having populated the context (filter runs after it).
        SecurityContextHolder.getContext().setAuthentication(
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
                        new UserSubject(88L, 66L, "DOCTOR", "Neurology", Set.of("DOCTOR")),
                        token,
                        java.util.List.of()));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"error\":\"unauthorized\"");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void proceedsWhenAccessTokenNotBlacklisted() throws Exception {
        String token = jwtUtil.generateAccessToken(new UserSubject(88L, 66L, "DOCTOR", "Neurology", Set.of("DOCTOR")));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain(new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) {
                resp.setStatus(200);
            }
        });

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void proceedsWhenAuthorizationHeaderAbsent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain(new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) {
                resp.setStatus(200);
            }
        });

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void proceedsWhenBearerTokenUnparseable() throws Exception {
        // extractJti returns null for an unparseable token -> filter must skip, not 401.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain(new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse resp) {
                resp.setStatus(200);
            }
        });

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    /** Minimal in-memory stub of TokenBlacklistService. */
    static class StubBlacklist implements TokenBlacklistService {
        final java.util.Set<String> blacklisted = new java.util.HashSet<>();

        @Override
        public void blacklist(String jti, Duration ttl) {
            blacklisted.add(jti);
        }

        @Override
        public boolean isBlacklisted(String jti) {
            return blacklisted.contains(jti);
        }
    }
}
