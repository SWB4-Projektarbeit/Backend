package de.hsesslingen.timesy.backend.controller;

import de.hsesslingen.timesy.backend.service.FrontendService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Component
@RestController
@RequiredArgsConstructor
@RequestMapping("/api-timesy")
public class Controller {

	private final @NonNull FrontendService frontendService;

	@CrossOrigin
	@RequestMapping({"", "/"})
	public @NonNull ResponseEntity<?> index(@AuthenticationPrincipal final @Nullable OidcUser user) {
		if (user == null) {
			return new ResponseEntity<>(Map.of("message", "TimeSy Backend"), HttpStatus.OK);
		}
		return new ResponseEntity<>(Map.of("message", "Authenticated as '" + user.getUserInfo().getFullName() + "'"), HttpStatus.OK);
	}

	@CrossOrigin
	@GetMapping("/rooms")
	public @NonNull ResponseEntity<?> getAllRooms(
			@AuthenticationPrincipal final @Nullable OidcUser user,
			@RequestParam(required = false, name = "building") final @Nullable String building,
			@RequestParam(required = false, name = "floor") final @Nullable String floor,
			@RequestParam(required = false, name = "room_uid") final @Nullable Integer roomUid,
			@RequestParam(required = false, name = "room_name") final @Nullable String roomName,
			@RequestParam(required = false, name = "course_uid") final @Nullable Integer courseUid,
			@RequestParam(required = false, name = "course_name") final @Nullable String courseName,
			@RequestParam(required = false, name = "room_type") final @Nullable String roomType) {
		if (user == null) {
			return new ResponseEntity<>(Map.of("message", "Unauthorized"), HttpStatus.UNAUTHORIZED);
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
			return new ResponseEntity<>(Map.of("message", "Unauthorized"), HttpStatus.UNAUTHORIZED);
		}

		return this.frontendService.updateRoom(roomUid, templateUid);
	}

	@CrossOrigin
	@GetMapping("/templates")
	public @NonNull ResponseEntity<?> getAllTemplates(@AuthenticationPrincipal final @Nullable OidcUser user) {
		if (user == null) {
			return new ResponseEntity<>(Map.of("message", "Unauthorized"), HttpStatus.UNAUTHORIZED);
		}

		return this.frontendService.getAllTemplates();
	}

	@CrossOrigin
	@GetMapping("/templates/update")
	public @NonNull ResponseEntity<?> updateTemplates(@AuthenticationPrincipal final @Nullable OidcUser user) {
		if (user == null) {
			return new ResponseEntity<>(Map.of("message", "Unauthorized"), HttpStatus.UNAUTHORIZED);
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
			@RequestParam(required = false, name = "room_uid") final @Nullable Integer roomUid,
			@RequestParam(required = false, name = "image_path") final @Nullable String imagePath) {
		if (user == null) {
			return new ResponseEntity<>(Map.of("message", "Unauthorized"), HttpStatus.UNAUTHORIZED);
		}

		return this.frontendService.updateDisplay(roomUid, imagePath);
	}

	@CrossOrigin
	@GetMapping("/dummydata")
	public @NonNull ResponseEntity<?> createDummyData(@AuthenticationPrincipal final @Nullable OidcUser user) {
		if (user == null) {
			return new ResponseEntity<>(Map.of("message", "Unauthorized"), HttpStatus.UNAUTHORIZED);
		}

		return this.frontendService.createDummyData();
	}
}
