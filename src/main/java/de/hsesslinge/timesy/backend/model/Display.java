package de.hsesslinge.timesy.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Display {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long uid;

    @Column(name = "template")
    private long template_uid;

    @Column(name = "display_uid")
    private long display_uid;

    public Display() {}

    public Display(final long uid, final long template_uid, final long display_uid) {
        this.uid = uid;
        this.template_uid = template_uid;
        this.display_uid = display_uid;
    }
}
