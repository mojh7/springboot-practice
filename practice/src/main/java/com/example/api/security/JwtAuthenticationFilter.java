package com.example.api.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtProvider jwtTokenProvider;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = jwtTokenProvider.resolveToken(request);

        try {
            if (token != null && !"".equals(token) && (jwtTokenProvider.validateToken(token))) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                context.setAuthentication(authentication);

                SecurityContextHolder.setContext(context);
            }
            filterChain.doFilter(request, response);
        } catch (AuthenticationException e) {
            logger.error(e.getMessage(), e);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        }
    }

}
