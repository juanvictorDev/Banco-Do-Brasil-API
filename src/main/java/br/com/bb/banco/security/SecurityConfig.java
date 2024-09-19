package br.com.bb.banco.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**").disable())
        .authorizeHttpRequests(req -> req.requestMatchers("/h2-console/**").permitAll())
        .headers(headers -> headers.frameOptions(config -> config.disable()));

        return http.build();
    }   
    
}
