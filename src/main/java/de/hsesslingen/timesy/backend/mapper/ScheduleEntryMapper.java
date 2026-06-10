package de.hsesslingen.timesy.backend.mapper;

import de.hsesslingen.timesy.backend.dto.ScheduleEntryDTO;
import de.hsesslingen.timesy.backend.dto.StatusDTO;
import de.hsesslingen.timesy.backend.model.Appointment;
import de.hsesslingen.timesy.backend.model.Course;
import de.hsesslingen.timesy.backend.service.HEOnlineService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
@AllArgsConstructor
public class ScheduleEntryMapper {

	private final @NonNull StatusMapper statusMapper;
	private final @NonNull HEOnlineService heOnlineService;

	public @Nullable ScheduleEntryDTO toScheduleEntryDTO(final @Nullable Appointment appointment) {
		if (null == appointment) {
			return null;
		}

		final @Nullable Map<Locale, String> appointmentNames = getAppointmentTitle(appointment);
		if (null == appointmentNames) {
			return null;
		}

		final @Nullable StatusDTO status = this.statusMapper.toStatusDTO(appointment, this);
		if (null == status) {
			return null;
		}

		return new ScheduleEntryDTO(
				appointmentNames.get(Locale.GERMAN),
				appointmentNames.get(Locale.ENGLISH),
				appointment.startAt(),
				appointment.endAt(),
				appointment.roomUid(),
				status
		);
	}

	public @Nullable ScheduleEntryDTO toScheduleEntryDTO(final int appointmentId) {
		return toScheduleEntryDTO(this.heOnlineService.getAppointment(appointmentId));
	}

	private @Nullable Map<Locale, String> getAppointmentTitle(final @Nullable Appointment appointment) {
		if (null == appointment) {
			return null;
		}

		final @Nullable Course course = this.heOnlineService.getCourse(appointment);
		if (null == course) {
			return null;
		}

		return course.title().get("value");
	}
}
