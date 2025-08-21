package com.devmaster.goatfarm.authority.config.customgrant;

import com.devmaster.goatfarm.authority.model.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CustomTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {
        Authentication authentication = context.getPrincipal();

        if ("access_token".equals(context.getTokenType().getValue())) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof User user) {
                context.getClaims().claim("userId", user.getId());
                context.getClaims().claim("userName", user.getName());
                context.getClaims().claim("userEmail", user.getEmail());

                // ✅ Converte as authorities para lista de strings (ex: ["ROLE_ADMIN"])
                context.getClaims().claim("authorities",
                        user.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList())
                );
            } else {
                System.out.println("⚠ TokenCustomizer: principal não é do tipo User: " + principal.getClass().getName());
            }
        }
    }
}
