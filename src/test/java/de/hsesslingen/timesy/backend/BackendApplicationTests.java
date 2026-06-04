package de.hsesslingen.timesy.backend;

import de.hsesslingen.timesy.backend.controller.Controller;
import de.hsesslingen.timesy.backend.dto.BuildingDTO;
import de.hsesslingen.timesy.backend.model.Appointment;
import de.hsesslingen.timesy.backend.model.Course;
import de.hsesslingen.timesy.backend.model.Display;
import de.hsesslingen.timesy.backend.repository.DisplayRepository;
import de.hsesslingen.timesy.backend.repository.TemplateRepository;
import de.hsesslingen.timesy.backend.service.DisplayService;
import de.hsesslingen.timesy.backend.service.HEOnlineService;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@AllArgsConstructor
class BackendApplicationTests {

	@Test
	void contextLoads(@Autowired HEOnlineService heOnlineService) {
        List<Appointment> appointments = heOnlineService.getAppointments();
        if (appointments == null) {
            Assertions.fail("No appointments found");
        }
        System.out.println("[Tests] HeOnline");
        for (Appointment appointment : appointments) {
            System.out.println("    - Appointment: " + appointment);
            Course course = heOnlineService.getCourse(appointment);
            Assertions.assertNotNull(course);
            System.out.println("        - Course: " + course);
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
        System.out.println("[Tests] Templates");
        for (TemplateRepository.Template template : templates) {
            System.out.println("    - Template: " + template);
            System.out.println("        - Path: " + template.getTemplatePath());
            byte[] imageData = displayService.capturePng(template.getTemplatePath(), Paths.get("src/test/resources/testimages/test.png"));
            System.out.println("        - Imagedata: " + Arrays.toString(imageData));
        }
    }

    @Test
    public void mappper(@Autowired DisplayRepository displayRepository, @Autowired Controller controller) {
        controller.createDummyData();
        System.out.println("[Test] Mapper - Displays");
        for (Display display : displayRepository.findAll()) {
            System.out.println("    - " + display);
        }

        List<BuildingDTO> buildings = controller.getAllRooms2(null, null, null, null, null, null);
        System.out.println("[Test] Mapper - Buildings");
        for (BuildingDTO room : buildings) {
            System.out.println("    - " + room);
        }

        Assertions.assertEquals(2, buildings.size());
    }
}
