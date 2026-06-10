package de.hsesslingen.timesy.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Display {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private @Nullable Long uid;

	@Column
	private long displayUid;

	@Column
	private int roomUid;

	@Column
	private int templateUid;

	@Column
	private @NonNull String roomName;

	@Column
	private @NonNull String roomType;

	@Column
	private @NonNull String roomTypeEn;

	@Column
	private @NonNull String buildingName;

	@Column
	private @NonNull String floor;

	@Column
	private @NonNull List<String> requiredPermissions;
}
