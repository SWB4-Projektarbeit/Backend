package de.hsesslinge.timesy.backend.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long uid;

    @NotNull
    @Column(name = "data")
    private String template_data;

    public Template() {}

    public Template(final long uid, final @NotNull String template_data) {
        this.uid = uid;
        this.template_data = template_data;
    }
}
