package de.hsesslingen.timesy.backend.model;

import lombok.Data;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Data
public class Course {
    private final long uid;
    private final boolean blocked;
    private final String course_classification_key;
    private final String course_code;
    private final int course_identity_code_uid;
    private final String course_type_key;
    private final float credits;
    private final String formatted_course_code;
    private final List<Locale> instruction_languages;
    private final String main_language_of_instructions;
    private final long organisation_uid;
    private final Map<String, Map<Locale, String>> registration_config_type;
    private final float semester_hours;
    private final String semester_key;
    private final Map<String, Map<Locale, String>> title;
}
