package com.ann.annovation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration // 이 파일이 스프링의 환경 설정 파일임을 의미하는 어노테이션
@EnableWebSecurity // 모든 요청 URL이 스프링 시큐리티의 제어를 받도록 만드는 어노테이션 (스프링 시큐리티 활성화)
@EnableMethodSecurity(prePostEnabled = true) // prePostEnabled = true : @PreAuthorize 어노테이션을 사용하기 위해 반드시 필요한 설정
public class SecurityConfig {
    @Bean // 스프링 시큐리티의 세부 설정 (SecurityFilterChain 빈을 생성하여 설정)
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 로그인하지 않더라도 모든 페이지에 접근할 수 있도록 인증되지 않은 모든 페이지의 요청을 허락
        http
            .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                .requestMatchers(new AntPathRequestMatcher("/**")).permitAll())
            // h2-console/로 시작하는 모든 URL은 CSRF 검증을 하지 않는다는 설정을 추가
            .csrf((csrf) -> csrf
                .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")))
            .headers((headers) -> headers
                .addHeaderWriter(new XFrameOptionsHeaderWriter(
                    XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
            // 로그인 URL 설정
            // .formLogin : Spring Security 로그인 설정 Method
            .formLogin((formLogin) -> formLogin
                .loginPage("/user/login") // 로그인 페이지의 URL
                .defaultSuccessUrl("/")) // 로그인 성공 시에 이동할 페이지 URL
             .logout((logout) -> logout
                 .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout")) // 로그아웃 URL
                 .logoutSuccessUrl("/") // 로그아웃 성공 시 이동할 페이지 URL
                 .invalidateHttpSession(true)) // 로그아웃 시 생성된 사용자 세션 삭제
             .oauth2Login(Customizer.withDefaults());
        return http.build();
    }
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager : 사용자 인증 시 UserSecurityService와 PasswordEncoder를 내부적으로 사용하여 인증과 권한 부여 프로세스를 처리
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}