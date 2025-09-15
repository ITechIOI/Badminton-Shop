package com.example.gateway.security;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Configuration
@EnableWebFluxSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    // ✅ Public routes - không cần JWT
    @Bean
    @Order(1)
    public SecurityWebFilterChain publicFilterChain(ServerHttpSecurity http) {
        http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers(
                        "/eureka/**",
                        "/products/actuator/**",
                        "/users/users/id/**",
                        "/users/**",
                        "/orders/order-details/service/**",
                        "/orders/discounts/**",
                        "/products/products/services/**",
                        "/products/products/**",
                        "/products/categories/**",
                        "/products/flash-sale/**",
                        "/products/flash-sale-detail/**",
                        "/notifications/**",
                        "/ws/**",
                        "/actuator/prometheus",
                        "/payment/success/**",
                        "/payment/cancel/**",
                        "/payment/error/**",
                        "/recommend/predict/**",
                        "/behaviors/**",
                        "/products/cloudinary/**",
                        "orders/orders/**",
                        "orders/order-details/**"
                ))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex.anyExchange().permitAll());
        return http.build();
    }

    // ✅ Secured routes - yêu cầu xác thực JWT
    @Bean
    @Order(2)
    public SecurityWebFilterChain securedFilterChain(ServerHttpSecurity http) {
        http
                .cors(Customizer.withDefaults())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex.anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor()))
                );

        return http.build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://127.0.0.1:5500"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        return jwt -> {
            List<String> realmRoles = jwt.getClaim("realm_access") != null
                    ? ((List<String>) ((Map<String, Object>) jwt.getClaim("realm_access")).get("roles"))
                    : List.of();

            logger.debug("⚡ JWT Token: {}", jwt.getTokenValue());
            logger.debug("⚡ User Roles: {}", realmRoles);

            Collection<GrantedAuthority> authorities = realmRoles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            return Mono.just(new JwtAuthenticationToken(jwt, authorities));
        };
    }
}
