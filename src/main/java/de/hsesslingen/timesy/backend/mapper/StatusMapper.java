package de.hsesslingen.timesy.backend.mapper;

import de.hsesslingen.timesy.backend.dto.StatusDTO;
import de.hsesslingen.timesy.backend.model.Appointment;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StatusMapper {

	public @Nullable StatusDTO toStatusDTO(final @Nullable Appointment appointment, final @NonNull ScheduleEntryMapper scheduleEntryMapper) {
		if (null == appointment) {
			return null;
		}
		try {
			return new StatusDTO(
					StatusDTO.Status.valueOf(appointment.statusTypeKey()),
					null == appointment.successorUid() ? null : scheduleEntryMapper.toScheduleEntryDTO(appointment.successorUid())
			);
		} catch (final Exception _) {
			return null;
		}
	}
}
