package com.devmaster.goatfarm.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtDebugFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtDebugFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");
        
        logger.debug("ðŸ” JWT DEBUG: {} {}", method, requestURI);
        
        if (authHeader != null) {
            logger.debug("ðŸ” JWT DEBUG: Authorization header presente: {}...", authHeader.substring(0, Math.min(50, authHeader.length())));
        } else {
            logger.debug("ðŸ” JWT DEBUG: Authorization header ausente");
        }
        
                filterChain.doFilter(request, response);
        
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            logger.debug("ðŸ” JWT DEBUG: UsuÃ¡rio autenticado: {}", auth.getName());
            logger.debug("ðŸ” JWT DEBUG: Authorities: {}", auth.getAuthorities());
        } else {
            logger.debug("ðŸ” JWT DEBUG: Nenhuma autenticaÃ§Ã£o encontrada");
        }
        
        logger.debug("ðŸ” JWT DEBUG: Response status: {}", response.getStatus());
        logger.debug("ðŸ” JWT DEBUG: ===========================================");
    }
}
