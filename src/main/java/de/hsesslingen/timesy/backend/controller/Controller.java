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

	/**
	 * Just a generic "landing page" so it exists
	 *
	 * @param user the authenticated user or null if not authenticated
	 */
	@CrossOrigin
	@RequestMapping({"", "/"})
	public @NonNull ResponseEntity<?> index(@AuthenticationPrincipal final @Nullable OidcUser user) {
		if (user == null) {
			return new ResponseEntity<>(Map.of("message", "TimeSy Backend"), HttpStatus.OK);
		}
		return new ResponseEntity<>(Map.of("message", "Authenticated as '" + user.getUserInfo().getFullName() + "'"), HttpStatus.OK);
	}

	/**
	 * Get all rooms the given filters apply to
	 *
	 * @param user the authenticated user, or null if not authenticated
	 * @param building the name of the building to be filtered on
	 * @param floor the name of the floor to be filtered on
	 * @param roomUid the uid of the room to be filtered on
	 * @param roomName the name of the room to be filtered on (mutually exclusive with roomUid)
	 * @param courseUid the uid of the course to be filtered on
	 * @param courseName the name of the course to be filtered on (mutually exclusive with courseUid)
	 * @param roomType the type of the room to be filtered on
	 *
	 * @return a list of rooms matching the given filters if authenticated, else a 401
	 */
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

	/**
	 * Update the template of a room
	 *
	 * @param user the authenticated user, or null if not authenticated
	 * @param roomUid the uid of the room to be updated
	 * @param templateUid the uid of the template to be used for the room
	 *
	 * @return 200 if successful, 404 if the room was not found, 500 if an internal server error occurred,
	 * or 401 if not authenticated
	 */
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

	/**
	 * Get all templates
	 *
	 * @param user the authenticated user, or null if not authenticated
	 *
	 * @return all templates, or 404 if none were found, or 401 if not authenticated
	 */
	@CrossOrigin
	@GetMapping("/templates")
	public @NonNull ResponseEntity<?> getAllTemplates(@AuthenticationPrincipal final @Nullable OidcUser user) {
		if (user == null) {
			return new ResponseEntity<>(Map.of("message", "Unauthorized"), HttpStatus.UNAUTHORIZED);
		}

		return this.frontendService.getAllTemplates();
	}

	/**
	 * Re-read all templates from the filesystem
	 *
	 * @param user the authenticated user, or null if not authenticated
	 *
	 * @return all templates, or 404 if none were found, or 401 if not authenticated
	 */
	@CrossOrigin
	@GetMapping("/templates/update")
	public @NonNull ResponseEntity<?> updateTemplates(@AuthenticationPrincipal final @Nullable OidcUser user) {
		if (user == null) {
			return new ResponseEntity<>(Map.of("message", "Unauthorized"), HttpStatus.UNAUTHORIZED);
		}

		return this.frontendService.updateTemplates();
	}

	/**
	 * Get the data to render the template for a given room
	 *
	 * @param roomUid the uid of the room
	 *
	 * @return the data for the template, 500 if an internal error occurred or 401 if not authenticated
	 */
	@CrossOrigin
	@GetMapping("/templates/data/{room_uid}")
	public @NonNull ResponseEntity<?> getTemplateData(@PathVariable("room_uid") final int roomUid) {
		return this.frontendService.getTemplateData(roomUid);
	}

	/**
	 * Update the display of all rooms or a single room
	 *
	 * @param user the authenticated user, or null if not authenticated
	 * @param roomUid the uid of the room to be updated, or null to update all rooms
	 * @param imagePath the path where the screenshot should be exported to, or null to not export a screenshot
	 *
	 * @return the display dto, 500 if an internal error occurred, 404 if no display could be found, 200 if all displays were updated or 401 if not authenticated
	 */
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

	/**
	 * Debug endpoint to create dummy data in the database
	 *
	 * @param user the authenticated user, or null if not authenticated
	 *
	 * @return information of /rooms
	 */
	@CrossOrigin
	@GetMapping("/dummydata")
	public @NonNull ResponseEntity<?> createDummyData(@AuthenticationPrincipal final @Nullable OidcUser user) {
		if (user == null) {
			return new ResponseEntity<>(Map.of("message", "Unauthorized"), HttpStatus.UNAUTHORIZED);
		}

		return this.frontendService.createDummyData();
	}
}
