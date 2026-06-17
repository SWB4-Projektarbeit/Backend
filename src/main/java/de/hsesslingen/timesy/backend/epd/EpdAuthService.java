package de.hsesslingen.timesy.backend.epd;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Manages a session with the EPD Management Console.
 * The EPD API authenticates via session cookie: POST /api/users/auth returns a Set-Cookie
 * header that must be included in all subsequent requests.
 */
@Slf4j
@Service
public class EpdAuthService {

	private final @NonNull RestClient restClient;
	private final @NonNull String username;
	private final @NonNull String password;

	private @Nullable String sessionCookie;

	public EpdAuthService(
			@Value("${displayserver.url}") final @NonNull String displayServerUrl,
			@Value("${displayserver.username:admin}") final @NonNull String username,
			@Value("${displayserver.password:admin}") final @NonNull String password) {
		this.restClient = RestClient.builder().baseUrl(displayServerUrl).build();
		this.username = username;
		this.password = password;
	}

	/**
	 * Returns a valid session cookie, authenticating first if necessary.
	 * Returns null if authentication fails.
	 */
	public synchronized @Nullable String getSessionCookie() {
		if (this.sessionCookie == null) {
			this.authenticate();
		}
		return this.sessionCookie;
	}

	/**
	 * Clears the cached session so the next call to getSessionCookie() re-authenticates.
	 */
	public synchronized void invalidateSession() {
		this.sessionCookie = null;
	}

	private void authenticate() {
		final @NonNull String body = String.format(
				"{\"data\":{\"name\":\"%s\",\"password\":\"%s\"}}",
				this.username, this.password);
		try {
			final @NonNull ResponseEntity<String> response = this.restClient.post()
					.uri("/api/users/auth")
					.contentType(MediaType.APPLICATION_JSON)
					.body(body)
					.retrieve()
					.toEntity(String.class);

			final @Nullable List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
			if (cookies != null && !cookies.isEmpty()) {
				// Extract the name=value portion (everything before the first semicolon)
				this.sessionCookie = cookies.get(0).split(";")[0];
				log.info("EPD server: authentication successful.");
			} else {
				log.warn("EPD server: authentication response contained no Set-Cookie header.");
			}
		} catch (final Exception e) {
			log.error("EPD server: authentication failed — {}", e.getMessage());
			this.sessionCookie = null;
		}
	}
}
