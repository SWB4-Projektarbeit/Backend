package de.hsesslingen.timesy.backend.repository;

import de.zeanon.jsonfilemanager.JsonFileManager;
import de.zeanon.jsonfilemanager.internal.files.raw.JsonFile;
import de.zeanon.storagemanagercore.internal.utility.basic.BaseFileUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Component
public class TemplateRepository {

    private final String templatesFolder;

    private final Map<String, Template> templates = new HashMap<>();

    public TemplateRepository(@Value("${templates.folder}") String templatesFolder) {
        this.templatesFolder = templatesFolder;
        readTemplates();
    }

    public void readTemplates() {
        try {
            Set<String> toRemove = new HashSet<>(templates.keySet());
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
                String templateUid = metaData.getString("template_uid");
                this.templates.put(templateUid, new Template(
                        templateUid,
                        metaData.getString("template_name"),
                        template.getAbsolutePath()
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

    public Optional<Template> getByUid(String templateUid) {
        return Optional.ofNullable(templates.get(templateUid));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Template {
        public String templateUid;
        public String templateName;
        public String templatePath;
    }
}
