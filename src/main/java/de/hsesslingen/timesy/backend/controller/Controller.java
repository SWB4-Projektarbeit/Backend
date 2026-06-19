package de.hsesslingen.timesy.backend.controller;

import de.hsesslingen.timesy.backend.service.FrontendService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Component
@RestController
@RequiredArgsConstructor
@RequestMapping("/api-timesy")
public class Controller {

	private final @NonNull FrontendService frontendService;
	@Value("${heonline.keycloak.url}")
	private String keycloakUrl;

	@CrossOrigin
	@GetMapping("/login")
	public @NonNull ResponseEntity<?> login(
			@AuthenticationPrincipal final @Nullable OidcUser user,
			@RequestParam(required = false, name = "redirect_uri") final @Nullable String redirectUri) {
		if (user == null) {
			return new ResponseEntity<>("Not a valid user", HttpStatus.UNAUTHORIZED);
		}
		if (redirectUri != null) {
			return ResponseEntity
					.status(HttpStatus.FOUND)
					.header(HttpHeaders.LOCATION, redirectUri)
					.body("Logged in");
		}
		return new ResponseEntity<>("Logged in", HttpStatus.OK);
	}

	@CrossOrigin
	@GetMapping("/rooms")
	public @NonNull ResponseEntity<?> getAllRooms(
			@AuthenticationPrincipal final @Nullable OidcUser user,
			@RequestParam(required = false) final @Nullable String building,
			@RequestParam(required = false) final @Nullable String floor,
			@RequestParam(required = false) final @Nullable Integer roomUid,
			@RequestParam(required = false) final @Nullable String roomName,
			@RequestParam(required = false) final @Nullable Integer courseUid,
			@RequestParam(required = false) final @Nullable String courseName,
			@RequestParam(required = false) final @Nullable String roomType) {
		if (user == null) {
			return new ResponseEntity<>("Not a valid user", HttpStatus.UNAUTHORIZED);
		}
		return this.frontendService.getAllRooms(
				building, floor, roomUid, roomName, courseUid, courseName, roomType
		);
	}


	@CrossOrigin
	@PatchMapping("/rooms/{room_uid}")
	public @NonNull ResponseEntity<?> updateRoom(
			@AuthenticationPrincipal final @Nullable OidcUser user,
			@PathVariable("room_uid") final int roomUid,
			@RequestBody final int templateUid) {
		if (user == null) {
			return new ResponseEntity<>("Not a valid user", HttpStatus.UNAUTHORIZED);
		}

		return this.frontendService.updateRoom(roomUid, templateUid);
	}

	@CrossOrigin
	@GetMapping("/templates")
	public @NonNull ResponseEntity<?> getAllTemplates(@AuthenticationPrincipal final @Nullable OidcUser user) {
		if (user == null) {
			return new ResponseEntity<>("Not a valid user", HttpStatus.UNAUTHORIZED);
		}

		return this.frontendService.getAllTemplates();
	}

	@CrossOrigin
	@GetMapping("/templates/update")
	public @NonNull ResponseEntity<?> updateTemplates(@AuthenticationPrincipal final @Nullable OidcUser user) {
		if (user == null) {
			return new ResponseEntity<>("Not a valid user", HttpStatus.UNAUTHORIZED);
		}

		return this.frontendService.updateTemplates();
	}

	@CrossOrigin
	@GetMapping("/templates/data/{room_uid}")
	public @NonNull ResponseEntity<?> getTemplateData(@PathVariable("room_uid") final int roomUid) {
		return this.frontendService.getTemplateData(roomUid);
	}

	@CrossOrigin
	@GetMapping("/display/update")
	public @NonNull ResponseEntity<?> updateDisplay(
			@AuthenticationPrincipal final @Nullable OidcUser user,
			@RequestParam(required = false) final @Nullable Integer roomUid) {
		if (user == null) {
			return new ResponseEntity<>("Not a valid user", HttpStatus.UNAUTHORIZED);
		}

		return this.frontendService.updateDisplay(roomUid);
	}

	@CrossOrigin
	@GetMapping("/dummydata")
	public @NonNull ResponseEntity<?> createDummyData(@AuthenticationPrincipal final @Nullable OidcUser user) {
		if (user == null) {
			return new ResponseEntity<>("Not a valid user", HttpStatus.UNAUTHORIZED);
		}

		return this.frontendService.createDummyData();
	}
}
