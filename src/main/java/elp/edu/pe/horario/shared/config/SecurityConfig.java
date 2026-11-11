package elp.edu.pe.horario.shared.config;

import elp.edu.pe.horario.application.usecase.user.LoadByUsernameUserCase;
import elp.edu.pe.horario.shared.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final LoadByUsernameUserCase loadByUsernameUserCase;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        //Rutas publicas
                        .requestMatchers("/auth/**").permitAll()

                        //Rutas privadas
                        .requestMatchers("/aula/**").hasRole("COORDINADOR")
                        .requestMatchers("/bloque-horario/**").hasRole("COORDINADOR")
                        .requestMatchers("/curso/**").hasRole("COORDINADOR")
                        .requestMatchers("/curso-seccion-docente/").hasRole("COORDINADOR")
                        .requestMatchers("/docente/**").hasRole("COORDINADOR")
                        .requestMatchers("/horario/**").hasRole("COORDINADOR")
                        .requestMatchers("/horarios/**").hasRole("COORDINADOR")
                        .requestMatchers("/periodo-academico/**").hasRole("COORDINADOR")
                        .requestMatchers("/restriccion-docente/**").hasRole("COORDINADOR")
                        .requestMatchers("/seccion-academico/**").hasRole("COORDINADOR")
                        .requestMatchers("/unidad-academica/**").hasRole("COORDINADOR")

                        //Rutas de administracion
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        //Rutas no configuradas
                        .anyRequest().authenticated())

                .userDetailsService(loadByUsernameUserCase)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
