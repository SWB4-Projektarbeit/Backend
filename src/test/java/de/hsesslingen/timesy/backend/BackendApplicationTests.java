package de.hsesslingen.timesy.backend;

import de.hsesslingen.timesy.backend.dto.BuildingDTO;
import de.hsesslingen.timesy.backend.dto.TemplateDataDTO;
import de.hsesslingen.timesy.backend.model.Appointment;
import de.hsesslingen.timesy.backend.model.Course;
import de.hsesslingen.timesy.backend.model.Display;
import de.hsesslingen.timesy.backend.repository.DisplayRepository;
import de.hsesslingen.timesy.backend.repository.TemplateRepository;
import de.hsesslingen.timesy.backend.service.DisplayService;
import de.hsesslingen.timesy.backend.service.FrontendService;
import de.hsesslingen.timesy.backend.service.HEOnlineService;
import de.hsesslingen.timesy.backend.utils.Utils;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@NoArgsConstructor
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
class BackendApplicationTests {

	@BeforeAll
	static void initDB(@Autowired final @NonNull FrontendService frontendService) {
		frontendService.createDummyData();
	}

	@BeforeAll
	static void cleanFolder() {
		System.out.println("[INFO] Clearing TestImages folder to run tests");
		boolean result = true;
		try {
			final @NonNull File tempFolder = new File("src/test/resources/testimages");
			if (tempFolder.exists() && tempFolder.listFiles() != null) {
				final @Nullable File[] fileList = tempFolder.listFiles();
				if (fileList != null) {
					for (final @Nullable File tempFile : fileList) {
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
	@Order(1)
	void checkProperties(@Value("${heonline.url}") final @NonNull String heOnlineUrl,
						 @Value("${displayserver.url}") final @NonNull String displayServerUrl,
						 @Value("${templates.folder}") final @NonNull String templatesFolder) {
		System.out.println("[Tests] HeOnline URL: '" + heOnlineUrl + "'");
		Assertions.assertNotEquals("\"\"", heOnlineUrl);
		Assertions.assertNotEquals("<placeholder>", heOnlineUrl);
		Utils.validateUrl(heOnlineUrl, "HeOnline");
		System.out.println("[Tests] DisplayServer URL: '" + displayServerUrl + "'");
		Assertions.assertNotEquals("\"\"", displayServerUrl);
		Assertions.assertNotEquals("<placeholder>", displayServerUrl);
		Utils.validateUrl(displayServerUrl, "DisplayServer");
		System.out.println("[Tests] Templates folder: '" + templatesFolder + "'");
		Assertions.assertNotEquals("\"\"", templatesFolder);
		Assertions.assertNotEquals("<placeholder>", templatesFolder);
		System.out.println();
	}

	@Test
	@Order(2)
	void contextLoads(@Autowired final @NonNull HEOnlineService heOnlineService) {
		final @Nullable List<Appointment> appointments = heOnlineService.getAppointments();
		if (appointments == null) {
			Assertions.fail("No appointments found");
		}
		System.out.println("[Tests] HeOnline");
		for (final @NonNull Appointment appointment : appointments) {
			System.out.println("    - Appointment: " + appointment);
			final @Nullable Course course = heOnlineService.getCourse(appointment);
			Assertions.assertNotNull(course);
			System.out.println("        - Course: " + course);
		}
		System.out.println();
	}

	@Test
	@Order(3)
	void templateLoads(@Autowired final @NonNull TemplateRepository repository,
					   @Autowired final @NonNull DisplayService displayService) {
		repository.readTemplates();
		final @NonNull Collection<TemplateRepository.Template> templates = repository.findAll();
		if (templates.isEmpty()) {
			Assertions.fail("No templates found");
		}
		System.out.println("[Tests] Templates");
		for (final @NonNull TemplateRepository.Template template : templates) {
			System.out.println("    - Template: " + template);
			System.out.println("        - Path: " + template.templatePath());
			final byte[] imageData = displayService.capturePng(template.templatePath(), 6976, Paths.get("src/test/resources/testimages/test.png"));
			System.out.println("        - Imagedata: " + Arrays.toString(imageData));
		}
		System.out.println();
	}

	@Test
	@Order(4)
	public void mappper(@Autowired final @NonNull DisplayRepository displayRepository,
						@Autowired final @NonNull FrontendService frontendService) {
		System.out.println("[Test] Mapper - Displays");
		for (final @NonNull Display display : displayRepository.findAll()) {
			System.out.println("    - " + display);
		}
		System.out.println();

		final @NonNull ResponseEntity<?> buildingEntity = frontendService.getAllRooms(null, null, null, null, null, null, null);
		Assertions.assertEquals(HttpStatus.OK, buildingEntity.getStatusCode());
		Assertions.assertInstanceOf(List.class, buildingEntity.getBody());
		//noinspection unchecked
		final @NonNull List<BuildingDTO> buildings = (List<BuildingDTO>) buildingEntity.getBody();
		System.out.println("[Test] Mapper - Buildings");
		for (final @NonNull BuildingDTO building : buildings) {
			System.out.println("    - " + building);
		}
		System.out.println();

		Assertions.assertEquals(2, buildings.size());
	}

	@Test
	@Order(5)
	public void updateTemplate(@Autowired final @NonNull FrontendService frontendService,
							   @Autowired final @NonNull TemplateRepository templateRepository) {
		templateRepository.readTemplates();
		frontendService.updateRoom(6976, 124);
		final @NonNull ResponseEntity<?> buildingEntity = frontendService.getAllRooms(null, null, 6976, null, null, null, null);
		Assertions.assertEquals(HttpStatus.OK, buildingEntity.getStatusCode());
		Assertions.assertInstanceOf(List.class, buildingEntity.getBody());
		//noinspection unchecked
		final @NonNull List<BuildingDTO> buildings = (List<BuildingDTO>) buildingEntity.getBody();
		Assertions.assertEquals(1, buildings.size());
		Assertions.assertEquals(1, buildings.getFirst().rooms().size());
		Assertions.assertEquals(124, buildings.getFirst().rooms().getFirst().templateUid());
	}

	@Test
	@Order(6)
	public void getTemplateData(@Autowired final @NonNull FrontendService frontendService,
								@Autowired final @NonNull TemplateRepository templateRepository) {
		templateRepository.readTemplates();
		final @NonNull ResponseEntity<?> templateDataEntity = frontendService.getTemplateData(6976);
		Assertions.assertEquals(HttpStatus.OK, templateDataEntity.getStatusCode());
		Assertions.assertInstanceOf(TemplateDataDTO.class, templateDataEntity.getBody());
		final @NonNull TemplateDataDTO templateData = (TemplateDataDTO) templateDataEntity.getBody();

		Assertions.assertEquals("Room1", templateData.room().name());
		Assertions.assertEquals("Vorlesungssaal", templateData.room().type());
		Assertions.assertEquals("Lecture Hall", templateData.room().typeEn());
		Assertions.assertEquals(4, templateData.slots().size());

		Assertions.assertEquals("08:45", templateData.slots().getFirst().timeStart());
		Assertions.assertEquals("11:15", templateData.slots().getFirst().timeEnd());
		Assertions.assertEquals("Gesellschaftliche Bedeutung von familien- und schulergänzenden Angeboten", templateData.slots().getFirst().title());
		Assertions.assertEquals("This is an english title", templateData.slots().getFirst().titleEn());
		Assertions.assertNull(templateData.slots().getFirst().movedTo());

		Assertions.assertEquals("11:30", templateData.slots().get(1).timeStart());
		Assertions.assertEquals("13:00", templateData.slots().get(1).timeEnd());
		Assertions.assertEquals("Gesellschaftliche Bedeutung von familien- und schulergänzenden Angeboten 3", templateData.slots().get(1).title());
		Assertions.assertNull(templateData.slots().get(1).titleEn());

		Assertions.assertEquals("13:00", templateData.slots().get(2).timeStart());
		Assertions.assertEquals("14:00", templateData.slots().get(2).timeEnd());
		Assertions.assertEquals("FREI - 60 Min", templateData.slots().get(2).title());
		Assertions.assertEquals("FREE - 60 Min", templateData.slots().get(2).titleEn());

		Assertions.assertEquals("14:00", templateData.slots().get(3).timeStart());
		Assertions.assertEquals("15:30", templateData.slots().get(3).timeEnd());
		Assertions.assertEquals("Gesellschaftliche Bedeutung von familien- und schulergänzenden Angeboten 4", templateData.slots().get(3).title());
		Assertions.assertNull(templateData.slots().get(3).titleEn());
	}
}
