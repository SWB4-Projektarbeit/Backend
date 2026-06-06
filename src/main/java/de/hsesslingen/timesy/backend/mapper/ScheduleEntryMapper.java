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

		final @Nullable String appointmentName = getAppointmentName(appointment);
		if (null == appointmentName) {
			return null;
		}

		final @Nullable StatusDTO status = this.statusMapper.toStatusDTO(appointment, this);
		if (null == status) {
			return null;
		}

		return new ScheduleEntryDTO(
				appointmentName,
				appointment.startAt(),
				appointment.endAt(),
				status
		);
	}

	public @Nullable ScheduleEntryDTO toScheduleEntryDTO(final int appointmentId) {
		return toScheduleEntryDTO(this.heOnlineService.getAppointment(appointmentId));
	}

	private @Nullable String getAppointmentName(final @Nullable Appointment appointment) {
		if (null == appointment) {
			return null;
		}

		final @Nullable Course course = this.heOnlineService.getCourse(appointment);
		if (null == course) {
			return null;
		}

		final @Nullable Map<Locale, String> localizedTitles = course.title().get("value");
		if (null == localizedTitles) {
			return null;
		}

		return localizedTitles.get(Locale.GERMANY);
	}
}
