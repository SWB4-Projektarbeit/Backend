package de.hsesslingen.timesy.backend.controller;

import de.hsesslingen.timesy.backend.dto.BuildingDTO;
import de.hsesslingen.timesy.backend.mapper.Mapper;
import de.hsesslingen.timesy.backend.model.Display;
import de.hsesslingen.timesy.backend.repository.DisplayRepository;
import de.hsesslingen.timesy.backend.repository.TemplateRepository;
import de.hsesslingen.timesy.backend.service.DisplayService;
import de.hsesslingen.timesy.backend.service.HEOnlineService;
import de.hsesslingen.timesy.backend.service.UpdateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api-timesy")
public class Controller {

	private final Mapper mapper;

	private final UpdateService updateService;
	private final DisplayService displayService;
	private final HEOnlineService heOnlineService;
	private final DisplayRepository displayRepository;
	private final TemplateRepository templateRepository;

	public Controller(Mapper mapper,
	                  UpdateService updateService,
	                  DisplayService displayService,
	                  HEOnlineService heOnlineService,
	                  DisplayRepository displayRepository,
	                  TemplateRepository templateRepository,
	                  @Value("${dummydata}") final boolean dummyData) {
		this.mapper = mapper;
		this.updateService = updateService;
		this.displayService = displayService;
		this.heOnlineService = heOnlineService;
		this.displayRepository = displayRepository;
		this.templateRepository = templateRepository;
		if (dummyData) {
			this.createDummyData();
		}
	}

	@CrossOrigin
	@GetMapping("/rooms")
	public ResponseEntity<?> getAllRooms(@RequestParam(required = false) final String building,
	                                     @RequestParam(required = false) final String floor,
	                                     @RequestParam(required = false) final Integer roomUid,
	                                     @RequestParam(required = false) final String roomName,
	                                     @RequestParam(required = false) final Integer courseUid,
	                                     @RequestParam(required = false) final String courseName) {
		if (roomUid != null && roomName != null) {
			return new ResponseEntity<>("ROOM_UID and ROOM_NAME are mutually exclusive", HttpStatus.BAD_REQUEST);
		}

		if (courseUid != null && courseName != null) {
			return new ResponseEntity<>("COURSE_UID and COURSE_NAME are mutually exclusive", HttpStatus.BAD_REQUEST);
		}

		List<BuildingDTO> buildingDTOS = mapper.toBuildingDTOs(
				heOnlineService.getAppointments(),
				building,
				floor,
				roomUid,
				roomName,
				courseUid,
				courseName
		);
		if (buildingDTOS == null) {
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
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@CrossOrigin
	@GetMapping("/templates")
	public ResponseEntity<?> getAllTemplates() {
		Collection<TemplateRepository.Template> templates = templateRepository.findAll();
		if (templates.isEmpty()) {
			return new ResponseEntity<>("No templates found.", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(templates, HttpStatus.OK);
	}

	@CrossOrigin
	@GetMapping("/templates/update")
	public ResponseEntity<?> updateTemplates() {
		templateRepository.readTemplates();
		return getAllTemplates();
	}

	@CrossOrigin
	@PatchMapping("/rooms/{room_uid}")
	public ResponseEntity<?> updateRoom(@PathVariable("room_uid") final int roomUid, @RequestBody final int templateUid) {
		List<Display> displayData = displayRepository.findByRoomUid(roomUid);
		if (displayData.isEmpty()) {
			return new ResponseEntity<>("No display found for '" + roomUid + "'", HttpStatus.NOT_FOUND);
		}

		Optional<TemplateRepository.Template> templateData = templateRepository.getByUid(templateUid);
		if (templateData.isEmpty()) {
			return new ResponseEntity<>("No valid template found for the display at room'" + roomUid + "'", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		Display display = displayData.getFirst();
		display.setTemplateUid(templateUid);
		return new ResponseEntity<>(displayRepository.save(display), HttpStatus.OK);
	}

	@CrossOrigin
	@GetMapping("/display/update")
	public ResponseEntity<?> updateDisplay(@RequestParam(required = false) final Integer roomUid) {
		if (roomUid == null) {
			updateService.updateDisplays();
			return new ResponseEntity<>(HttpStatus.OK);
		}

		List<Display> displayData = displayRepository.findByRoomUid(roomUid);
		if (displayData.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		Display display = displayData.getFirst();
		Optional<TemplateRepository.Template> templateData = templateRepository.getByUid(display.getTemplateUid());
		if (templateData.isEmpty()) {
			return new ResponseEntity<>("No valid template found for the display at room'" + roomUid + "'", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		displayService.sendImage(display.getDisplayUid(), templateData.get().getTemplatePath());
		return new ResponseEntity<>(display, HttpStatus.OK);
	}

	@CrossOrigin
	@GetMapping("/dummydata")
	public ResponseEntity<?> createDummyData() {
		Display display1 = new Display(
				null,
				23,
				6976,
				123,
				"Room1",
				"Building1",
				"Ground floor",
				new ArrayList<>()
		);
		displayRepository.save(display1);

		Display display2 = new Display(
				null,
				24,
				6977,
				124,
				"Room2",
				"Building2",
				"First floor",
				new ArrayList<>()
		);
		displayRepository.save(display2);

		return getAllRooms(null, null, null, null, null, null);
	}
}
