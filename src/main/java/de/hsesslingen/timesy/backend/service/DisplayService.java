package de.hsesslingen.timesy.backend.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import de.hsesslingen.timesy.backend.epd.EpdAuthService;
import de.hsesslingen.timesy.backend.model.Display;
import de.hsesslingen.timesy.backend.utils.Utils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Slf4j
@Service
public class DisplayService {

	public static final @NonNull String LOCATION_DTO_ENDPOINT = "/api/location/%d";
	public static final @NonNull String IMAGE_ENDPOINT = "/api/location/%d/mem_combo/%d";

	private final @NonNull RestClient restClient;
	private final @NonNull EpdAuthService epdAuthService;

	@Value("${server.port}")
	private int port;

	public DisplayService(
			@Value("${displayserver.url}") final @NonNull String displayServerUrl,
			final @NonNull EpdAuthService epdAuthService) {
		Utils.validateUrl(displayServerUrl, "DisplayServer");
		this.restClient = RestClient.builder()
				.baseUrl(displayServerUrl)
				.build();
		this.epdAuthService = epdAuthService;
	}

	public byte[] capturePng(final @NonNull Path path, final int roomUid) {
		return this.capturePng(path, roomUid, null);
	}

	public byte[] capturePng(final @NonNull Path path, final int roomUid, final @Nullable Path imagePath) {
		try (final @NonNull Playwright playwright = Playwright.create();
		     final @NonNull Browser browser = playwright.chromium().launch()) {
			final @NonNull Page page = browser.newPage();
			page.navigate("file://"
					.concat(
							path.resolve("index.html")
									.toAbsolutePath()
									.normalize()
									.toString())
					.replace("\\", "/")
					.concat("?http://localhost:" + this.port + "/api-timesy/templates/data/" + roomUid));
			final @NonNull Page.ScreenshotOptions screenshotOptions = new Page.ScreenshotOptions().setFullPage(true);
			if (null != imagePath) {
				screenshotOptions.setPath(imagePath);
			}
			return page.screenshot(screenshotOptions);
		}
	}

	public @Nullable String getLocationDTO(final long displayUid) {
		final @Nullable String cookie = this.epdAuthService.getSessionCookie();
		if (null == cookie) {
			log.warn("EPD server: no session cookie available, skipping getLocationDTO for location {}.", displayUid);
			return null;
		}
		final @NonNull ResponseEntity<@NotNull String> responseEntity;
		try {
			responseEntity = this.restClient.get()
					.uri(String.format(LOCATION_DTO_ENDPOINT, displayUid))
					.header(HttpHeaders.COOKIE, cookie)
					.accept(MediaType.APPLICATION_JSON)
					.acceptCharset(StandardCharsets.UTF_8)
					.retrieve()
					.toEntity(String.class);
		} catch (final Exception e) {
			if (e.getMessage() != null && e.getMessage().contains("401")) {
				this.epdAuthService.invalidateSession();
			}
			log.warn("EPD server: getLocationDTO failed for location {} — {}", displayUid, e.getMessage());
			return null;
		}
		if (200 != responseEntity.getStatusCode().value()) {
			return null;
		}
		return responseEntity.getBody();
	}

	public void sendImage(final @NonNull Display display, final @NonNull Path path) {
		this.sendImage(display, path, 2);
	}

	public void sendImage(final @NonNull Display display, final @NonNull Path path, final int slot) {
		if (2 > slot || 100 < slot) {
			log.warn("EPD server: slot must be between 2 and 100, aborting.");
			return;
		}

		final @Nullable String cookie = this.epdAuthService.getSessionCookie();
		if (null == cookie) {
			log.warn("EPD server: no session cookie available, aborting sendImage for display {}.", display.getDisplayUid());
			return;
		}

		final @Nullable String locationDTO = this.getLocationDTO(display.getDisplayUid());
		if (null == locationDTO) {
			log.warn("EPD server: locationDTO for display '{}' could not be obtained, aborting.", display.getDisplayUid());
			return;
		}

		final byte[] imageBytes = this.capturePng(path, display.getRoomUid());

		// Build multipart/form-data body:
		// - "dto"          → LocationDTO JSON string
		// - "images[img1]" → PNG bytes (slot img1 = screen 1)
		final @NonNull LinkedMultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();

		final @NonNull HttpHeaders dtoHeaders = new HttpHeaders();
		dtoHeaders.setContentType(MediaType.APPLICATION_JSON);
		formData.add("dto", new HttpEntity<>(locationDTO, dtoHeaders));

		final @NonNull ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
			@Override
			public @NonNull String getFilename() {
				return "schedule.png";
			}
		};
		final @NonNull HttpHeaders imageHeaders = new HttpHeaders();
		imageHeaders.setContentType(MediaType.IMAGE_PNG);
		formData.add("images[img1]", new HttpEntity<>(imageResource, imageHeaders));

		final @NonNull ResponseEntity<@NotNull String> responseEntity;
		try {
			responseEntity = this.restClient.post()
					.uri(String.format(IMAGE_ENDPOINT, display.getDisplayUid(), slot))
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.header(HttpHeaders.COOKIE, cookie)
					.body(formData)
					.retrieve()
					.toEntity(String.class);
		} catch (final Exception e) {
			if (e.getMessage() != null && e.getMessage().contains("401")) {
				this.epdAuthService.invalidateSession();
			}
			log.warn("EPD server: sendImage failed for display {} — {}", display.getDisplayUid(), e.getMessage());
			return;
		}

		if (200 != responseEntity.getStatusCode().value()) {
			log.warn("EPD server: sendImage returned '{}' for display {}.", responseEntity.getStatusCode(), display.getDisplayUid());
		} else {
			log.info("EPD server: image successfully sent to display {} (location {}, slot {}).",
					display.getDisplayUid(), display.getDisplayUid(), slot);
		}
	}
}
