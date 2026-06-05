package de.hsesslingen.timesy.backend.mapper;

import de.hsesslingen.timesy.backend.dto.StatusDTO;
import de.hsesslingen.timesy.backend.model.Appointment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StatusMapper {

    public StatusDTO toStatusDTO(final Appointment appointment, final ScheduleEntryMapper scheduleEntryMapper) {
        if (appointment == null) {
            return null;
        }
        return new StatusDTO(
                StatusDTO.Status.valueOf(appointment.statusTypeKey()),
                appointment.successorUid() == null ? null : scheduleEntryMapper.toScheduleEntryDTO(appointment.successorUid())
        );
    }
}
