package de.hsesslingen.timesy.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class SecurityConfig {

	private final @NonNull KeycloakLogoutHandler keycloakLogoutHandler;
	@Value("${heonline.keycloak.url}")
	private String keycloakUrl;
	@Value("${frontend.url}")
	private String frontendUrl;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						// permit for all requests so auth can be handled in the endpoints
						.anyRequest().permitAll()
				)
				.oauth2Login(login -> login
						// custom login page
						.loginPage("/api-timesy/login"))
				.oauth2Client(Customizer.withDefaults()) // Enables OAuth2 client
				.logout(logout -> logout
						.logoutUrl("/api-timesy/logout")
						.logoutSuccessUrl("/api-timesy")
						.addLogoutHandler(keycloakLogoutHandler)
						.invalidateHttpSession(true)
						.clearAuthentication(true)
						.deleteCookies("JSESSIONID"))
				.csrf(AbstractHttpConfigurer::disable)  // Disable CSRF for APIs
				.cors(cors -> cors.configurationSource(corsConfigurationSource())); // Enable CORS

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.setAllowedOrigins(List.of(
				this.frontendUrl,
				this.keycloakUrl
		));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowedMethods(List.of("GET", "PATCH"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Component
	static class KeycloakLogoutHandler implements LogoutHandler {
		private final RestTemplate restTemplate = new RestTemplate();

		@Override
		public void logout(final @Nullable HttpServletRequest request, final @Nullable HttpServletResponse response, final @Nullable Authentication authentication) {
			if (authentication == null) {
				return;
			}
			final @Nullable OidcUser user = (OidcUser) authentication.getPrincipal();
			if (user == null) {
				return;
			}
			String endSessionEndpoint = user.getIssuer() + "/protocol/openid-connect/logout";
			UriComponentsBuilder builder = UriComponentsBuilder
					.fromUriString(endSessionEndpoint)
					.queryParam("id_token_hint", user.getIdToken().getTokenValue());

			ResponseEntity<?> logoutResponse = restTemplate.getForEntity(builder.toUriString(), String.class);
			if (!logoutResponse.getStatusCode().is2xxSuccessful()) {
				log.warn("Could not propagate logout to Keycloak with status code: '{}'", logoutResponse.getStatusCode());
			}
		}
	}
}