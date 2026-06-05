package de.hsesslingen.timesy.backend.mapper;

import de.hsesslingen.timesy.backend.dto.RoomDTO;
import de.hsesslingen.timesy.backend.model.Appointment;
import de.hsesslingen.timesy.backend.model.Display;
import de.hsesslingen.timesy.backend.repository.TemplateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;

@Component
@AllArgsConstructor
public class RoomMapper {

	private final TemplateRepository templateRepository;

	public RoomDTO toRoomDTO(final Appointment appointment, final Display display) {
		if (appointment == null) {
			return null;
		}
		Optional<TemplateRepository.Template> templateData = templateRepository.getByUid(display.getTemplateUid());
		return templateData.map(template -> new RoomDTO(
				appointment.roomUid(),
				display.getRoomName(),
				display.getTemplateUid(),
				template.getTemplateName(),
				new ArrayList<>(),
				display.getFloor(),
				display.getRequiredPermissions()
		)).orElse(null);
	}
}
