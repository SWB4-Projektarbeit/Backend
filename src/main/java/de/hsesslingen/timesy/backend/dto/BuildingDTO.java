package de.hsesslingen.timesy.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class BuildingDTO {
    private String buildingName;
    private List<RoomDTO> rooms;
}
