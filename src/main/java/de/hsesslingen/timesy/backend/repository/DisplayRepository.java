package de.hsesslingen.timesy.backend.repository;

import de.hsesslingen.timesy.backend.model.Display;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DisplayRepository extends JpaRepository<@NotNull Display, @NotNull Long> {

    Optional<Display> findByRoomUid(int roomUid);
}
