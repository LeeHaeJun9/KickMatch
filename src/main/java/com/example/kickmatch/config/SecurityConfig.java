package com.example.kickmatch.config;

import com.example.kickmatch.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;



@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApplicationContext applicationContext; // 💡 추가

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 🔐 CSRF 보호 해제: 특정 경로만 (Deprecated 없이)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**", "/users/register", "/users/login", "/users/logout", "/users/delete")
                )

                // 🧱 H2 콘솔 iframe 허용 (Spring Security 6+)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )

                // ✅ URL 별 보안 정책 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**", "/users/register", "/users/login", "/css/**", "/js/**","/","/images/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/match/create").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                // 🔑 로그인 설정
                .formLogin(form -> form
                        .loginPage("/users/login")
                        .defaultSuccessUrl("/")
                        .failureUrl("/users/login?error=true")
                        .permitAll()
                )

                // 🚪 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/users/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(new CustomAccessDeniedHandler())
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
