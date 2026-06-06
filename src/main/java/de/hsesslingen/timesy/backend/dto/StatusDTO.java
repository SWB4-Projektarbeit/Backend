package de.hsesslingen.timesy.backend.dto;

import lombok.*;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
public class StatusDTO {
	private final @NonNull Status status;
	private @Nullable ScheduleEntryDTO successor = null;

	@ToString
	public enum Status {
		CONFIRMED,
		RESCHEDULED,
		CANCELLED,
	}
}
