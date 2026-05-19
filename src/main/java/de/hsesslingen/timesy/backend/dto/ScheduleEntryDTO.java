package de.hsesslingen.timesy.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEntryDTO {
    private String name;
    private String startTime;
    private String endTime;
    private StatusDTO status;
}
