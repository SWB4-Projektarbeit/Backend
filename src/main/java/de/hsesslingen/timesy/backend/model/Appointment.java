package de.hsesslingen.timesy.backend.model;

import lombok.Data;

@Data
public class Appointment {
    private final int uid;
    private final String application_type_key;
    private final int course_group_uid;
    private final int course_uid;
    private final String start_at;
    private final String end_at;
    private final String event_type_key;
    private final int resource_uid;
    private final int external_object_uid;
    private final int room_uid;
    private final String resource_url;
    private final String status_type_key;
}
