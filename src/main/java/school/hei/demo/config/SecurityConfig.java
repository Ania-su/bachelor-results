package school.hei.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import school.hei.demo.security.JwtAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .formLogin(formLogin -> formLogin.disable())
        .httpBasic(httpBasic -> httpBasic.disable())
        .sessionManagement(
            sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/auth/login")
                    .permitAll()
                    .requestMatchers("/web/login")
                    .permitAll()
                    .requestMatchers("/assets/**")
                    .permitAll()
                    .requestMatchers("/web/promotions/**")
                    .hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                    .requestMatchers(HttpMethod.GET, "/users")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(HttpMethod.GET, "/users/*")
                    .hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                    .requestMatchers("/users/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/specialties", "/specialties/**")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(
                        HttpMethod.GET, "/courses/*/assignments", "/courses/*/assignments/**")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(
                        HttpMethod.GET, "/courses/*/exams/*/grades", "/courses/*/exams/*/grades/**")
                    .hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                    .requestMatchers("/courses/*/exams/*/grades", "/courses/*/exams/*/grades/**")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(
                        HttpMethod.GET, "/courses/*/exams/*/groups", "/courses/*/exams/*/groups/**")
                    .hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                    .requestMatchers("/courses/*/exams/*/groups", "/courses/*/exams/*/groups/**")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(HttpMethod.GET, "/courses/*/exams", "/courses/*/exams/**")
                    .hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                    .requestMatchers("/courses/*/exams", "/courses/*/exams/**")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(HttpMethod.GET, "/courses", "/courses/**")
                    .hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                    .requestMatchers(HttpMethod.GET, "/groups", "/groups/**")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(HttpMethod.GET, "/me/transcript-email")
                    .hasRole("STUDENT")
                    .requestMatchers(HttpMethod.GET, "/students/*/transcript")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .hasRole("ADMIN"))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
