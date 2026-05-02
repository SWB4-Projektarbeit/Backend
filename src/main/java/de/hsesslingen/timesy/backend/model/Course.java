package de.hsesslingen.timesy.backend.model;

import lombok.Data;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Data
public class Course {
    private final long uid;
    private final boolean blocked;
    private final String courseClassificationKey;
    private final String courseCode;
    private final int courseIdentityCodeUid;
    private final String courseTypeKey;
    private final float credits;
    private final String formattedCourseCode;
    private final List<Locale> instructionLanguages;
    private final String mainLanguageOfInstruction;
    private final long organisationUid;
    private final Map<String, Map<Locale, String>> registrationConfigType;
    private final float semesterHours;
    private final String semesterKey;
    private final Map<String, Map<Locale, String>> title;
}
