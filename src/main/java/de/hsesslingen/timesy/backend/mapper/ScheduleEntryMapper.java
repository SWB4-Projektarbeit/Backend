package de.hsesslingen.timesy.backend.mapper;

import de.hsesslingen.timesy.backend.dto.ScheduleEntryDTO;
import de.hsesslingen.timesy.backend.model.Appointment;
import de.hsesslingen.timesy.backend.model.Course;
import de.hsesslingen.timesy.backend.service.HEOnlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

@Component
public class ScheduleEntryMapper {

    @Autowired
    HEOnlineService heOnlineService;

    @Autowired
    StatusMapper statusMapper;

    public ScheduleEntryDTO toScheduleEntryDTO(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        return new ScheduleEntryDTO(
                getAppName(appointment),
                appointment.startAt(),
                appointment.endAt(),
                statusMapper.toStatusDTO(appointment)
        );
    }

    public ScheduleEntryDTO toScheduleEntryDTO(int appointmentId) {
        return toScheduleEntryDTO(heOnlineService.getAppointment(appointmentId));
    }

    private String getAppName(Appointment appointment) {
        Course course = heOnlineService.getCourse(appointment);
        if (course == null) {
            return null;
        }
        Map<Locale,String> localizedTitles = course.title().get("value");
        if (localizedTitles == null) {
            return null;
        }
        return localizedTitles.get(Locale.GERMANY);
    }
}
