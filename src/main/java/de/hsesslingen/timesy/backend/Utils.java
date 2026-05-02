package de.hsesslingen.timesy.backend;

import de.zeanon.storagemanagercore.internal.utility.basic.Objects;
import de.zeanon.thunderfilemanager.internal.files.config.ThunderConfig;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URI;

@UtilityClass
public class Utils {
    
    public String getAndValidateUrl(final @NotNull ThunderConfig thunderConfig, final @NotNull String key) {
        final String url = Objects.notNull(thunderConfig.getString(key),
                key + " wurde nicht in der Config angegeben, bitte geben sie die URL an.");

        if (url.isEmpty()) {
            throw new IllegalArgumentException(key + " darf nicht leer sein.");
        }

        try {
            //noinspection ResultOfMethodCallIgnored
            URI.create(url).toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new IllegalArgumentException("In " + key + " (" + url + ") ist keine valide URL.");
        }

        return url;
    }
}
