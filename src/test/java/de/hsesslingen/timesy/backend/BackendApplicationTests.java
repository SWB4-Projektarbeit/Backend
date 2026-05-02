package de.hsesslingen.timesy.backend;

import de.hsesslingen.timesy.backend.model.Appointment;
import de.hsesslingen.timesy.backend.model.Course;
import lombok.AllArgsConstructor;
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
            System.out.println("API returned non 200 status code");
            return;
        }
        appointments.forEach(System.out::println);

        for (Appointment appointment : appointments) {
            Course course = service.getCourse(appointment);
            System.out.println(appointment + " -> " + course);
        }
	}
}
