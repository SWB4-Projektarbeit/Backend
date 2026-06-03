package de.hsesslingen.timesy.backend.mapper;

import de.hsesslingen.timesy.backend.dto.RoomDTO;
import de.hsesslingen.timesy.backend.model.Appointment;
import de.hsesslingen.timesy.backend.model.Display;
import de.hsesslingen.timesy.backend.repository.DisplayRepository;
import de.hsesslingen.timesy.backend.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class RoomMapper {

    @Autowired
    DisplayRepository displayRepository;

    @Autowired
    TemplateRepository templateRepository;

    public RoomDTO toRoomDTO(Appointment appointment, Display display) {
        if (appointment == null) {
            return null;
        }

        //TODO map attributes
        return new RoomDTO(
                appointment.roomUid(),
                display.getRoomName(),
                templateRepository.getTemplateName(display.getTemplateUid()),
                new ArrayList<>(),
                display.getFloor(),
                display.getRequiredPermissions()
        );
    }
}
