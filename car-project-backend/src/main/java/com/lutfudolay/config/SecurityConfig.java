package com.lutfudolay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/license/**").permitAll()
                .requestMatchers("/api/obd/**").permitAll()
                .requestMatchers("/api/vag/**").permitAll()
                .requestMatchers("/api/history/**").permitAll()
                .anyRequest().permitAll() 
            )
            .formLogin(login -> login.disable())
            .httpBasic(basic -> basic.disable());
        return http.build();
    }
}
