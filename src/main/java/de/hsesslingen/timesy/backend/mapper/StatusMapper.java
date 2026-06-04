package de.hsesslingen.timesy.backend.mapper;

import de.hsesslingen.timesy.backend.dto.StatusDTO;
import de.hsesslingen.timesy.backend.model.Appointment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StatusMapper {

    private final ScheduleEntryMapper scheduleEntryMapper;

    public StatusDTO toStatusDTO(final Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        return new StatusDTO(
                StatusDTO.Status.valueOf(appointment.statusTypeKey()),
                scheduleEntryMapper.toScheduleEntryDTO(appointment.successorUid())
        );
    }
}
