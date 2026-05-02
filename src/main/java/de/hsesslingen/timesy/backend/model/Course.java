package de.hsesslingen.timesy.backend.model;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * JSON DTO from HE Online API
 */
public record Course(long uid, boolean blocked, String courseClassificationKey, String courseCode,
                     int courseIdentityCodeUid, String courseTypeKey, float credits, String formattedCourseCode,
                     List<Locale> instructionLanguages, String mainLanguageOfInstruction, long organisationUid,
                     Map<String, Map<Locale, String>> registrationConfigType, float semesterHours, String semesterKey,
                     Map<String, Map<Locale, String>> title) {
}
