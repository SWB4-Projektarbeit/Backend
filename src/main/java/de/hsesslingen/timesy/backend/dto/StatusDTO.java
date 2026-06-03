package de.hsesslingen.timesy.backend.dto;

import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
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
