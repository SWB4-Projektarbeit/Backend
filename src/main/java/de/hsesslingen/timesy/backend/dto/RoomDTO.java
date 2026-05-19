package de.hsesslingen.timesy.backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private String room_uid;
    private String template_name;
    private String room_name;
    private List<String> required_permissions;
}
