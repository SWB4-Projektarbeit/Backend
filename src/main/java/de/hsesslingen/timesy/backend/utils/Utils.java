package de.hsesslingen.timesy.backend.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class Utils {

	public void validateUrl(final @NotNull String url) {
		if (url.isEmpty()) {
			throw new IllegalArgumentException("Die HeOnline URL darf nicht leer sein.");
		}

		/**
		 try {
		 //noinspection ResultOfMethodCallIgnored
		 URI.create(url).toURL();
		 } catch (MalformedURLException | IllegalArgumentException e) {
		 throw new IllegalArgumentException("'" + url + "' ist keine valide URL.");
		 }
		 */
	}
}
