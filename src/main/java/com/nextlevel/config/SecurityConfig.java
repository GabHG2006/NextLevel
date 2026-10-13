package com.nextlevel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import com.nextlevel.security.CustomAuthenticationFailureHandler;
import com.nextlevel.security.CustomAuthenticationSuccessHandler;
import com.nextlevel.security.CustomUserDetailsService;
import com.nextlevel.security.JwtAuthenticationFilter;
import com.nextlevel.security.JwtCookieService;
import com.nextlevel.security.OAuth2AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            DaoAuthenticationProvider daoAuthenticationProvider,
            CustomAuthenticationSuccessHandler successHandler,
            CustomAuthenticationFailureHandler failureHandler,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtCookieService jwtCookieService,
            OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler) throws Exception {
        http.authenticationProvider(daoAuthenticationProvider)
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/login?expired")
                .maximumSessions(1).maxSessionsPreventsLogin(true).expiredUrl("/login?expired"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/registro", "/catalogo", "/producto/**", "/productos",
                        "/admin/login", "/auth/login", "/auth/logout", "/css/**", "/images/**", "/webjars/**",
                        "/403", "/404", "/error", "/error/**", "/carrito", "/carrito/**", "/pedido/detalle",
                        "/pedido/continuar", "/ubicacion", "/auth/oauth2/google/**", "/oauth2/**", "/login/oauth2/**")
                .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registro").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/email-exists").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/pedido/mis-pedidos", "/pedido/realizar", "/perfil/**").hasAnyRole("CLIENTE", "ADMIN")
                .requestMatchers("/api/auth/me").hasAnyRole("CLIENTE", "ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login")
                .usernameParameter("correo").passwordParameter("password")
                .successHandler(successHandler).failureHandler(failureHandler).permitAll())
            .oauth2Login(oauth -> oauth
                .loginPage("/login")
                .successHandler(oAuth2AuthenticationSuccessHandler))
            .logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .addLogoutHandler((request, response, authentication) -> jwtCookieService.clearJwtCookie(response))
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true).deleteCookies("JSESSIONID", "NEXTLEVELSESSION", "JWT_TOKEN").permitAll())
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(403);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"error\":\"Acceso denegado\",\"message\":\"No tienes permisos para este recurso.\"}");
                    } else {
                        response.sendRedirect("/403");
                    }
                })
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(401);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"error\":\"No autorizado\",\"message\":\"Debes iniciar sesión o enviar un token JWT válido.\"}");
                    } else {
                        response.sendRedirect("/login?error");
                    }
                }))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() { return new HttpSessionEventPublisher(); }
}
