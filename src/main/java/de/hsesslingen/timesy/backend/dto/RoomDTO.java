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
public class RoomDTO {
	private int roomUid;
	private String roomName;
	private int templateUid;
	private String templateName;
	private List<ScheduleEntryDTO> schedule;
	private String floor;
	private List<String> requiredPermissions;
}
