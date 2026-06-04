package de.hsesslingen.timesy.backend;

import de.hsesslingen.timesy.backend.model.Appointment;
import de.hsesslingen.timesy.backend.model.Course;
import de.hsesslingen.timesy.backend.repository.TemplateRepository;
import de.hsesslingen.timesy.backend.service.DisplayService;
import de.hsesslingen.timesy.backend.service.HEOnlineService;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@AllArgsConstructor
class BackendApplicationTests {

	@Test
	void contextLoads(@Autowired HEOnlineService heOnlineService, @Value("${heonline.url}") String heOnlineUrl) {
        List<Appointment> appointments = heOnlineService.getAppointments();
        if (appointments == null) {
            Assertions.fail("No appointments found");
        }
        for (Appointment appointment : appointments) {
            System.out.println("HEOnline tests: " + appointment);
            Course course = heOnlineService.getCourse(appointment);
            Assertions.assertNotNull(course);
            System.out.println("HEOnline tests: " + course);
        }
	}

    @Test
    void templateLoads(@Autowired TemplateRepository repository, @Autowired DisplayService displayService) {
        System.out.println("[INFO] Clearing TestImages folder to run tests");
        boolean result = true;
        try {
            final File tempFolder = new File("src/test/resources/testimages");
            if (tempFolder.exists() && tempFolder.listFiles() != null) {
                final File[] fileList = tempFolder.listFiles();
                if (fileList != null) {
                    for (final File tempFile : fileList) {
                        if (tempFile != null && !tempFile.delete()) {
                            result = false;
                        }
                    }
                }
            } else {
                result = false;
                System.out.println("[INFO] Folder does not exist");
            }
            System.out.println(result ? "[INFO] Cleared Folder" : "[INFO] Failed to clear Folder");
        } catch (final UncheckedIOException e) {
            System.out.println(e.getMessage());
        }

        repository.readTemplates();
        Collection<TemplateRepository.Template> templates = repository.findAll();
        if (templates.isEmpty()) {
            Assertions.fail("No templates found");
        }
        for (TemplateRepository.Template template : templates) {
            System.out.println("Template tests: " + template);
            System.out.println("Template tests: " + template.getTemplatePath());
            byte[] imageData = displayService.capturePng(template.getTemplatePath(), Paths.get("src/test/resources/testimages/test.png"));
            System.out.println("Template tests: " + Arrays.toString(imageData));
        }
    }
}
