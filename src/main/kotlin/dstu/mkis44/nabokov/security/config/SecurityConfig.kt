package dstu.mkis44.nabokov.security.config

import dstu.mkis44.nabokov.security.filter.JwtAuthenticationFilter
import dstu.mkis44.nabokov.security.service.TokenSerializerService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.csrf.CsrfFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
open class SecurityConfig(
    private val userDetailsService: UserDetailsService,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val passwordEncoder: PasswordEncoder
) {

    @Bean
    open fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
//            .csrf { csrf ->
//                csrf.ignoringRequestMatchers(
//                    "/auth/**",
//                    "/v3/api-docs/**",
//                    "/swagger-ui/**",
//                    "/swagger-ui.html"
//                )
//            }
            .csrf { csrf -> csrf.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/swagger-ui.html").permitAll()
                    .anyRequest().authenticated()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, CsrfFilter::class.java)

        return http.build()
    }

    @Bean
    open fun authenticationProvider(): AuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService)
        authProvider.setPasswordEncoder(passwordEncoder)
        return authProvider
    }

    @Bean
    open fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }
} 