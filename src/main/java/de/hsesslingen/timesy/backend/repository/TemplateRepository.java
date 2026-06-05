package de.hsesslingen.timesy.backend.repository;

import de.zeanon.jsonfilemanager.JsonFileManager;
import de.zeanon.jsonfilemanager.internal.files.raw.JsonFile;
import de.zeanon.storagemanagercore.internal.utility.basic.BaseFileUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

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
                    //TODO log that the folder was empty
                    continue;
                }
                File metaDataFile = Arrays.stream(templateFiles).filter(
                        templateFile -> templateFile.getName().equals("metadata.json")
                ).findFirst().orElse(null);
                if (metaDataFile == null) {
                    //TODO log that metadata could not be found
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
