package de.hsesslingen.timesy.backend.model;

import lombok.Data;

@Data
public class Appointment {
    private final int uid;
    private final String applicationTypeKey;
    private final int courseGroupUid;
    private final int courseUid;
    private final String startAt;
    private final String endAt;
    private final String eventTypeKey;
    private final int resourceUid;
    private final int externalObjectUid;
    private final int roomUid;
    private final String resourceUrl;
    private final String statusTypeKey;
}
