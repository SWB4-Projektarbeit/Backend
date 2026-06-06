package de.hsesslingen.timesy.backend.dto;

import lombok.NonNull;

import java.util.List;

public record RoomDTO(int roomUid,
                      @NonNull String roomName,
                      int templateUid,
                      @NonNull String templateName,
                      @NonNull List<ScheduleEntryDTO> schedule,
                      @NonNull String floor,
                      @NonNull List<String> requiredPermissions) {
}
