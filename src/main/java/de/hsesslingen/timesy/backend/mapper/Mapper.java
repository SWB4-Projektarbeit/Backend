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
													  final @Nullable String courseName,
													  final @Nullable String roomType) {
		if (null == appointments) {
			return null;
		}

		@NonNull Stream<Appointment> appointmentStream = appointments.stream();
		if (null != roomUid) {
			appointmentStream = appointmentStream.filter(appointment -> appointment.roomUid() == roomUid);
		}
		if (null != courseUid) {
			appointmentStream = appointmentStream.filter(appointment -> appointment.courseUid() == courseUid);
		}
		if (null != courseName) {
			appointmentStream = appointmentStream.filter(appointment -> {
				final @Nullable Course course = this.heOnlineService.getCourse(appointment);
				if (null == course) {
					return false;
				}

				final @Nullable Map<Locale, String> localizedTitles = course.title().get("value");
				if (null == localizedTitles) {
					return false;
				}

				return localizedTitles.get(Locale.GERMANY).equals(courseName);
			});
		}

		// Pair up an appointment with the appropriate display
		@NonNull Stream<Pair<Appointment, Display>> dataStream = appointmentStream.flatMap(
				appointment -> {
					final @Nullable List<Display> displays = this.displayRepository.findByRoomUid(appointment.roomUid());
					if (null == displays || displays.isEmpty()) {
						return Stream.empty();
					}
					return Stream.of(new Pair<>(appointment, displays.getFirst()));
				}
		);
		if (null != floor) {
			// entry = Pair<Appointment, Display>
			dataStream = dataStream.filter(entry -> {
				if (null == entry || null == entry.getValue()) {
					return false;
				}
				return Objects.equals(entry.getValue().getFloor(), floor);
			});
		}
		if (null != roomName) {
			// entry = Pair<Appointment, Display>
			dataStream = dataStream.filter(entry -> {
				if (null == entry || null == entry.getValue()) {
					return false;
				}
				return Objects.equals(entry.getValue().getRoomName(), roomName);
			});
		}
		if (null != building) {
			// entry = Pair<Appointment, Display>
			dataStream = dataStream.filter(entry -> {
				if (null == entry || null == entry.getValue()) {
					return false;
				}
				return Objects.equals(entry.getValue().getBuildingName(), building);
			});
		}
		if (null != roomType) {
			// entry = Pair<Appointment, Display>
			dataStream = dataStream.filter(entry -> {
				if (null == entry || null == entry.getValue()) {
					return false;
				}
				return Objects.equals(entry.getValue().getRoomType(), roomType);
			});
		}

		// Map<BuildingName, Map<RoomUID, RoomDTO>>
		final @NonNull Map<String, Map<Integer, RoomDTO>> buildingDTOs = new HashMap<>();
		dataStream.forEach(entry -> {
			if (null == entry || null == entry.getKey() || null == entry.getValue()) {
				return;
			}
			final @Nullable RoomDTO room = buildingDTOs
					// if we don't have the building in the map already, add it, else use the value that already exists
					.computeIfAbsent(entry.getValue().getBuildingName(), _ -> new HashMap<>())
					// if we don't have the room in the map already, add it
					.computeIfAbsent(entry.getKey().roomUid(), _ -> this.roomMapper.toRoomDTO(entry.getKey(), entry.getValue()));
			if (null == room) {
				return;
			}
			final @Nullable ScheduleEntryDTO scheduleEntry = this.scheduleEntryMapper.toScheduleEntryDTO(entry.getKey());
			if (null != scheduleEntry) {
				room.schedule().add(scheduleEntry);
			}
		});

		return buildingDTOs
				.entrySet()
				.stream()
				// entry = Map<BuildingName, Map<RoomUID, RoomDTO>>
				.map(entry -> new BuildingDTO(entry.getKey(), new ArrayList<>(entry.getValue().values())))
				.toList();
	}
}
