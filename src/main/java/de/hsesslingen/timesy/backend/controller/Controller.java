package de.hsesslingen.timesy.backend.controller;

import de.hsesslingen.timesy.backend.dto.BuildingDTO;
import de.hsesslingen.timesy.backend.mapper.Mapper;
import de.hsesslingen.timesy.backend.model.Display;
import de.hsesslingen.timesy.backend.repository.DisplayRepository;
import de.hsesslingen.timesy.backend.repository.TemplateRepository;
import de.hsesslingen.timesy.backend.service.DisplayService;
import de.hsesslingen.timesy.backend.service.HEOnlineService;
import de.hsesslingen.timesy.backend.service.UpdateService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/api-timesy")
public class Controller {

    private final Mapper mapper;

    private final UpdateService updateService;
    private final DisplayService displayService;
    private final HEOnlineService heOnlineService;
    private final DisplayRepository displayRepository;
    private final TemplateRepository templateRepository;

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

        try {
            return new ResponseEntity<>(
                    mapper.toBuildingDTOs(
                            heOnlineService.getAppointments(),
                            building,
                            floor,
                            roomUid,
                            roomName,
                            courseUid,
                            courseName
                    ),
                    HttpStatus.OK
            );
        }  catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @CrossOrigin
    @GetMapping("/templates")
    public ResponseEntity<?> getAllTemplates() {
        return new ResponseEntity<>(templateRepository.findAll(), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/templates/update")
    public ResponseEntity<?> updateTemplates() {
        templateRepository.readTemplates();
        return new ResponseEntity<>(templateRepository.findAll(), HttpStatus.OK);
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

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
