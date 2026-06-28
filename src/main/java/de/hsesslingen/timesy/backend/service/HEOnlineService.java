package de.hsesslingen.timesy.backend.service;

import de.hsesslingen.timesy.backend.model.Appointment;
import de.hsesslingen.timesy.backend.model.Course;
import de.hsesslingen.timesy.backend.security.KeycloakClient;
import de.hsesslingen.timesy.backend.utils.Utils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Slf4j
@Service
public class HEOnlineService {

	private static final @NonNull ParameterizedTypeReference<List<Appointment>> APPOINTMENT_TYPE = new ParameterizedTypeReference<>() {
	};
	private static final @NonNull ParameterizedTypeReference<List<Course>> COURSE_TYPE = new ParameterizedTypeReference<>() {
	};

	private final @NonNull String heOnlineUrl;
	private final @NonNull String courseEndpoint;
	private final @NonNull String coursesEndpoint;
	private final @NonNull String appointmentsEndpoint;
	private final @NonNull RestClient restClient;
	private final @NonNull KeycloakClient keycloakClient;

	public HEOnlineService(@Value("${heonline.url}") final @NonNull String heOnlineUrl,
						   @Value("${heonline.course-endpoint}") final @NonNull String courseEndpoint,
						   @Value("${heonline.courses-endpoint}") final @NonNull String coursesEndpoint,
						   @Value("${heonline.appointments-endpoint}") final @NonNull String appointmentsEndpoint,
						   @Value("${heonline.keycloak.url}") final @NonNull String keycloakUrl,
						   @Value("${heonline.keycloak.realm}") final @NonNull String keycloakRealm,
						   @Value("${heonline.keycloak.client-id}") final @NonNull String keycloakClientID,
						   @Value("${heonline.keycloak.client-secret}") final @NonNull String keycloakClientSecret) {
		Utils.validateUrl(heOnlineUrl, "HeOnline");
		Utils.validateUrl(heOnlineUrl + "/" + courseEndpoint.replace("{id}", "2"), "CourseEndpoint");
		Utils.validateUrl(heOnlineUrl + "/" + coursesEndpoint, "CoursesEndpoint");
		Utils.validateUrl(heOnlineUrl + "/" + appointmentsEndpoint, "AppointmentsEndpoint");
		Utils.validateUrl(keycloakUrl, "Keycloak");
		this.heOnlineUrl = heOnlineUrl;
		this.courseEndpoint = courseEndpoint;
		this.coursesEndpoint = coursesEndpoint;
		this.appointmentsEndpoint = appointmentsEndpoint;
		this.restClient = RestClient
				.builder()
				.build();

		this.keycloakClient = new KeycloakClient(
				keycloakUrl,
				keycloakRealm,
				keycloakClientID,
				keycloakClientSecret);
	}

	public @Nullable Appointment getAppointment(final int appointmentId) {
		final @Nullable List<Appointment> appointments = this.getAppointments();
		if (null == appointments) {
			log.error("No appointments found");
			return null;
		}
		try {
			return appointments.stream().filter(appointment -> appointment.uid() == appointmentId).findFirst().orElse(null);
		} catch (final @NonNull Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}

	public @Nullable List<Appointment> getAppointments() {
		KeycloakClient.TokenCollection tokens;
		try {
			tokens = keycloakClient.getTokens();
		} catch (final @NonNull IOException e) {
			log.error(e.getMessage());
			return null;
		}

		final @NonNull RestClient.ResponseSpec response = this.restClient.get()
				.uri(this.heOnlineUrl + "/" + this.appointmentsEndpoint)
				.header("Authorization", "Bearer " + tokens.getAccessToken())
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(StandardCharsets.UTF_8)
				.retrieve();

		final @NonNull ResponseEntity<@NotNull List<Appointment>> responseEntity;
		try {
			responseEntity = response.toEntity(APPOINTMENT_TYPE);
		} catch (final @NonNull Exception e) {
			log.error(e.getMessage());
			return null;
		}

		if (200 != responseEntity.getStatusCode().value()) {
			log.error("Response was '{}'", responseEntity.getStatusCode().value());
			return null;
		}

		try {
			return responseEntity.getBody();
		} catch (final @NonNull Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}

	public @Nullable Course getCourse(final Appointment appointment) {
		KeycloakClient.TokenCollection tokens;
		try {
			tokens = keycloakClient.getTokens();
		} catch (final @NonNull IOException e) {
			log.error(e.getMessage());
			return null;
		}
		final @NonNull RestClient.ResponseSpec response = this.restClient.get()
				.uri(this.heOnlineUrl + "/" + this.courseEndpoint, appointment.courseUid())
				.header("Authorization", "Bearer " + tokens.getAccessToken())
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(StandardCharsets.UTF_8)
				.retrieve();

		final @NonNull ResponseEntity<@NotNull Course> responseEntity;
		try {
			responseEntity = response.toEntity(Course.class);
		} catch (final @NonNull Exception e) {
			log.error(e.getMessage());
			return null;
		}

		if (200 != responseEntity.getStatusCode().value()) {
			log.error("Response was '{}'", responseEntity.getStatusCode().value());
			return null;
		}

		try {
			return responseEntity.getBody();
		} catch (final @NonNull Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}

	public @Nullable List<Course> getCourses() {
		KeycloakClient.TokenCollection tokens;
		try {
			tokens = keycloakClient.getTokens();
		} catch (final @NonNull IOException e) {
			log.error(e.getMessage());
			return null;
		}
		final @NonNull RestClient.ResponseSpec response = this.restClient.get()
				.uri(this.heOnlineUrl + "/" + this.coursesEndpoint)
				.header("Authorization", "Bearer " + tokens.getAccessToken())
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(StandardCharsets.UTF_8)
				.retrieve();

		final @NonNull ResponseEntity<@NotNull List<Course>> responseEntity;
		try {
			responseEntity = response.toEntity(COURSE_TYPE);
		} catch (final @NonNull Exception e) {
			log.error(e.getMessage());
			return null;
		}

		if (200 != responseEntity.getStatusCode().value()) {
			log.error("Response was '{}'", responseEntity.getStatusCode().value());
			return null;
		}

		try {
			return responseEntity.getBody();
		} catch (final @NonNull Exception e) {
			log.error(e.getMessage());
			return null;
		}
	}
}
