package com.tanhung.antifraudsystem.config;

import com.tanhung.antifraudsystem.CustomAccessDeniedHandler;
import com.tanhung.antifraudsystem.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                                                   CustomAccessDeniedHandler customAccessDeniedHandler){
        return http
                .csrf(CsrfConfigurer::disable)
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(customAuthenticationEntryPoint))
                .exceptionHandling(denied ->
                        denied.accessDeniedHandler(customAccessDeniedHandler))
                .authorizeHttpRequests(requests-> requests
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/user/*").hasAuthority("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/auth/access", "/api/auth/role").hasAuthority("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/auth/list").hasAnyAuthority("ADMINISTRATOR","SUPPORT")
                        .requestMatchers("/api/antifraud/suspicious-ip/**", "/api/antifraud/stolencard/**").hasAnyAuthority("ADMINISTRATOR","SUPPORT")
                        .requestMatchers(HttpMethod.POST,"/api/antifraud/transaction").hasAuthority("MERCHANT")
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/authenticate").permitAll()
                        .anyRequest().denyAll())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig){
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


}
