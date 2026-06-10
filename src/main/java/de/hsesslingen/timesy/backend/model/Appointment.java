package de.hsesslingen.timesy.backend.model;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * JSON DTO from HE Online API
 */
public record Appointment(int uid,
						  @NonNull String applicationTypeKey,
						  int courseGroupUid,
						  int courseUid,
						  @NonNull String endAt,
						  @NonNull String eventTypeKey,
						  int externalObjectUid,
						  int resourceUid,
						  @NonNull String resourceUrl,
						  int roomUid,
						  @NonNull String startAt,
						  @NonNull String statusTypeKey,
						  @Nullable Integer successorUid) {
}
