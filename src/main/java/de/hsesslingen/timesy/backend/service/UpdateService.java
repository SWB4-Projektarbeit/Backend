package de.hsesslingen.timesy.backend.service;

import de.hsesslingen.timesy.backend.model.Display;
import de.hsesslingen.timesy.backend.repository.DisplayRepository;
import de.hsesslingen.timesy.backend.repository.TemplateRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
@AllArgsConstructor
public class UpdateService {

	private final @NonNull DisplayService displayService;
	private final @NonNull DisplayRepository displayRepository;
	private final @NonNull TemplateRepository templateRepository;

	@Scheduled(cron = "0 45 7 * * *")
	@Scheduled(cron = "0 15 9 * * *")
	@Scheduled(cron = "0 0 11 * * *")
	@Scheduled(cron = "0 45 12 * * *")
	@Scheduled(cron = "0 15 15 * * *")
	@Scheduled(cron = "0 0 17 * * *")
	@Scheduled(cron = "0 45 16 * * *")
	public void updateDisplays() {
		for (final @NonNull Display display : this.displayRepository.findAll()) {
			this.templateRepository.getByUid(display.getTemplateUid()).ifPresent(template -> this.displayService.sendImage(display, template.templatePath()));
		}
	}
}
