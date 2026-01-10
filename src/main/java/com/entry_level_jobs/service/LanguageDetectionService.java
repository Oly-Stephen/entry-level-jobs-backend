package com.entry_level_jobs.service;

import com.entry_level_jobs.language.JobLanguage;
import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Wraps Lingua language detection for reuse across classification components.
 */
@Service
@Slf4j
public class LanguageDetectionService {
    private final LanguageDetector detector;

    public LanguageDetectionService() {
        this.detector = LanguageDetectorBuilder.fromLanguages(
                Language.ENGLISH,
                Language.POLISH,
                Language.GERMAN,
                Language.SPANISH,
                Language.FRENCH)
                .build();
    }

    public JobLanguage detectLanguage(String text) {
        if (text == null || text.isBlank()) {
            return JobLanguage.EN;
        }
        try {
            Language detected = detector.detectLanguageOf(text);
            return mapLanguage(detected);
        } catch (Exception ex) {
            log.debug("Falling back to English after detection failure", ex);
            return JobLanguage.EN;
        }
    }

    private JobLanguage mapLanguage(Language detected) {
        if (detected == null) {
            return JobLanguage.EN;
        }
        return switch (detected) {
            case POLISH -> JobLanguage.PL;
            case GERMAN -> JobLanguage.DE;
            case SPANISH -> JobLanguage.ES;
            case FRENCH -> JobLanguage.FR;
            default -> JobLanguage.EN;
        };
    }
}
