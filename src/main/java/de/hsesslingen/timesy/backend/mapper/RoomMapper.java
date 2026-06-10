package de.hsesslingen.timesy.backend.mapper;

import de.hsesslingen.timesy.backend.dto.RoomDTO;
import de.hsesslingen.timesy.backend.model.Appointment;
import de.hsesslingen.timesy.backend.model.Display;
import de.hsesslingen.timesy.backend.repository.TemplateRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;

@Component
@AllArgsConstructor
public class RoomMapper {

	private final @NonNull TemplateRepository templateRepository;

	public @Nullable RoomDTO toRoomDTO(final @Nullable Appointment appointment, final @Nullable Display display) {
		if (null == appointment) {
			return null;
		}

		if (null == display) {
			return null;
		}

		final @NonNull Optional<TemplateRepository.Template> templateOptional = this.templateRepository.getByUid(display.getTemplateUid());
		// create a new RoomDTO if the templateOptional is present, else return null
		return templateOptional.map(template -> new RoomDTO(
				appointment.roomUid(),
				display.getRoomName(),
				display.getRoomType(),
				display.getRoomTypeEn(),
				display.getTemplateUid(),
				template.templateName(),
				new ArrayList<>(),
				display.getFloor(),
				display.getRequiredPermissions()
		)).orElse(null);
	}
}
