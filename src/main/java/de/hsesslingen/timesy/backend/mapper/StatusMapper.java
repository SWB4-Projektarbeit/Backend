package de.hsesslingen.timesy.backend.mapper;

import de.hsesslingen.timesy.backend.dto.StatusDTO;
import de.hsesslingen.timesy.backend.model.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatusMapper {

    @Autowired
    ScheduleEntryMapper scheduleEntryMapper;

    public StatusDTO toStatusDTO(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        return new StatusDTO(
                StatusDTO.Status.valueOf(appointment.statusTypeKey()),
                scheduleEntryMapper.toScheduleEntryDTO(appointment.successorUid())
        );
    }
}
