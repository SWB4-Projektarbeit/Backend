package de.hsesslingen.timesy.backend.mapper;

import de.hsesslingen.timesy.backend.dto.*;
import de.hsesslingen.timesy.backend.model.Appointment;
import de.hsesslingen.timesy.backend.service.HEOnlineService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@Slf4j
@Component
@AllArgsConstructor
public class TemplateDataMapper {

	private final Mapper mapper;
	private final @NonNull HEOnlineService heOnlineService;

	public @Nullable TemplateDataDTO getTemplateDataDTO(final int roomUid) {
		final @Nullable List<Appointment> appointments = this.heOnlineService.getAppointments();
		final @Nullable List<BuildingDTO> buildingDTOS = mapper.toBuildingDTOs(
				appointments,
				null,
				null,
				roomUid,
				null,
				null,
				null,
				null
		);

		if (null == buildingDTOS || buildingDTOS.isEmpty()) {
			return null;
		}

		final @NonNull List<RoomDTO> rooms = buildingDTOS.getFirst().rooms();

		if (rooms.isEmpty()) {
			return null;
		}

		final @NonNull RoomDTO room = rooms.getFirst();

		final @NonNull List<TemplateDataDTO.SlotDTO> slots = new ArrayList<>();

		final @NonNull Date now = new Date();
		AtomicReference<@NonNull Date> lastEndTime = new AtomicReference<>(now);
		final @NonNull DateFormat inputTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		final @NonNull DateFormat slotTimeFormatter = new SimpleDateFormat("HH:mm");
		final @NonNull List<ScheduleEntryDTO> scheduleEntries = room.schedule();

		scheduleEntries.forEach(scheduleEntry -> {
			final @NonNull String type;
			@Nullable String movedTo = null;
			final @NonNull Date startTime;
			try {
				startTime = inputTimeFormatter.parse(scheduleEntry.startTime().replace("t", " "));
			} catch (ParseException e) {
				log.warn("Error while parsing startTime for {}: {}", scheduleEntry.name(), e.getMessage());
				return;
			}

			if (!slots.isEmpty()) {
				long breakDuration = TimeUnit.MILLISECONDS.toMinutes(startTime.getTime() - lastEndTime.get().getTime());
				if (breakDuration > 15) {
					slots.add(new TemplateDataDTO.SlotDTO(
							slotTimeFormatter.format(lastEndTime.get()),
							slotTimeFormatter.format(startTime),
							"FREI - " + breakDuration + " Min",
							"FREE - " + breakDuration + " Min",
							"free",
							null
					));
				}
			}

			try {
				lastEndTime.set(inputTimeFormatter.parse(scheduleEntry.endTime().replace("t", " ")));
			} catch (ParseException e) {
				log.warn("Error while parsing endTime for {}: {}", scheduleEntry.name(), e.getMessage());
				return;
			}

			if (StatusDTO.Status.CANCELLED == scheduleEntry.status().status()) {
				type = "cancelled";
			} else if (startTime.before(now) && lastEndTime.get().after(now)) {
				type = "active";
			} else if (StatusDTO.Status.RESCHEDULED == scheduleEntry.status().status()) {
				if (null != scheduleEntry.status().successor()) {
					List<BuildingDTO> buildings = mapper.toBuildingDTOs(
							appointments,
							null,
							null,
							null,
							null,
							null,
							null,
							null
					);
					if (null != buildings) {
						movedTo = buildings
								.stream()
								.flatMap(building -> building
										.rooms()
										.stream()
										.flatMap(r -> {
											if (r.roomUid() != scheduleEntry.status().successor().roomUid()) {
												return Stream.empty();
											}
											return Stream.of(r.roomName());
										})).findFirst().orElse(null);
					}
				}
				type = "rescheduled";
			} else if (StatusDTO.Status.CONFIRMED == scheduleEntry.status().status()) {
				type = "booked";
			} else {
				type = "unknown";
			}

			slots.add(new TemplateDataDTO.SlotDTO(
					slotTimeFormatter.format(startTime),
					slotTimeFormatter.format(lastEndTime.get()),
					scheduleEntry.name(),
					scheduleEntry.nameEn(),
					type,
					movedTo
			));
		});

		return new TemplateDataDTO(
				new TemplateDataDTO.RoomDTO(
						room.roomName(),
						room.roomType(),
						room.roomTypeEn(),
						slotTimeFormatter.format(now),
						"https://www.youtube.com/watch?v=LDU_Txk06tM"), //TODO Placeholder until we know where to get the URL from
				new SimpleDateFormat("dd.MM.yyyy").format(now),
				slots
		);
	}
}
