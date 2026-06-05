package de.hsesslingen.timesy.backend.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
public class StatusDTO {
    private Status status;
    private ScheduleEntryDTO successor = null;

    public enum Status {
        CONFIRMED,
        RESCHEDULED,
        CANCELLED,
    }
}
