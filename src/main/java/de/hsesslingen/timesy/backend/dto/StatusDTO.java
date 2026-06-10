package de.hsesslingen.timesy.backend.dto;

import lombok.NonNull;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

public record StatusDTO(
		@NonNull Status status,
		@Nullable ScheduleEntryDTO successor) {

	@ToString
	public enum Status {
		CONFIRMED,
		RESCHEDULED,
		CANCELLED,
	}
}
