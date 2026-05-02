package de.hsesslingen.timesy.backend;

import de.hsesslingen.timesy.backend.model.Appointment;
import de.hsesslingen.timesy.backend.model.Course;
import de.hsesslingen.timesy.backend.service.HEOnlineService;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@AllArgsConstructor
class BackendApplicationTests {

	@Test
	void contextLoads(@Autowired HEOnlineService service) {
        List<Appointment> appointments = service.getAppointments();
        if (appointments == null) {
            Assertions.fail("No appointments found");
        }

        for (Appointment appointment : appointments) {
            Course course = service.getCourse(appointment);
            Assertions.assertNotNull(course);
        }

        service.getCourses();
	}
}
