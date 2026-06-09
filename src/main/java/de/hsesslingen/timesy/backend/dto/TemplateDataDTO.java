package de.hsesslingen.timesy.backend.dto;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record TemplateDataDTO(
		@NonNull TemplateDataDTO.RoomDTO room,
		@NonNull String date,
		@NonNull List<SlotDTO> slots) {

	public record RoomDTO(
			@NonNull String name,
			@NonNull String type,
			@Nullable String typeEn,
			@NonNull String lastChanged,
			@NonNull String scheduleUrl) {
	}

	public record SlotDTO(
			@NonNull String timeStart,
			@NonNull String timeEnd,
			@NonNull String title,
			@Nullable String titleEn,
			@NonNull String type,
			@Nullable String movedTo) {
	}
}
