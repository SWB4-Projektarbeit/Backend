package de.hsesslingen.timesy.backend.controller;

import de.hsesslingen.timesy.backend.dto.RoomDTO;
import de.hsesslingen.timesy.backend.mapper.Mapper;
import de.hsesslingen.timesy.backend.model.Display;
import de.hsesslingen.timesy.backend.repository.DisplayRepository;
import de.hsesslingen.timesy.backend.repository.TemplateRepository;
import de.hsesslingen.timesy.backend.service.DisplayService;
import de.hsesslingen.timesy.backend.service.HEOnlineService;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api-timesy")
@AllArgsConstructor
public class Controller {

    private final Mapper mapper;

    private final HEOnlineService  heOnlineService;
    private final DisplayRepository displayRepository;
    private final DisplayService displayService;
    private final TemplateRepository templateRepository;

    @GetMapping("/rooms")
    public ResponseEntity getAllRooms(final @RequestParam(required = false) String building,
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

    @PatchMapping("/rooms/{room_uid}")
    public ResponseEntity<@NotNull Display> updateRoom(@PathVariable("room_uid") final int room_uid, @RequestBody final String templateUid) {
        Optional<Display> displayData = displayRepository.findByRoomUid(room_uid);
        if (displayData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Display display = displayData.get();

        display.setTemplateUid(templateUid);
        return new ResponseEntity<>(displayRepository.save(display), HttpStatus.OK);
    }

    @GetMapping("/update")
    public ResponseEntity<@NotNull Display> updateRoom(final @RequestParam(required = true) Integer room_uid) {
        Optional<Display> displayData = displayRepository.findByRoomUid(room_uid);
        if (displayData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Display display = displayData.get();
        displayService.sendImage(display.getDisplayUid(), templateRepository.getByUid(display.getTemplateUid()).getTemplatePath());
        return new ResponseEntity<>(display, HttpStatus.OK);
    }
}
