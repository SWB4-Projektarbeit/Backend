package de.hsesslingen.timesy.backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class RoomDTO {
    private int room_uid;
    private String room_name;
    private int template_uid;
    private String template_name;
    private List<ScheduleEntryDTO> schedule;
    private String floor;
    private List<String> required_permissions;
}
