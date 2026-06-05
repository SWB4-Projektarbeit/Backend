package de.hsesslingen.timesy.backend.service;

import de.hsesslingen.timesy.backend.model.Appointment;
import de.hsesslingen.timesy.backend.model.Course;
import de.hsesslingen.timesy.backend.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;


@Slf4j
@Service
public class HEOnlineService {

	public static final String APPOINTMENTS_ENDPOINT = "he/co/co-tm-core/course/api/appointments";
	public static final String COURSE_ENDPOINT = "he/co/co-tm-core/course/api/courses/{id}";
	public static final String COURSES_ENDPOINT = "he/co/co-tm-core/course/api/courses?limit=10000";

	private static final ParameterizedTypeReference<List<Appointment>> APPOINTMENT_TYPE = new ParameterizedTypeReference<>() {
	};
	private static final ParameterizedTypeReference<List<Course>> COURSE_TYPE = new ParameterizedTypeReference<>() {
	};

	private final RestClient restClient;
	private final String heOnlineUrl;

	public HEOnlineService(@Value("${heonline.url}") final String heOnlineUrl) {
		Utils.validateUrl(heOnlineUrl);
		this.heOnlineUrl = heOnlineUrl;
		restClient = RestClient.builder()
				.build();
		// TODO: Cookies for KeyCloak instance!
	}

	public @Nullable Appointment getAppointment(final int appointmentId) {
		List<Appointment> appointments = getAppointments();
		if (appointments == null) {
			return null;
		}
		try {
			return appointments.stream().filter(appointment -> appointment.uid() == appointmentId).findFirst().orElse(null);
		} catch (Exception e) {
			return null;
		}
	}

	public @Nullable List<Appointment> getAppointments() {
		RestClient.ResponseSpec response = restClient.get()
				.uri(this.heOnlineUrl + "/" + APPOINTMENTS_ENDPOINT)
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(StandardCharsets.UTF_8)
				.retrieve();

		ResponseEntity<@NotNull List<Appointment>> responseEntity;
		try {
			responseEntity = response.toEntity(APPOINTMENT_TYPE);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return null;
		}

		if (responseEntity.getStatusCode().value() != 200) {
			return null;
		}

		try {
			return responseEntity.getBody();
		} catch (Exception e) {
			return null;
		}
	}

	public @Nullable Course getCourse(final Appointment appointment) {
		RestClient.ResponseSpec response = restClient.get()
				.uri(this.heOnlineUrl + "/" + COURSE_ENDPOINT, appointment.courseUid())
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(StandardCharsets.UTF_8)
				.retrieve();

		ResponseEntity<@NotNull Course> responseEntity;
		try {
			responseEntity = response.toEntity(Course.class);
		} catch (Exception e) {
			return null;
		}

		if (responseEntity.getStatusCode().value() != 200) {
			return null;
		}

		try {
			return responseEntity.getBody();
		} catch (Exception e) {
			return null;
		}
	}

	public @Nullable List<Course> getCourses() {
		RestClient.ResponseSpec response = restClient.get()
				.uri(this.heOnlineUrl + "/" + COURSES_ENDPOINT)
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(StandardCharsets.UTF_8)
				.retrieve();

		ResponseEntity<@NotNull List<Course>> responseEntity;
		try {
			responseEntity = response.toEntity(COURSE_TYPE);
		} catch (Exception e) {
			return null;
		}

		if (responseEntity.getStatusCode().value() != 200) {
			return null;
		}

		try {
			return responseEntity.getBody();
		} catch (Exception e) {
			return null;
		}
	}
}
