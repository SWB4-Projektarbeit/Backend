package de.hsesslingen.timesy.backend.dto;

import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
