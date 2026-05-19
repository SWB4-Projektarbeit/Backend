package de.hsesslingen.timesy.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class StatusDTO {
    private String status;
    private ScheduleEntryDTO successor = null;
}
