package de.hsesslingen.timesy.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class SecurityConfig {

	@Value("${keycloak.url}")
	private String keycloakUrl;

	@Value("${spring-cloud.gateway}")
	private String gateway;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(auth ->
						auth
								.requestMatchers("/actuator/**").permitAll()
								.anyRequest().authenticated()
				)
				.oauth2Login(Customizer.withDefaults())  // Enables OAuth2 login
				.oauth2Client(Customizer.withDefaults()) // Enables OAuth2 client
				.csrf(AbstractHttpConfigurer::disable)  // Disable CSRF for APIs
				.cors(cors -> cors.configurationSource(corsConfigurationSource())); // Enable CORS

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.setAllowedOrigins(List.of(
				this.keycloakUrl,  // Keycloak
				this.gateway // Spring Cloud Gateway
		));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}