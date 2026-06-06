package de.hsesslingen.timesy.backend.repository;

import de.hsesslingen.timesy.backend.model.Display;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisplayRepository extends JpaRepository<@NotNull Display, @NotNull Long> {

	@Nullable List<Display> findByRoomUid(final int roomUid);
}
