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
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class Mapper {

	private final @NonNull RoomMapper roomMapper;
	private final @NonNull HEOnlineService heOnlineService;
	private final @NonNull DisplayRepository displayRepository;
	private final @NonNull ScheduleEntryMapper scheduleEntryMapper;

	public @Nullable List<BuildingDTO> toBuildingDTOs(final @Nullable List<Appointment> appointments,
													  final @Nullable String building,
													  final @Nullable String floor,
													  final @Nullable Integer roomUid,
													  final @Nullable String roomName,
													  final @Nullable Integer courseUid,
													  final @Nullable String courseName) {
		if (null == appointments) {
			return null;
		}

		@NonNull Stream<Appointment> appointmentStream = appointments.stream();
		if (roomUid != null) {
			appointmentStream = appointmentStream.filter(appointment -> appointment.roomUid() == roomUid);
		}
		if (courseUid != null) {
			appointmentStream = appointmentStream.filter(appointment -> appointment.courseUid() == courseUid);
		}
		if (courseName != null) {
			appointmentStream = appointmentStream.filter(appointment -> {
				final @Nullable Course course = this.heOnlineService.getCourse(appointment);
				if (course == null) {
					return false;
				}

				final @Nullable Map<Locale, String> localizedTitles = course.title().get("value");
				if (localizedTitles == null) {
					return false;
				}

				return localizedTitles.get(Locale.GERMANY).equals(courseName);
			});
		}

		@NonNull Stream<Pair<Appointment, Display>> dataStream = appointmentStream.flatMap(
				appointment -> {
					List<Display> displays = this.displayRepository.findByRoomUid(appointment.roomUid());
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

		final @NonNull Map<String, Map<Integer, RoomDTO>> buildingDTOs = new HashMap<>();
		dataStream.forEach(display -> {
			if (display == null || display.getKey() == null || display.getValue() == null) {
				return;
			}
			final @Nullable RoomDTO room = buildingDTOs
					.computeIfAbsent(display.getValue().getBuildingName(), _ -> new HashMap<>())
					.computeIfAbsent(display.getKey().roomUid(), _ -> this.roomMapper.toRoomDTO(display.getKey(), display.getValue()));
			if (room == null) {
				return;
			}
			final @Nullable ScheduleEntryDTO scheduleEntry = this.scheduleEntryMapper.toScheduleEntryDTO(display.getKey());
			if (scheduleEntry != null) {
				room.schedule().add(scheduleEntry);
			}
		});

		return buildingDTOs
				.entrySet()
				.stream()
				.map(entry -> new BuildingDTO(entry.getKey(), new ArrayList<>(entry.getValue().values())))
				.toList();
	}
}
