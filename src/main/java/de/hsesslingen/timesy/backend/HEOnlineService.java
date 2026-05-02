package de.hsesslingen.timesy.backend;

import de.hsesslingen.timesy.backend.model.Appointment;
import de.hsesslingen.timesy.backend.model.Course;
import de.zeanon.thunderfilemanager.internal.files.config.ThunderConfig;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;


@Service
@Slf4j
public class HEOnlineService {

    public static final String HE_ONLINE_URL = "HE-Online URL";
    public static final String APPOINTMENTS_ENDPOINT = "he/co/co-tm-core/course/api/appointments";
    public static final String COURSE_ENDPOINT = "he/co/co-tm-core/course/api/courses/{id}";

    private static final ParameterizedTypeReference<List<Appointment>> APPOINTMENT_TYPE = new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    public HEOnlineService(final @NotNull ThunderConfig thunderConfig) {
        restClient = RestClient.builder()
                .baseUrl(Utils.getAndValidateUrl(thunderConfig, HE_ONLINE_URL))
                .build();
        // TODO: Cookies for KeyCloak instance!
    }

    public @Nullable List<Appointment> getAppointments() {
        RestClient.ResponseSpec response = restClient.get()
                .uri(APPOINTMENTS_ENDPOINT)
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .retrieve();

        ResponseEntity<@NotNull List<Appointment>> responseEntity = response.toEntity(APPOINTMENT_TYPE);

        if (responseEntity.getStatusCode().value() == 200) {
            return responseEntity.getBody();
        } else {
            return null;
        }
    }

    public @Nullable Course getCourse(Appointment appointment) {
        RestClient.ResponseSpec response = restClient.get()
                .uri(COURSE_ENDPOINT, appointment.getCourseUid())
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .retrieve();

        ResponseEntity<@NotNull Course> responseEntity = response.toEntity(Course.class);

        if (responseEntity.getStatusCode().value() == 200) {
            return responseEntity.getBody();
        } else {
            return null;
        }
    }
}
