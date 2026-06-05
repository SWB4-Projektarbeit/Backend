package de.hsesslingen.timesy.backend.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import de.hsesslingen.timesy.backend.utils.Utils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Service
public class DisplayService {

    public static final String LOCATION_DTO_ENDPOINT = "/api/location/%d";
    public static final String IMAGE_ENDPOINT = "/api/location/%d/mem_combo/%d";

    private final RestClient restClient;

    public DisplayService(@Value("${displayserver.url}") final String displayServerUrl) {
        Utils.validateUrl(displayServerUrl);
        restClient = RestClient.builder()
                .baseUrl(displayServerUrl)
                .build();
    }

    public byte[] capturePng(final Path path) {
        return capturePng(path, null);
    }

    public byte[] capturePng(final Path path, final Path imagePath) {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch()) {
            Page page = browser.newPage();
            page.navigate(path.resolve("index.html").toAbsolutePath().normalize().toString().replace("\\", "/"));
            Page.ScreenshotOptions screenshotOptions = new Page.ScreenshotOptions().setFullPage(true);
            if (imagePath != null) {
                screenshotOptions.setPath(imagePath);
            }
            return page.screenshot(screenshotOptions);
        }
    }

    public String getLocationDTO(final long displayUid) {
        RestClient.ResponseSpec response = this.restClient.get()
                .uri(String.format(LOCATION_DTO_ENDPOINT, displayUid))
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .retrieve();
        ResponseEntity<String> responseEntity = response.toEntity(String.class);
        if (responseEntity.getStatusCode().value() != 200) {
            return null;
        }
        try {
            return responseEntity.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public void sendImage(final long displayUid, final Path path) {
        this.sendImage(displayUid, path, 2);
    }

    public void sendImage(final long displayUid, final Path path, int slot) {
        if (slot < 2 || slot > 100) {
            //TODO log that slot has to be between 2 and 100
            return;
        }
        String locationDTO = getLocationDTO(displayUid);
        if (locationDTO == null) {
            //TODO log that locationDTO could not be obtained
            return;
        }
        RestClient.ResponseSpec response = this.restClient.post()
                .uri(String.format(IMAGE_ENDPOINT, displayUid, slot))
                .body(new ImagePostBody(locationDTO, capturePng(path)))
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .retrieve();
        ResponseEntity<String> responseEntity = response.toEntity(String.class);
        if (responseEntity.getStatusCode().value() != 200) {
            //TODO log that image could not be posted
        }
    }

    @AllArgsConstructor
    public static class ImagePostBody {
        public String dto;
        public byte[] images;
    }
}
