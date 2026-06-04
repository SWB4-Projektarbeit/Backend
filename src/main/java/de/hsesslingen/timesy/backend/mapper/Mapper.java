package de.hsesslingen.timesy.backend.mapper;

import de.hsesslingen.timesy.backend.dto.BuildingDTO;
import de.hsesslingen.timesy.backend.dto.RoomDTO;
import de.hsesslingen.timesy.backend.model.Appointment;
import de.hsesslingen.timesy.backend.model.Course;
import de.hsesslingen.timesy.backend.model.Display;
import de.hsesslingen.timesy.backend.repository.DisplayRepository;
import de.hsesslingen.timesy.backend.service.HEOnlineService;
import de.zeanon.storagemanagercore.internal.utility.basic.Pair;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class Mapper {

    private final RoomMapper roomMapper;

    private final ScheduleEntryMapper scheduleEntryMapper;

    private final DisplayRepository displayRepository;

    private final HEOnlineService heOnlineService;

    public List<BuildingDTO> toBuildingDTOs(final List<Appointment> appointments,
                                            final String building,
                                            final String floor,
                                            final Integer roomUid,
                                            final String roomName,
                                            final Integer courseUid,
                                            final String courseName) {
        if (appointments == null) {
            return null;
        }

        Stream<Appointment> appointmentStream = appointments.stream();
        if (roomUid != null) {
            appointmentStream = appointmentStream.filter(appointment -> appointment.roomUid() == roomUid);
        }
        if (courseUid != null) {
            appointmentStream = appointmentStream.filter(appointment -> appointment.courseUid() == courseUid);
        }
        if (courseName != null) {
            appointmentStream = appointmentStream.filter(appointment -> {
                Course course = heOnlineService.getCourse(appointment);
                if (course == null) return false;
                Map<Locale, String> localizedTitles = course.title().get("value");
                if (localizedTitles == null) return false;
                return localizedTitles.get(Locale.GERMANY).equals(courseName);
            });
        }

        Stream<Pair<Appointment, Display>> dataStream = appointmentStream.map(appointment -> new Pair<>(appointment, displayRepository.findByRoomUid(appointment.roomUid())));
        if (floor != null) {
            dataStream = dataStream.filter(display -> Objects.equals(display.getValue().getFloor(), floor));
        }
        if (roomName != null) {
            dataStream = dataStream.filter(display -> Objects.equals(display.getValue().getRoomName(), roomName));
        }
        if (building != null) {
            dataStream = dataStream.filter(display -> Objects.equals(display.getValue().getBuildingName(), building));
        }

        Map<String, Map<Integer, RoomDTO>> buildingDTOs = new HashMap<>();
        dataStream.forEach(display -> {
            buildingDTOs.computeIfAbsent(display.getValue().getBuildingName(), __ -> {
                        return new HashMap<>();
                    })
                    .computeIfAbsent(display.getKey().roomUid(), __ -> {
                        return roomMapper.toRoomDTO(display.getKey(), display.getValue());
                    })
                    .getSchedule()
                    .add(scheduleEntryMapper.toScheduleEntryDTO(display.getKey()));
        });

        return buildingDTOs.entrySet()
                .stream()
                .map(entry -> new BuildingDTO(entry.getKey(), new ArrayList<>(entry.getValue().values())))
                .toList();
    }
}
