package de.hsesslingen.timesy.backend.mapper;

import de.hsesslingen.timesy.backend.dto.BuildingDTO;
import de.hsesslingen.timesy.backend.dto.RoomDTO;
import de.hsesslingen.timesy.backend.model.Appointment;
import de.hsesslingen.timesy.backend.model.Course;
import de.hsesslingen.timesy.backend.model.Display;
import de.hsesslingen.timesy.backend.repository.DisplayRepository;
import de.hsesslingen.timesy.backend.service.HEOnlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Mapper {

    @Autowired
    RoomMapper roomMapper;

    @Autowired
    ScheduleEntryMapper scheduleEntryMapper;

    @Autowired
    DisplayRepository displayRepository;

    @Autowired
    HEOnlineService heOnlineService;

    public List<BuildingDTO> toBuildingDTOs(List<Appointment> appointments,
                                            String building,
                                            String floor,
                                            Integer roomUid,
                                            String roomName,
                                            Integer courseUid,
                                            String courseName) {
        if (appointments == null) {
            return null;
        }

        Map<String, Map<Integer, RoomDTO>> buildingDTOs = new HashMap<>();
        appointments.forEach(appointment -> {
            if (appointment == null) {
                return;
            }

            // if filtered, ignore
            if (roomUid != null && appointment.roomUid() != roomUid) {
                return;
            }

            Display display = displayRepository.findByRoomUid(appointment.roomUid());

            if (floor != null && !display.getFloor().equals(floor)) {
                return;
            }

            if (roomName != null && !display.getRoomName().equals(roomName)) {
                return;
            }

            if (courseUid != null && appointment.courseUid() != courseUid) {
                return;
            }

            if (courseName != null) {
                Course course = heOnlineService.getCourse(appointment);
                if (course != null) {
                    Map<Locale,String> localizedTitles = course.title().get("value");
                    if (localizedTitles != null && !localizedTitles.get(Locale.GERMANY).equals(courseName)) {
                        return;
                    }
                }
            }

            String buildingName = display.getBuildingName();
            if (building != null && !building.equals(buildingName)) {
                return;
            }

            if (!buildingDTOs.containsKey(buildingName)) {
                buildingDTOs.put(buildingName, new HashMap<>());
            }

            Map<Integer, RoomDTO> roomDTOs = buildingDTOs.get(buildingName);
            if (!roomDTOs.containsKey(appointment.roomUid())) {
                roomDTOs.put(appointment.roomUid(), roomMapper.toRoomDTO(appointment, display));
            }

            roomDTOs.get(appointment.roomUid()).getSchedule().add(scheduleEntryMapper.toScheduleEntryDTO(appointment));
        });

        return buildingDTOs.values().stream().map(
                b -> new BuildingDTO(b.values().stream().toList())
        ).toList();
    }
}
