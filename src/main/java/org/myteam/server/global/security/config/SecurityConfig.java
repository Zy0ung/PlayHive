package org.myteam.server.global.security.config;

import lombok.RequiredArgsConstructor;
import org.myteam.server.global.security.jwt.JwtProvider;
import org.myteam.server.global.security.filter.TokenAuthenticationFilter;
import org.myteam.server.oauth2.handler.CustomOauth2SuccessHandler;
import org.myteam.server.oauth2.handler.OAuth2LoginFailureHandler;
import org.myteam.server.oauth2.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.HstsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /* 권한 제외 대상 */
    private static final String[] permitAllUrl = new String[]{
            /** @brief test */"/test/exception-test",
            /** @brief Swagger Docs */ "/v3/api-docs/**", "/swagger-ui/**",
            /** @brief database url */ "/h2-console",
            /** @brief about login */ "/auth/**",
    };

    /* Admin 접근 권한 */
    private static final String[] permitAdminUrl = new String[]{
            /** @brief Check Access Admin */ "/test/manager-access-test/**",
    };

    /* member 접근 권한 */
    private static final String[] permitMemberUrl = new String[]{
            "/test/user-access-test/**",
    };

    private final JwtProvider jwtProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOauth2SuccessHandler customOauth2SuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.headers(headers -> headers.httpStrictTransportSecurity(HstsConfig::disable)
                        .frameOptions(FrameOptionsConfig::disable)).csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable).sessionManagement(
                        sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(permitAllUrl).permitAll()
                        .requestMatchers(permitAdminUrl).hasRole("ADMIN")
                        .anyRequest().authenticated())


                .oauth2Login(oauth2 -> oauth2
                                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                                .userService(customOAuth2UserService))
                                        .successHandler(customOauth2SuccessHandler)
                                        .failureHandler(oAuth2LoginFailureHandler)
                );

        http.addFilterBefore(new TokenAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
