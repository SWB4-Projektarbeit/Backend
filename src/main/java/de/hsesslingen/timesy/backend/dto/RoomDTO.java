package de.hsesslingen.timesy.backend.dto;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record RoomDTO(int roomUid,
					  @NonNull String roomName,
					  @NonNull String roomType,
					  @Nullable String roomTypeEn,
					  int templateUid,
					  @NonNull String templateName,
					  @NonNull List<ScheduleEntryDTO> schedule,
					  @NonNull String floor,
					  @NonNull List<String> requiredPermissions) {
}
