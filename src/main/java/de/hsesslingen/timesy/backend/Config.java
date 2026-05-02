package de.hsesslingen.timesy.backend;

import de.zeanon.storagemanagercore.internal.base.settings.Comment;
import de.zeanon.storagemanagercore.internal.base.settings.Reload;
import de.zeanon.thunderfilemanager.ThunderFileManager;
import de.zeanon.thunderfilemanager.internal.files.config.ThunderConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public ThunderConfig loadConfig() {
        return ThunderFileManager.thunderConfig("Config", "config")
                                 .fromResource("resources/config.tf")
                                 .reloadSetting(Reload.INTELLIGENT)
                                 .commentSetting(Comment.PRESERVE)
                                 .concurrentData(true)
                                 .create();
    }
}
