package de.hsesslingen.timesy.backend.dto;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public record ScheduleEntryDTO(@NonNull String name,
							   @Nullable String nameEn,
							   @NonNull String startTime,
							   @NonNull String endTime,
							   int roomUid,
							   @NonNull StatusDTO status) {
}
