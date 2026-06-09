package de.hsesslingen.timesy.backend.controller;

import de.hsesslingen.timesy.backend.dto.BuildingDTO;
import de.hsesslingen.timesy.backend.dto.TemplateDataDTO;
import de.hsesslingen.timesy.backend.mapper.Mapper;
import de.hsesslingen.timesy.backend.mapper.TemplateDataMapper;
import de.hsesslingen.timesy.backend.model.Display;
import de.hsesslingen.timesy.backend.repository.DisplayRepository;
import de.hsesslingen.timesy.backend.repository.TemplateRepository;
import de.hsesslingen.timesy.backend.service.DisplayService;
import de.hsesslingen.timesy.backend.service.HEOnlineService;
import de.hsesslingen.timesy.backend.service.UpdateService;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RestController
@RequiredArgsConstructor
@RequestMapping("/api-timesy")
public class Controller {

	private final Mapper mapper;
	private final TemplateDataMapper templateDataMapper;

	private final @NonNull UpdateService updateService;
	private final @NonNull DisplayService displayService;
	private final @NonNull HEOnlineService heOnlineService;
	private final @NonNull DisplayRepository displayRepository;
	private final @NonNull TemplateRepository templateRepository;
	@Value("${dummydata}")
	private boolean dummyData;

	@PostConstruct
	private void initDD() {
		if (this.dummyData) {
			this.createDummyData();
		}
	}

	@CrossOrigin
	@GetMapping("/rooms")
	public @NonNull ResponseEntity<?> getAllRooms(@RequestParam(required = false) final @Nullable String building,
												  @RequestParam(required = false) final @Nullable String floor,
												  @RequestParam(required = false) final @Nullable Integer roomUid,
												  @RequestParam(required = false) final @Nullable String roomName,
												  @RequestParam(required = false) final @Nullable Integer courseUid,
												  @RequestParam(required = false) final @Nullable String courseName,
												  @RequestParam(required = false) final @Nullable String roomType) {
		if (null != roomUid && null != roomName) {
			return new ResponseEntity<>("ROOM_UID and ROOM_NAME are mutually exclusive", HttpStatus.BAD_REQUEST);
		}

		if (courseUid != null && courseName != null) {
			return new ResponseEntity<>("COURSE_UID and COURSE_NAME are mutually exclusive", HttpStatus.BAD_REQUEST);
		}

		final @Nullable List<BuildingDTO> buildingDTOS = mapper.toBuildingDTOs(
				this.heOnlineService.getAppointments(),
				building,
				floor,
				roomUid,
				roomName,
				courseUid,
				courseName,
				roomType
		);
		if (null == buildingDTOS) {
			return new ResponseEntity<>("Error while getting BuildingDTOs", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (buildingDTOS.isEmpty()) {
			return new ResponseEntity<>("No BuildingDTOs found", HttpStatus.NOT_FOUND);
		}
		try {
			return new ResponseEntity<>(
					buildingDTOS,
					HttpStatus.OK
			);
		} catch (final Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@CrossOrigin
	@GetMapping("/templates")
	public @NonNull ResponseEntity<?> getAllTemplates() {
		final @NonNull Collection<TemplateRepository.Template> templates = this.templateRepository.findAll();
		if (templates.isEmpty()) {
			return new ResponseEntity<>("No templates found", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(templates, HttpStatus.OK);
	}

	@CrossOrigin
	@GetMapping("/templates/data/{room_uid}")
	public @NonNull ResponseEntity<?> getTemplateData(@PathVariable("room_uid") final int roomUid) {
		final @Nullable TemplateDataDTO templateData = this.templateDataMapper.getTemplateDataDTO(roomUid);
		if (null == templateData) {
			return new ResponseEntity<>("Error while getting TemplateData", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(templateData, HttpStatus.OK);
	}

	@CrossOrigin
	@GetMapping("/templates/update")
	public @NonNull ResponseEntity<?> updateTemplates() {
		this.templateRepository.readTemplates();
		return getAllTemplates();
	}

	@CrossOrigin
	@PatchMapping("/rooms/{room_uid}")
	public @NonNull ResponseEntity<?> updateRoom(@PathVariable("room_uid") final int roomUid, @RequestBody final int templateUid) {
		final @Nullable List<Display> displayData = this.displayRepository.findByRoomUid(roomUid);
		if (displayData == null || displayData.isEmpty()) {
			return new ResponseEntity<>("No display found for '" + roomUid + "'", HttpStatus.NOT_FOUND);
		}

		final @NonNull Optional<TemplateRepository.Template> templateData = this.templateRepository.getByUid(templateUid);
		if (templateData.isEmpty()) {
			return new ResponseEntity<>("No valid template found for the display at room'" + roomUid + "'", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		final @NonNull Display display = displayData.getFirst();
		display.setTemplateUid(templateUid);
		return new ResponseEntity<>(this.displayRepository.save(display), HttpStatus.OK);
	}

	@CrossOrigin
	@GetMapping("/display/update")
	public @NonNull ResponseEntity<?> updateDisplay(@RequestParam(required = false) final @Nullable Integer roomUid) {
		if (null == roomUid) {
			this.updateService.updateDisplays();
			return new ResponseEntity<>(HttpStatus.OK);
		}

		final @Nullable List<Display> displays = this.displayRepository.findByRoomUid(roomUid);
		if (displays == null || displays.isEmpty()) {
			return new ResponseEntity<>("No displays found", HttpStatus.NOT_FOUND);
		}

		final @NonNull Display display = displays.getFirst();
		final @NonNull Optional<TemplateRepository.Template> templateData = this.templateRepository.getByUid(display.getTemplateUid());
		if (templateData.isEmpty()) {
			return new ResponseEntity<>("No valid template found for the display at room'" + roomUid + "'", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		this.displayService.sendImage(display, templateData.get().templatePath());
		return new ResponseEntity<>(display, HttpStatus.OK);
	}

	@CrossOrigin
	@GetMapping("/dummydata")
	public @NonNull ResponseEntity<?> createDummyData() {
		final @NonNull Display display1 = new Display(
				null,
				23,
				6976,
				123,
				"Room1",
				"Vorlesungssaal",
				"Lecture Hall",
				"Building1",
				"Ground floor",
				new ArrayList<>()
		);
		this.displayRepository.save(display1);

		final @NonNull Display display2 = new Display(
				null,
				24,
				6977,
				124,
				"Room2",
				"Vorlesungssaal",
				"Lecture Hall",
				"Building2",
				"First floor",
				new ArrayList<>()
		);
		this.displayRepository.save(display2);

		return getAllRooms(null, null, null, null, null, null, null);
	}
}
