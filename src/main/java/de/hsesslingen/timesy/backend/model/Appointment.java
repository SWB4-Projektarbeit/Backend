package de.hsesslingen.timesy.backend.model;

/**
 * JSON DTO from HE Online API
 */
public record Appointment(int uid, String applicationTypeKey,
                          int courseGroupUid, int courseUid, String endAt,
                          String eventTypeKey, int externalObjectUid,
                          int resourceUid, String resourceUrl,
                          int roomUid, String startAt, String statusTypeKey,
                          Integer successorUid) {
}
