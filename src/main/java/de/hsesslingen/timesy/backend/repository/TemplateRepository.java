package de.hsesslingen.timesy.backend.repository;

import de.zeanon.jsonfilemanager.JsonFileManager;
import de.zeanon.jsonfilemanager.internal.files.raw.JsonFile;
import de.zeanon.storagemanagercore.internal.utility.basic.BaseFileUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@Component
public class TemplateRepository {

	private final String templatesFolder;

	private final Map<Integer, Template> templates = new HashMap<>();

	public TemplateRepository(@Value("${templates.folder}") final String templatesFolder) {
		this.templatesFolder = templatesFolder;
		readTemplates();
	}

	public void readTemplates() {
		try {
			Set<Integer> toRemove = new HashSet<>(templates.keySet());
			List<File> templateFolders = BaseFileUtils.listFolders(new File(this.templatesFolder));
			for (File template : templateFolders) {
				File[] templateFiles = template.listFiles();
				if (templateFiles == null || templateFiles.length == 0) {
					log.info("The folder '{}' was empty and thus skipped.", template.getName());
					continue;
				}
				File metaDataFile = Arrays.stream(templateFiles).filter(
						templateFile -> templateFile.getName().equals("metadata.json")
				).findFirst().orElse(null);
				if (metaDataFile == null) {
					log.info("No metadata.json was found in '{}', skipping...", template.getName());
					continue;
				}

				if (Arrays.stream(templateFiles).filter(
						templateFile -> templateFile.getName().equals("index.html")
				).findFirst().orElse(null) == null) {
					log.info("No index.html was found in '{}', skipping...", template.getName());
					continue;
				}

				JsonFile metaData = JsonFileManager.jsonFile(metaDataFile).create();
				Integer templateUid = metaData.getInt("template_uid");
				this.templates.put(templateUid, new Template(
						templateUid,
						metaData.getString("template_name"),
						template.toPath().toAbsolutePath().normalize()
				));
				toRemove.remove(templateUid);
			}
			toRemove.forEach(templates::remove);
		} catch (IOException _) {

		}
	}

	public Collection<Template> findAll() {
		return this.templates.values();
	}

	public Optional<Template> getByUid(final int templateUid) {
		return Optional.ofNullable(templates.get(templateUid));
	}

	@Getter
	@Setter
	@ToString
	@AllArgsConstructor
	public static class Template {
		public int templateUid;
		public String templateName;
		public Path templatePath;
	}
}
