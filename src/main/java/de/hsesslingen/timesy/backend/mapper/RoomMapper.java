package de.hsesslingen.timesy.backend.mapper;

import de.hsesslingen.timesy.backend.dto.RoomDTO;
import de.hsesslingen.timesy.backend.model.Appointment;
import de.hsesslingen.timesy.backend.model.Display;
import de.hsesslingen.timesy.backend.repository.TemplateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@AllArgsConstructor
public class RoomMapper {

    private final TemplateRepository templateRepository;

    public RoomDTO toRoomDTO(final Appointment appointment, final Display display) {
        if (appointment == null) {
            return null;
        }
        return new RoomDTO(
                appointment.roomUid(),
                display.getRoomName(),
                display.getTemplateUid(),
                templateRepository.getByUid(display.getTemplateUid()).getTemplateName(),
                new ArrayList<>(),
                display.getFloor(),
                display.getRequiredPermissions()
        );
    }
}
