package com.mathhelper.mathhelperserver.configurers.web_security

import com.mathhelper.mathhelperserver.authorization.JwtAuthEntryPoint
import com.mathhelper.mathhelperserver.authorization.JwtAuthTokenFilter
import com.mathhelper.mathhelperserver.authorization.JwtUserDetailsServiceImpl
import com.mathhelper.mathhelperserver.authorization.OAuth2AuthenticationSuccessHandler
import com.mathhelper.mathhelperserver.services.user.OAuth2UserServiceImpl
import com.mathhelper.mathhelperserver.services.user.OidcUserServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebSecurityConfigurer : WebSecurityConfigurerAdapter() {
    @Autowired
    private lateinit var userDetailsService: JwtUserDetailsServiceImpl
    @Autowired
    private lateinit var unAuthorizedHandler: JwtAuthEntryPoint

    @Autowired
    private lateinit var oauth2SuccessHandler: OAuth2AuthenticationSuccessHandler
    @Autowired
    private lateinit var oauth2UserService: OAuth2UserServiceImpl
    @Autowired
    private lateinit var oidcUserService: OidcUserServiceImpl

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationJwtTokenFilter(): JwtAuthTokenFilter {
        return JwtAuthTokenFilter()
    }

    @Bean
    @Throws(Exception::class)
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

    @Throws(Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder())
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
            .cors().and().csrf().disable()
            .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                // you can register new user only if you are not logged in
                .antMatchers("/api/auth/signup").not().fullyAuthenticated()
                // you can sign in as any user in all situations (logged in or not)
                .antMatchers("/api/auth/signin").permitAll()
                .antMatchers("/api/auth/google_sign_in").permitAll()
                .antMatchers(HttpMethod.GET, "/api/is_running").permitAll()
                .antMatchers("/", "/error", "/webjars/**", "/user", "/login/oauth2/code/google", "/login").permitAll()
                .antMatchers(HttpMethod.GET, "/api/rule-pack/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/rule-pack/**").permitAll()
                .antMatchers(HttpMethod.PATCH, "/api/rule-pack/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/task/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/task/**").permitAll()
                .antMatchers(HttpMethod.PATCH, "/api/task/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/taskset/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/taskset/**").permitAll()
                .antMatchers(HttpMethod.PATCH, "/api/taskset/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/namespace/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/namespace/**").permitAll()
                .antMatchers(HttpMethod.PATCH, "/api/namespace/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/users/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/log/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/log/**").permitAll()
                .antMatchers(HttpMethod.PATCH, "/api/log/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/log/**").permitAll()
            // all other urls require authentication
            .anyRequest().authenticated()
            .and()
            .exceptionHandling { e: ExceptionHandlingConfigurer<HttpSecurity?> ->
                e.authenticationEntryPoint(unAuthorizedHandler).and()
                    ?.sessionManagement()?.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }.oauth2Login()
                .successHandler(oauth2SuccessHandler)
            .userInfoEndpoint()
                .userService(oauth2UserService)
                .oidcUserService(oidcUserService)

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter::class.java)
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource? {
        val configuration = CorsConfiguration()
        configuration.addAllowedOrigin("*")
        configuration.addAllowedHeader("*")
        configuration.addAllowedMethod("GET")
        configuration.addAllowedMethod("POST")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}