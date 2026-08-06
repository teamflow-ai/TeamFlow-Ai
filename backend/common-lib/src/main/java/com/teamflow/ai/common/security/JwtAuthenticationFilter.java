package com.teamflow.ai.common.security;

import com.teamflow.ai.common.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Establishes the security context from a {@code Bearer} access token.
 *
 * <p>Shared by all three services so that token interpretation cannot drift between
 * them. The filter is intentionally permissive on failure: it clears the context
 * and delegates onward rather than writing a response itself, letting the
 * configured {@code AuthenticationEntryPoint} produce the standard error envelope.
 *
 * <p>Both the role and each permission are published as authorities. The role is
 * prefixed with {@code ROLE_} so {@code hasRole('ADMIN')} works, while permissions
 * are registered verbatim for {@code hasAuthority('CREATE_PROJECT')}.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);
        if (token == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.parseClaims(token);

            // A refresh token must never be accepted as a credential for normal API access.
            if (!jwtService.isAccessToken(claims)) {
                log.warn("Refresh token presented as access credential on {}", request.getRequestURI());
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            AuthenticatedUser principal = new AuthenticatedUser(
                    jwtService.extractUserId(claims),
                    jwtService.extractEmail(claims),
                    jwtService.extractRole(claims),
                    jwtService.extractPermissions(claims),
                    jwtService.extractEmployeeId(claims));

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (principal.role() != null) {
                authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + principal.role()));
            }
            principal.permissions().forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission)));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (InvalidTokenException ex) {
            // Expected for expired or forged tokens; the entry point renders the 401.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            return token.isEmpty() ? null : token;
        }
        return null;
    }
}
