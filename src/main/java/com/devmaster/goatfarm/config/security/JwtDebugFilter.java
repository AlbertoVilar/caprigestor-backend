package com.devmaster.goatfarm.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtDebugFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");
        
        System.out.println("🔍 JWT DEBUG: " + method + " " + requestURI);
        
        if (authHeader != null) {
            System.out.println("🔍 JWT DEBUG: Authorization header presente: " + authHeader.substring(0, Math.min(50, authHeader.length())) + "...");
        } else {
            System.out.println("🔍 JWT DEBUG: Authorization header ausente");
        }
        
        // Continuar com o filtro
        filterChain.doFilter(request, response);
        
        // Verificar autenticação após processamento
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            System.out.println("🔍 JWT DEBUG: Usuário autenticado: " + auth.getName());
            System.out.println("🔍 JWT DEBUG: Authorities: " + auth.getAuthorities());
        } else {
            System.out.println("🔍 JWT DEBUG: Nenhuma autenticação encontrada");
        }
        
        System.out.println("🔍 JWT DEBUG: Response status: " + response.getStatus());
        System.out.println("🔍 JWT DEBUG: ===========================================");
    }
}