package de.hsesslingen.timesy.backend.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@Component
@RestController
@RequiredArgsConstructor
public class LoginController {

	private @Nullable String redirectUri;

	/**
	 * Login endpoint
	 * If redirect_uri is given, it is saved so a redirect can happen after a successful login
	 *
	 * @param redirectUri the uri to be redirected to
	 *
	 * @return redirect to the actual auth page
	 */
	@CrossOrigin
	@RequestMapping("/api-timesy/login")
	public @NonNull ResponseEntity<?> auth(@RequestParam(required = false, name = "redirect_uri") final @Nullable String redirectUri) {
		this.redirectUri = redirectUri;
		return ResponseEntity
				.status(HttpStatus.FOUND)
				.header(HttpHeaders.LOCATION, "/oauth2/authorization/swt-project-bff")
				.build();
	}

	/**
	 * Index for the backend, redirects if a redirect_uri was saved by a previous login request and the user is authenticated
	 *
	 * @param user the authenticated user or null if not authenticated
	 *
	 * @return redirect if the user is authenticated and a redirect_uri was saved, else a 200
	 */
	@CrossOrigin
	@RequestMapping(value = {"", "/"})
	public ResponseEntity<?> index(@AuthenticationPrincipal final @Nullable OidcUser user) {
		try {
			if (user == null) {
				return new ResponseEntity<>(Map.of("message", "TimeSy Backend"), HttpStatus.OK);
			}
			if (this.redirectUri != null) {
				return ResponseEntity
						.status(HttpStatus.FOUND)
						.header(HttpHeaders.LOCATION, this.redirectUri)
						.build();
			}
			return new ResponseEntity<>(Map.of("message", "Authenticated as '" + user.getUserInfo().getFullName() + "'"), HttpStatus.OK);
		} finally {
			this.redirectUri = null;
		}
	}
}
