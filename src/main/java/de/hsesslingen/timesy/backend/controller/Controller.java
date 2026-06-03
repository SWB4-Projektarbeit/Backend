package de.hsesslingen.timesy.backend.controller;

import de.hsesslingen.timesy.backend.dto.BuildingDTO;
import de.hsesslingen.timesy.backend.mapper.Mapper;
import de.hsesslingen.timesy.backend.model.Display;
import de.hsesslingen.timesy.backend.repository.DisplayRepository;
import de.hsesslingen.timesy.backend.service.HEOnlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api-timesy")
public class Controller {

    @Autowired
    private Mapper mapper;

    @Autowired
    private HEOnlineService  heOnlineService;

    @GetMapping("/rooms")
    public ResponseEntity getAllRooms(@RequestParam(required = false) String building,
                                                         @RequestParam(required = false) String floor,
                                                         @RequestParam(required = false) Integer room_uid,
                                                         @RequestParam(required = false) String room_name,
                                                         @RequestParam(required = false) Integer course_uid,
                                                         @RequestParam(required = false) String course_name) {
        if (room_uid != null && room_name != null) {
            return new ResponseEntity<>("ROOM_UID and ROOM_NAME are mutually exclusive", HttpStatus.BAD_REQUEST);
        }

        if (course_uid != null && course_name != null) {
            return new ResponseEntity<>("COURSE_UID and COURSE_NAME are mutually exclusive", HttpStatus.BAD_REQUEST);
        }

        try {
            return new ResponseEntity<>(mapper.toBuildingDTOs(heOnlineService.getAppointments(), building, floor, room_uid, room_name, course_uid, course_name), HttpStatus.OK);
        }  catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
