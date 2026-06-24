package de.hsesslingen.timesy.backend.utils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class Utils {

	private static final @NonNull Pattern URL_PATTERN = Pattern.compile("^(https?://)?([.\\w_-])*(:\\d+)?([/\\w_-])*(\\?[=&\\w_-]*)?$");

	public void validateUrl(final @NonNull String url, final @NonNull String urlName) {
		final @NonNull String strippedUrl = url.strip();
		if (strippedUrl.isEmpty() || strippedUrl.equals("\"\"")) {
			throw new IllegalArgumentException("Die " + urlName + " URL darf nicht leer sein.");
		}

		if (!URL_PATTERN.matcher(strippedUrl).matches()) {
			throw new IllegalArgumentException("Die " + urlName + " URL ist keine valide URL.");
		}
	}
}
