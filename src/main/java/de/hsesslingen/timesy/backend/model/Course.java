package de.hsesslingen.timesy.backend.model;

import lombok.NonNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * JSON DTO from HE Online API
 */
public record Course(long uid,
					 boolean blocked,
					 @NonNull String courseClassificationKey,
					 @NonNull String courseCode,
					 int courseIdentityCodeUid,
					 @NonNull String courseTypeKey,
					 float credits,
					 @NonNull String formattedCourseCode,
					 @NonNull List<Locale> instructionLanguages,
					 @NonNull String mainLanguageOfInstruction,
					 long organisationUid,
					 @NonNull Map<String, Map<Locale, String>> registrationConfigType,
					 float semesterHours,
					 @NonNull String semesterKey,
					 @NonNull Map<String, Map<Locale, String>> title) {
}
