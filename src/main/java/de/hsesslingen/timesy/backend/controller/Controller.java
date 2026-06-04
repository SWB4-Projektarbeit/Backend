package de.hsesslingen.timesy.backend.controller;

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

    @GetMapping("/rooms")
    public ResponseEntity<?> getAllRooms(final @RequestParam(required = false) String building,
                                         final @RequestParam(required = false) String floor,
                                         final @RequestParam(required = false) Integer room_uid,
                                         final @RequestParam(required = false) String room_name,
                                         final @RequestParam(required = false) Integer course_uid,
                                         final @RequestParam(required = false) String course_name) {
        if (room_uid != null && room_name != null) {
            return new ResponseEntity<>("ROOM_UID and ROOM_NAME are mutually exclusive", HttpStatus.BAD_REQUEST);
        }

        if (course_uid != null && course_name != null) {
            return new ResponseEntity<>("COURSE_UID and COURSE_NAME are mutually exclusive", HttpStatus.BAD_REQUEST);
        }

        try {
            return new ResponseEntity<>(
                    mapper.toBuildingDTOs(
                            heOnlineService.getAppointments(),
                            building,
                            floor,
                            room_uid,
                            room_name,
                            course_uid,
                            course_name
                    ),
                    HttpStatus.OK
            );
        }  catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/templates")
    public ResponseEntity<?> getAllTemplates() {
        return new ResponseEntity<>(templateRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/templates/update")
    public ResponseEntity<?> updateTemplates() {
        templateRepository.readTemplates();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/rooms/{room_uid}")
    public ResponseEntity<?> updateRoom(@PathVariable("room_uid") final int room_uid, @RequestBody final String templateUid) {
        Optional<Display> displayData = displayRepository.findByRoomUid(room_uid);
        if (displayData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<TemplateRepository.Template> templateData = templateRepository.getByUid(templateUid);
        if (templateData.isEmpty()) {
            return new ResponseEntity<>("No valid template found for the display at room'" + room_uid + "'", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Display display = displayData.get();
        display.setTemplateUid(templateUid);
        return new ResponseEntity<>(displayRepository.save(display), HttpStatus.OK);
    }

    @GetMapping("/display/update")
    public ResponseEntity<?> updateDisplay(final @RequestParam(required = false) Integer room_uid) {
        if (room_uid == null) {
            updateService.updateDisplays();
            return new ResponseEntity<>(HttpStatus.OK);
        }

        Optional<Display> displayData = displayRepository.findByRoomUid(room_uid);
        if (displayData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Display display = displayData.get();
        Optional<TemplateRepository.Template> templateData = templateRepository.getByUid(display.getTemplateUid());
        if (templateData.isEmpty()) {
            return new ResponseEntity<>("No valid template found for the display at room'" + room_uid + "'", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        displayService.sendImage(display.getDisplayUid(), templateData.get().getTemplatePath());
        return new ResponseEntity<>(display, HttpStatus.OK);
    }
}
