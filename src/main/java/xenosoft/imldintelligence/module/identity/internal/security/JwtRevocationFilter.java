package xenosoft.imldintelligence.module.identity.internal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;
import xenosoft.imldintelligence.module.identity.internal.service.TokenBlacklistService;
import xenosoft.imldintelligence.module.identity.internal.util.JwtUtil;

import java.io.IOException;

/**
 * Rejects access tokens whose jti has been blacklisted (logout / password change).
 *
 * <p>Runs immediately after {@link JwtAuthenticationFilter}; if that filter rejected the token this
 * filter never runs. If the token parsed cleanly, this filter extracts the jti and consults
 * {@link TokenBlacklistService}. A blacklisted token clears the security context and delegates to
 * the configured {@link AuthenticationEntryPoint} (401).</p>
 */
@RequiredArgsConstructor
public class JwtRevocationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank() || !authorization.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        String jti = jwtUtil.extractJti(token);
        if (jti != null && tokenBlacklistService.isBlacklisted(jti)) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response,
                    new BadCredentialsException("JWT access token has been revoked"));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
