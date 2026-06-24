package de.hsesslingen.timesy.backend.service;

import com.microsoft.playwright.*;
import de.hsesslingen.timesy.backend.model.Display;
import de.hsesslingen.timesy.backend.utils.Utils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
public class DisplayService {

	private final @NonNull RestClient restClient;
	@Value("${displayserver.location-dto-endpoint}")
	private @NonNull String locationDtoEndpoint;
	@Value("${displayserver.image-endpoint}")
	private @NonNull String imageEndpoint;
	@Value("${server.port}")
	private int port;

	public DisplayService(@Value("${displayserver.url}") final String displayServerUrl) {
		Utils.validateUrl(displayServerUrl, "DisplayServer");
		this.restClient = RestClient.builder()
				.baseUrl(displayServerUrl)
				.build();
	}

	public byte[] capturePng(final @NonNull Path path, final int roomUid) {
		return this.capturePng(path, roomUid, null);
	}

	public byte[] capturePng(final @NonNull Path path, final int roomUid, final @Nullable Path imagePath) {
		try (final @NonNull Playwright playwright = Playwright.create();
			 final @NonNull Browser browser = playwright.chromium().launch(
					 new BrowserType.LaunchOptions().setArgs(List.of("--disable-web-security")))
		) {
			final @NonNull Page page = browser.newPage(
					new Browser.NewPageOptions().setViewportSize(1200, 1600)
			);
			@NonNull ConsoleMessage msg = page.waitForConsoleMessage(() -> page
					.navigate("file://"
							.concat(
									path.resolve("index.html")
											.toAbsolutePath()
											.normalize()
											.toString())
							.replace("\\", "/")
							.concat("?http://localhost:" + this.port + "/api-timesy/templates/data/" + roomUid)));
			while (!msg.text().equals("template rendered")) {
				msg = page.waitForConsoleMessage(() -> {
				});
			}
			final @NonNull Page.ScreenshotOptions screenshotOptions = new Page.ScreenshotOptions().setFullPage(true);
			if (null != imagePath) {
				screenshotOptions.setPath(imagePath);
			}
			return page.screenshot(screenshotOptions);
		}
	}

	public @Nullable String getLocationDTO(final long displayUid) {
		final @NonNull ResponseEntity<@NotNull String> responseEntity = this.restClient.get()
				.uri(this.locationDtoEndpoint, displayUid)
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(StandardCharsets.UTF_8)
				.retrieve()
				.toEntity(String.class);
		if (200 != responseEntity.getStatusCode().value()) {
			return null;
		}
		try {
			return responseEntity.getBody();
		} catch (final Exception e) {
			return null;
		}
	}

	public void sendImage(final @NonNull Display display, final @NonNull Path path) {
		this.sendImage(display, path, null, 2, false);
	}

	public void sendImage(final @NonNull Display display, final @NonNull Path path, final @Nullable String imageName) {
		this.sendImage(display, path, imageName, 2, true);
	}

	public void sendImage(final @NonNull Display display, final @NonNull Path path, final @Nullable String imageName, final int slot, final boolean ignoreLocationDTO) {
		if (2 > slot || 100 < slot) {
			log.warn("The slot for the display has to be between 2 and 100, aborting.");
			return;
		}
		final @Nullable String locationDTO;
		if (!ignoreLocationDTO) {
			locationDTO = this.getLocationDTO(display.getDisplayUid());
			if (null == locationDTO) {
				log.warn("The locationDTO for '{}' could not be obtained, aborting.", display.getDisplayUid());
				return;
			}
		} else {
			locationDTO = null;
		}
		final @Nullable Path imagePath;
		if (imageName != null) {
			imagePath = path.resolve(imageName);
		} else {
			imagePath = null;
		}
		final @NonNull RestClient.ResponseSpec response = this.restClient.post()
				.uri(this.imageEndpoint, display.getDisplayUid(), slot)
				.body(new ImagePostBody(locationDTO, this.capturePng(path, display.getRoomUid(), imagePath)))
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(StandardCharsets.UTF_8)
				.retrieve();
		final @NonNull ResponseEntity<@NotNull String> responseEntity;
		try {
			responseEntity = response.toEntity(String.class);
		} catch (final Exception e) {
			log.warn("Image could not be posted");
			return;
		}
		if (200 != responseEntity.getStatusCode().value()) {
			log.warn("Image could not be posted, DisplayServer returned '{}'", responseEntity);
		}
	}

	public record ImagePostBody(@Nullable String dto, byte[] images) {
	}
}
