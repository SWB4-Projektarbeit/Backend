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

import static org.springframework.security.oauth2.client.web.ClientAttributes.clientRegistrationId;


@Slf4j
@Service
public class HEOnlineService {

	public static final @NonNull String APPOINTMENTS_ENDPOINT = "he/co/co-tm-core/course/api/appointments";
	public static final @NonNull String COURSE_ENDPOINT = "he/co/co-tm-core/course/api/courses/{id}";
	public static final @NonNull String COURSES_ENDPOINT = "he/co/co-tm-core/course/api/courses?limit=10000";

	private static final @NonNull ParameterizedTypeReference<List<Appointment>> APPOINTMENT_TYPE = new ParameterizedTypeReference<>() {
	};
	private static final @NonNull ParameterizedTypeReference<List<Course>> COURSE_TYPE = new ParameterizedTypeReference<>() {
	};

	private final @NonNull RestClient restClient;
	private final @NonNull KeycloakClient keycloakClient;
	private final @NonNull String heOnlineUrl;

	public HEOnlineService(@Value("${heonline.url}") final @NonNull String heOnlineUrl,
						   @Value("${heonline-keycloak.url}") final @NonNull String keycloakUrl,
						   @Value("${heonline-keycloak.realm}") final @NonNull String keycloakRealm,
						   @Value("${heonline-keycloak.client-id}") final @NonNull String keycloakClientID,
						   @Value("${heonline-keycloak.client-secret}") final @NonNull String keycloakClientSecret) {
		Utils.validateUrl(heOnlineUrl, "HeOnline");
		this.heOnlineUrl = heOnlineUrl;
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
			return null;
		}
		try {
			return appointments.stream().filter(appointment -> appointment.uid() == appointmentId).findFirst().orElse(null);
		} catch (Exception _) {
			return null;
		}
	}

	public @Nullable List<Appointment> getAppointments() {
		KeycloakClient.TokenCollection tokens;
		try {
			tokens = keycloakClient.getTokens();
		} catch (final @NonNull IOException e) {
            throw new RuntimeException(e);
        }

		final @NonNull RestClient.ResponseSpec response = this.restClient.get()
				.uri(this.heOnlineUrl + "/" + APPOINTMENTS_ENDPOINT)
				.header("Authorization", "Bearer " + tokens.getAccessToken())
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(StandardCharsets.UTF_8)
				.retrieve();
		System.out.println(response.toEntity(String.class));

		final @NonNull ResponseEntity<@NotNull List<Appointment>> responseEntity;
		try {
			responseEntity = response.toEntity(APPOINTMENT_TYPE);
		} catch (final Exception e) {
			log.error(e.getMessage());
			return null;
		}

		if (200 != responseEntity.getStatusCode().value()) {
			return null;
		}

		try {
			return responseEntity.getBody();
		} catch (final @NonNull Exception e) {
			return null;
		}
	}

	public @Nullable Course getCourse(final Appointment appointment) {
		KeycloakClient.TokenCollection tokens;
		try {
			tokens = keycloakClient.getTokens();
		} catch (final @NonNull IOException _) {
			return null;
		}
		final @NonNull RestClient.ResponseSpec response = this.restClient.get()
				.uri(this.heOnlineUrl + "/" + COURSE_ENDPOINT, appointment.courseUid())
				.header("Authorization", "Bearer " + tokens.getAccessToken())
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(StandardCharsets.UTF_8)
				.retrieve();

		final @NonNull ResponseEntity<@NotNull Course> responseEntity;
		try {
			responseEntity = response.toEntity(Course.class);
		} catch (final Exception e) {
			return null;
		}

		if (200 != responseEntity.getStatusCode().value()) {
			return null;
		}

		try {
			return responseEntity.getBody();
		} catch (final Exception e) {
			return null;
		}
	}

	public @Nullable List<Course> getCourses() {
		KeycloakClient.TokenCollection tokens;
		try {
			tokens = keycloakClient.getTokens();
		} catch (final @NonNull IOException _) {
			return null;
		}
		final @NonNull RestClient.ResponseSpec response = this.restClient.get()
				.uri(this.heOnlineUrl + "/" + COURSES_ENDPOINT)
				.header("Authorization", "Bearer " + tokens.getAccessToken())
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(StandardCharsets.UTF_8)
				.retrieve();

		final @NonNull ResponseEntity<@NotNull List<Course>> responseEntity;
		try {
			responseEntity = response.toEntity(COURSE_TYPE);
		} catch (final Exception e) {
			return null;
		}

		if (200 != responseEntity.getStatusCode().value()) {
			return null;
		}

		try {
			return responseEntity.getBody();
		} catch (final Exception e) {
			return null;
		}
	}
}
