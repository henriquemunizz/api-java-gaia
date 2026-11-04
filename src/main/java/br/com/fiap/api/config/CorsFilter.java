package br.com.fiap.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter extends org.springframework.web.filter.CorsFilter {
    public CorsFilter(@Value("${gaia.frontend-origin}") String origem) {
        super(configurar(origem));
    }

    private static UrlBasedCorsConfigurationSource configurar(String origem) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origem.split(",")).stream()
                .map(String::trim)
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
