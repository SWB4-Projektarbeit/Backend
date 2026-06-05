package de.hsesslingen.timesy.backend.service;

import de.hsesslingen.timesy.backend.model.Display;
import de.hsesslingen.timesy.backend.repository.DisplayRepository;
import de.hsesslingen.timesy.backend.repository.TemplateRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@EnableScheduling
@AllArgsConstructor
public class UpdateService {

    private static final long DELAY_MS = 90 * 60 * 1000;

    private final DisplayService displayService;
    private final DisplayRepository displayRepository;
    private final TemplateRepository templateRepository;
    private final HEOnlineService heOnlineService;

    @Scheduled(cron = "0 45 7 * * *")
    @Scheduled(cron = "0 15 9 * * *")
    @Scheduled(cron = "0 0 11 * * *")
    @Scheduled(cron = "0 45 12 * * *")
    @Scheduled(cron = "0 15 15 * * *")
    @Scheduled(cron = "0 0 17 * * *")
    @Scheduled(cron = "0 45 16 * * *")
    public void updateDisplays() {
        for (Display display : displayRepository.findAll()) {
            Optional<TemplateRepository.Template> templateData = templateRepository.getByUid(display.getTemplateUid());
            if (templateData.isEmpty()) {
                continue;
            }
            displayService.sendImage(display.getDisplayUid(), templateData.get().getTemplatePath());
        }
    }
}
