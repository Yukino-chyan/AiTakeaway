package com.aitakeaway.server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig{

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/doc.html", "/webjars/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/merchant/create").hasRole("MERCHANT")
                .requestMatchers(HttpMethod.PUT, "/api/merchant/update").hasRole("MERCHANT")
                .requestMatchers(HttpMethod.PUT, "/api/merchant/status").hasRole("MERCHANT")
                .requestMatchers(HttpMethod.GET, "/api/merchant/my-shop").hasRole("MERCHANT")
                .requestMatchers(HttpMethod.POST, "/api/dish/create").hasRole("MERCHANT")
                .requestMatchers(HttpMethod.PUT, "/api/dish/update").hasRole("MERCHANT")
                .requestMatchers(HttpMethod.PUT, "/api/dish/status").hasRole("MERCHANT")
                .requestMatchers(HttpMethod.DELETE, "/api/dish/**").hasRole("MERCHANT")
                .requestMatchers(HttpMethod.GET, "/api/dish/my-list").hasRole("MERCHANT")
                // 订单 - 用户端
                .requestMatchers(HttpMethod.POST,   "/api/order/place").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.PUT,     "/api/order/*/cancel").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.GET,     "/api/order/my-list").hasRole("CUSTOMER")
                // 订单 - 商家端
                .requestMatchers(HttpMethod.GET,     "/api/order/merchant-list").hasRole("MERCHANT")
                .requestMatchers(HttpMethod.PUT,     "/api/order/*/confirm").hasRole("MERCHANT")
                .requestMatchers(HttpMethod.PUT,     "/api/order/*/deliver").hasRole("MERCHANT")
                .requestMatchers(HttpMethod.PUT,     "/api/order/*/complete").hasRole("MERCHANT")
                // 订单详情两端都能看（在 service 层做权限细分）
                .requestMatchers(HttpMethod.GET,     "/api/order/*").authenticated()
                // 购物车接口（仅顾客可用）
                .requestMatchers("/api/cart/**").hasRole("CUSTOMER")
                .anyRequest().authenticated()
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, e) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token无效\"}");
                })

                .accessDeniedHandler((request, response, e) -> {
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
                })

            )

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
