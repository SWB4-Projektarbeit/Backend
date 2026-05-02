package de.hsesslingen.timesy.backend.model;

/**
 * JSON DTO from HE Online API
 */
public record Appointment(int uid, String applicationTypeKey, int courseGroupUid, int courseUid, String startAt,
                          String endAt, String eventTypeKey, int resourceUid, int externalObjectUid, int roomUid,
                          String resourceUrl, String statusTypeKey) {
}
