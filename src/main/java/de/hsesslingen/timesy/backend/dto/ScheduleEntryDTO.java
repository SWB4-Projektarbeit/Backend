package de.hsesslingen.timesy.backend.dto;

import lombok.NonNull;

public record ScheduleEntryDTO(@NonNull String name,
                               @NonNull String startTime,
                               @NonNull String endTime,
                               @NonNull StatusDTO status) {
}
