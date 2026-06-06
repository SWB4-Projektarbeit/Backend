package de.hsesslingen.timesy.backend.mapper;

import de.hsesslingen.timesy.backend.dto.ScheduleEntryDTO;
import de.hsesslingen.timesy.backend.dto.StatusDTO;
import de.hsesslingen.timesy.backend.model.Appointment;
import de.hsesslingen.timesy.backend.model.Course;
import de.hsesslingen.timesy.backend.service.HEOnlineService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
@AllArgsConstructor
public class ScheduleEntryMapper {

	private final StatusMapper statusMapper;
	private final HEOnlineService heOnlineService;

	public ScheduleEntryDTO toScheduleEntryDTO(final Appointment appointment) {
		if (appointment == null) {
			return null;
		}

		String appointmentName = getAppointmentName(appointment);
		if (appointmentName == null) {
			return null;
		}

		StatusDTO status = statusMapper.toStatusDTO(appointment, this);
		if (status == null) {
			return null;
		}

		return new ScheduleEntryDTO(
				appointmentName,
				appointment.startAt(),
				appointment.endAt(),
				status
		);
	}

	public ScheduleEntryDTO toScheduleEntryDTO(final int appointmentId) {
		return toScheduleEntryDTO(heOnlineService.getAppointment(appointmentId));
	}

	private String getAppointmentName(final Appointment appointment) {
		if (appointment == null) {
			return null;
		}

		Course course = heOnlineService.getCourse(appointment);
		if (course == null) {
			return null;
		}

		Map<Locale, String> localizedTitles = course.title().get("value");
		if (localizedTitles == null) {
			return null;
		}

		return localizedTitles.get(Locale.GERMANY);
	}
}
