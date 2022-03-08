package hexlet.code.config.security.filter;

import hexlet.code.config.security.component.TokenGenerator;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY;
import static hexlet.code.config.security.SecurityConfig.AUTHORITIES;

public class AuthorizationFilter extends OncePerRequestFilter {

    private final TokenGenerator tokenGenerator;
    private final RequestMatcher publicUrls;
    private static final String BEARER = "Bearer";

    public AuthorizationFilter(final TokenGenerator tokenGenerator,
                               final RequestMatcher publicUrls) {
        this.tokenGenerator = tokenGenerator;
        this.publicUrls = publicUrls;
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        return publicUrls.matches(request);
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        final String header = request.getHeader(AUTHORIZATION);
        final String token = header.replaceFirst("^" + BEARER, "");
        final String username = tokenGenerator.verify(token).get(SPRING_SECURITY_FORM_USERNAME_KEY).toString();
        final UsernamePasswordAuthenticationToken authToken = buildAuthToken(username);

        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken buildAuthToken(final String username) {
        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                AUTHORITIES);
    }
}
