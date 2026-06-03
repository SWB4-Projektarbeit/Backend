package de.hsesslingen.timesy.backend.repository;

import de.hsesslingen.timesy.backend.model.Display;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisplayRepository extends JpaRepository<Display, Long> {

    public Display findByRoomUid(int roomUid);
}
