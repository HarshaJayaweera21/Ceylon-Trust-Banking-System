package com.ceylonbank.webbasedbankingsystem.config;

import com.ceylonbank.webbasedbankingsystem.security.CustomUserDetailsService;
import com.ceylonbank.webbasedbankingsystem.security.AuditAuthenticationSuccessHandler;
import com.ceylonbank.webbasedbankingsystem.security.AuditAuthenticationFailureHandler;
import com.ceylonbank.webbasedbankingsystem.security.AuditLogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private AuditAuthenticationSuccessHandler auditAuthenticationSuccessHandler;

    @Autowired
    private AuditAuthenticationFailureHandler auditAuthenticationFailureHandler;

    @Autowired
    private AuditLogoutSuccessHandler auditLogoutSuccessHandler;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/api/users/register").permitAll()
                        .requestMatchers("/api/users/test-db").permitAll()
                        .requestMatchers("/api/users/test-controller").permitAll()
                        .requestMatchers("/api/users/**").hasRole("SystemAdministrator")
                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/dashboard/**").hasRole("Customer")
                        .requestMatchers("/loan-application/**").hasRole("Customer")
                        .requestMatchers("/admin/feedback/**").hasAnyRole("SystemAdministrator", "BankManager")
                        .requestMatchers("/admin/**").hasRole("SystemAdministrator")
                        .requestMatchers("/cashier/**").hasRole("Cashier")
                        .requestMatchers("/manager/**", "/bank-manager/**").hasRole("BankManager")
                        .requestMatchers("/loan-officer/**").hasRole("LoanOfficer")
                        .requestMatchers("/support/**").hasRole("CustomerServiceExecutive")
                        .requestMatchers("/faq/**").authenticated()
                        .requestMatchers("/", "/login", "/staff-login", "/signup", "/signup-step2", "/signup-step3", "/reset-password", "/news", "/news/**", "/access-denied").permitAll()
                        .requestMatchers("/profile/**").authenticated()
                        .requestMatchers("/contact/**").hasRole("Customer")
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .successHandler(auditAuthenticationSuccessHandler)
                        .failureHandler(auditAuthenticationFailureHandler)
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(auditLogoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling((exceptions) -> exceptions
                        .accessDeniedPage("/access-denied"))
                .csrf((csrf) -> csrf
                        .ignoringRequestMatchers("/api/notifications/**", "/api/users/register"));
        return http.build();
    }

    @Bean
    public CustomUserDetailsService customUserDetailsService() {
        return new CustomUserDetailsService();
    }
}