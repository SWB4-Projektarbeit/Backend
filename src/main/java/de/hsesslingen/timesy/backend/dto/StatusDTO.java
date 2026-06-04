package de.hsesslingen.timesy.backend.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
public class StatusDTO {
    private final Status status;
    private ScheduleEntryDTO successorUid = null;

    public enum Status {
        CONFIRMED,
        RESCHEDULED,
        CANCELLED,
    }
}
