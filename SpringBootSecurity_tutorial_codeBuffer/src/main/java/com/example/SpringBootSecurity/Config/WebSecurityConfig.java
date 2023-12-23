package com.example.SpringBootSecurity.Config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig {
    private static final String[] WHILTE_LIST_URLS = {
            "/hello",
            "/register",
            "/VerifyRegistration",
            "/savePassword",
            "/resetPassword",
            "/changePassword"};

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.
                cors(c -> c.disable())
                .csrf(c -> c.disable())
                .authorizeHttpRequests(auth -> auth.requestMatchers(WHILTE_LIST_URLS).permitAll());
        return http.build();
    }

}

