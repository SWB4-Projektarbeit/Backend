package de.hsesslingen.timesy.backend.mapper;

import de.hsesslingen.timesy.backend.dto.BuildingDTO;
import de.hsesslingen.timesy.backend.dto.RoomDTO;
import de.hsesslingen.timesy.backend.dto.ScheduleEntryDTO;
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
				if (course == null) {
					return false;
				}

				Map<Locale, String> localizedTitles = course.title().get("value");
				if (localizedTitles == null) {
					return false;
				}

				return localizedTitles.get(Locale.GERMANY).equals(courseName);
			});
		}

		Stream<Pair<Appointment, Display>> dataStream = appointmentStream.flatMap(
				appointment -> {
					List<Display> displays = displayRepository.findByRoomUid(appointment.roomUid());
					if (displays.isEmpty()) {
						return Stream.empty();
					}
					return Stream.of(new Pair<>(appointment, displays.getFirst()));
				}
		);
		if (floor != null) {
			dataStream = dataStream.filter(entry -> {
				if (entry == null || entry.getValue() == null) {
					return false;
				}
				return Objects.equals(entry.getValue().getFloor(), floor);
			});
		}
		if (roomName != null) {
			dataStream = dataStream.filter(entry -> {
				if (entry == null || entry.getValue() == null) {
					return false;
				}
				return Objects.equals(entry.getValue().getRoomName(), roomName);
			});
		}
		if (building != null) {
			dataStream = dataStream.filter(entry -> {
				if (entry == null || entry.getValue() == null) {
					return false;
				}
				return Objects.equals(entry.getValue().getBuildingName(), building);
			});
		}

		Map<String, Map<Integer, RoomDTO>> buildingDTOs = new HashMap<>();
		dataStream.forEach(display -> {
			if (display == null || display.getKey() == null || display.getValue() == null) {
				return;
			}
			RoomDTO room = buildingDTOs
					.computeIfAbsent(display.getValue().getBuildingName(), _ -> new HashMap<>())
					.computeIfAbsent(display.getKey().roomUid(), _ -> roomMapper.toRoomDTO(display.getKey(), display.getValue()));
			if (room == null) {
				return;
			}
			ScheduleEntryDTO scheduleEntry = scheduleEntryMapper.toScheduleEntryDTO(display.getKey());
			if (scheduleEntry != null) {
				room.getSchedule().add(scheduleEntry);
			}
		});

		return buildingDTOs
				.entrySet()
				.stream()
				.map(entry -> new BuildingDTO(entry.getKey(), new ArrayList<>(entry.getValue().values())))
				.toList();
	}
}
