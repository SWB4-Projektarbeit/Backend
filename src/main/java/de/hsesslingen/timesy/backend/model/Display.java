package de.hsesslingen.timesy.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Display {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long uid;

    @Column
    private long displayUid;

    @Column
    private int roomUid;

    @Column
    private int templateUid;

    @Column
    private String roomName;

    @Column
    private String buildingName;

    @Column
    private String floor;

    @Column
    private List<String> requiredPermissions;
}
