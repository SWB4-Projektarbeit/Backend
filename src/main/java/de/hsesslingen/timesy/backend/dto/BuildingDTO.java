package de.hsesslingen.timesy.backend.dto;

import lombok.NonNull;

import java.util.List;

public record BuildingDTO(@NonNull String buildingName,
						  @NonNull List<RoomDTO> rooms) {
}
