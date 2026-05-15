package de.hsesslingen.timesy.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Display {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long uid;

    @Column
    private long display_uid;

    @Column
    private String room_uid;

    @Column
    private String template_uid;

    @Column
    private String room_name;

    @Column
    private List<String> required_permissions;
}
