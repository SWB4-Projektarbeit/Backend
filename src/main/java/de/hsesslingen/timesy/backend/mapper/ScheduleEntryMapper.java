package de.hsesslingen.timesy.backend.mapper;

import de.hsesslingen.timesy.backend.dto.ScheduleEntryDTO;
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

    private final HEOnlineService heOnlineService;

    private final StatusMapper statusMapper;

    public ScheduleEntryDTO toScheduleEntryDTO(final Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        return new ScheduleEntryDTO(
                getAppointmentName(appointment),
                appointment.startAt(),
                appointment.endAt(),
                statusMapper.toStatusDTO(appointment, this)
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
