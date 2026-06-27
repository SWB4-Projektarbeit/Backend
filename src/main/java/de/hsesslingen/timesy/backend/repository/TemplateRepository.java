package de.hsesslingen.timesy.backend.repository;

import de.zeanon.jsonfilemanager.JsonFileManager;
import de.zeanon.jsonfilemanager.internal.files.raw.JsonFile;
import de.zeanon.storagemanagercore.internal.utility.basic.BaseFileUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@Component
public class TemplateRepository {

	private final @NonNull String templatesFolder;
	private final @NonNull Map<Integer, Template> templates = new HashMap<>();

	public TemplateRepository(@Value("${templates.folder}") final @NonNull String templatesFolder) {
		this.templatesFolder = templatesFolder;
		this.readTemplates();
	}

	public void readTemplates() {
		try {
			// Store which templates do not exist anymore and need to be removed
			final @NonNull Set<Integer> toRemove = new HashSet<>(this.templates.keySet());
			final @NonNull List<File> templateFolders = BaseFileUtils.listFolders(new File(this.templatesFolder));
			for (final @NonNull File templateFolder : templateFolders) {
				// check if the folder contains files
				final @Nullable File[] templateFiles = templateFolder.listFiles();
				if (null == templateFiles || templateFiles.length == 0) {
					log.info("The folder '{}' was empty and thus skipped.", templateFolder.getName());
					continue;
				}
				// check if metadata.json exists
				final @Nullable File metaDataFile = Arrays.stream(templateFiles).filter(
						templateFile -> null != templateFile && templateFile.getName().equals("metadata.json")
				).findFirst().orElse(null);
				if (null == metaDataFile) {
					log.info("No metadata.json was found in '{}', skipping...", templateFolder.getName());
					continue;
				}

				// check if index.html exists
				if (Arrays.stream(templateFiles).noneMatch(templateFile -> null != templateFile && templateFile.getName().equals("index.html"))) {
					log.info("No index.html was found in '{}', skipping...", templateFolder.getName());
					continue;
				}

				// check whether metadata.json contains the needed keys
				final @NonNull JsonFile metaData = JsonFileManager.jsonFile(metaDataFile).create();
				if (!metaData.hasKey("template_uid")) {
					log.info("metadata.json in '{}' is missing 'template_uid', skipping...", templateFolder.getName());
					continue;
				}
				if (!metaData.hasKey("template_name")) {
					log.info("metadata.json in '{}' is missing 'template_name', skipping...", templateFolder.getName());
					continue;
				}

				final int templateUid = metaData.getInt("template_uid");
				this.templates.put(
						templateUid,
						new Template(
								templateUid,
								metaData.getString("template_name"),
								templateFolder.toPath().toAbsolutePath().normalize()
						));
				// remove from the toRemove set since this template still exists
				toRemove.remove(templateUid);
			}
			toRemove.forEach(this.templates::remove);
		} catch (final IOException e) {
			log.warn("Error while reading templates folder: \n{}", e.getMessage());
		}
	}

	public @NonNull Collection<Template> findAll() {
		return this.templates.values();
	}

	public @NonNull Optional<Template> getByUid(final int templateUid) {
		return Optional.ofNullable(this.templates.get(templateUid));
	}


	public record Template(int templateUid,
						   String templateName,
						   Path templatePath) {
	}
}
