package de.hsesslingen.timesy.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class ScheduleEntryDTO {
    private String name;
    private String startTime;
    private String endTime;
    private StatusDTO status;
}
