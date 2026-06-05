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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    @BeforeAll
    static void checkProperties(@Value("${heonline.url}") final String heOnlineUrl, @Value("${templates.folder}") final String templatesFolder) {
        System.out.println("[Tests] HeOnline URL: '" + heOnlineUrl + "'");
        Assertions.assertNotEquals("", heOnlineUrl);
        System.out.println("[Tests] Templates folder: '" + templatesFolder + "'");
        Assertions.assertNotEquals("", templatesFolder);
    }

    @BeforeAll
    static void initDB(@Autowired final Controller controller) {
        controller.createDummyData();
    }

    @BeforeAll
    static void cleanFolder() {
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
    }

	@Test
	void contextLoads(@Autowired final HEOnlineService heOnlineService) {
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
    void templateLoads(@Autowired final TemplateRepository repository, @Autowired final DisplayService displayService) {
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
    public void mappper(@Autowired final DisplayRepository displayRepository, @Autowired final Controller controller) {
        System.out.println("[Test] Mapper - Displays");
        for (Display display : displayRepository.findAll()) {
            System.out.println("    - " + display);
        }

        ResponseEntity<?> buildingEntity = controller.getAllRooms(null, null, null, null, null, null);
        Assertions.assertEquals(HttpStatus.OK, buildingEntity.getStatusCode());
        Assertions.assertInstanceOf(List.class, buildingEntity.getBody());
        //noinspection unchecked
        List<BuildingDTO> buildings = (List<BuildingDTO>) buildingEntity.getBody();
        System.out.println("[Test] Mapper - Buildings");
        for (BuildingDTO building : buildings) {
            System.out.println("    - " + building);
        }

        Assertions.assertEquals(2, buildings.size());
    }

    @Test
    public void updateTemplate(@Autowired final Controller controller, @Autowired final TemplateRepository templateRepository) {
        templateRepository.readTemplates();
        controller.updateRoom(6976, 124);
        ResponseEntity<?> buildingEntity = controller.getAllRooms(null, null, 6976, null, null, null);
        Assertions.assertEquals(HttpStatus.OK, buildingEntity.getStatusCode());
        Assertions.assertInstanceOf(List.class, buildingEntity.getBody());
        //noinspection unchecked
        List<BuildingDTO> buildings = (List<BuildingDTO>) buildingEntity.getBody();
        Assertions.assertEquals(1, buildings.size());
        Assertions.assertEquals(1, buildings.getFirst().getRooms().size());
        Assertions.assertEquals(124, buildings.getFirst().getRooms().getFirst().getTemplateUid());
    }
}
