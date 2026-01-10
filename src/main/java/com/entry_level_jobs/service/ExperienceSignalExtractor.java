package com.entry_level_jobs.service;

import com.entry_level_jobs.dto.classification.ExperienceSignal;
import com.entry_level_jobs.dto.classification.ExperienceSignalType;
import com.entry_level_jobs.language.JobLanguage;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Uses Apache OpenNLP tokenization and regex heuristics to extract structured
 * experience signals (e.g., "3+ years" or "internship").
 */
@Service
@Slf4j
public class ExperienceSignalExtractor {
    private static final Pattern ENTRY_LEVEL_YEARS_PATTERN = Pattern.compile(
            "(?i)\\b0\\s*[-–]?\\s*1\\s*(year|years|yr|yrs|rok|lata|jahr|jahre|año|años|an|ans)\\b");
    private static final Pattern MID_LEVEL_YEARS_PATTERN = Pattern.compile(
            "(?i)\\b(2)\\s*[-–]?\\s*3\\s*(year|years|yr|yrs|rok|lata|jahr|jahre|año|años|an|ans)\\b");
    private static final Pattern SENIOR_YEARS_PATTERN = Pattern.compile(
            "(?i)\\b([3-9]|1[0-9])\\+?\\s*(year|years|yr|yrs|rok|lata|latach|jahr|jahre|año|años|an|ans)\\b");

    private static final Set<String> ENTRY_LEVEL_TOKENS = Set.of(
            "internship", "intern", "trainee", "apprentice", "stage", "stagiaire",
            "prácticas", "becario", "praktikum", "praktikant", "ausbildung",
            "einsteiger", "młodszy", "staż", "junior", "graduate", "recién", "debutant",
            "débutant", "bez", "doświadczenia");

    private static final Set<String> SENIOR_TOKENS = Set.of(
            "senior", "lead", "manager", "principal", "staff", "leiter", "gerente",
            "kierownik", "experienced", "doświadczony", "experiencia", "expérimenté");

    private static final int ENTRY_LEVEL_TOKEN_WEIGHT = 4;
    private static final int SENIOR_TOKEN_WEIGHT = -6;
    private static final int ENTRY_LEVEL_YEAR_WEIGHT = 6;
    private static final int MID_LEVEL_YEAR_WEIGHT = -2;
    private static final int SENIOR_YEAR_WEIGHT = -10;

    public List<ExperienceSignal> extractSignals(String text, JobLanguage language) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String normalized = normalize(text);
        List<ExperienceSignal> signals = new ArrayList<>();

        collectPatternMatches(normalized, ENTRY_LEVEL_YEARS_PATTERN, ExperienceSignalType.ENTRY_LEVEL_HINT,
                ENTRY_LEVEL_YEAR_WEIGHT, signals);
        collectPatternMatches(normalized, MID_LEVEL_YEARS_PATTERN, ExperienceSignalType.SENIOR_REQUIREMENT,
                MID_LEVEL_YEAR_WEIGHT, signals);
        collectPatternMatches(normalized, SENIOR_YEARS_PATTERN, ExperienceSignalType.SENIOR_REQUIREMENT,
                SENIOR_YEAR_WEIGHT, signals);

        addTokenSignals(normalized, signals);
        log.trace("Extracted {} signals for detected language {}", signals.size(), language);
        return signals;
    }

    private void collectPatternMatches(String text, Pattern pattern, ExperienceSignalType type,
            int weight, List<ExperienceSignal> signals) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            signals.add(ExperienceSignal.builder()
                    .phrase(matcher.group().trim())
                    .type(type)
                    .weight(weight)
                    .build());
        }
    }

    private void addTokenSignals(String normalizedText, List<ExperienceSignal> signals) {
        String[] tokens = SimpleTokenizer.INSTANCE.tokenize(normalizedText);
        Set<String> seen = new HashSet<>();
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (ENTRY_LEVEL_TOKENS.contains(trimmed) && seen.add("entry:" + trimmed)) {
                signals.add(ExperienceSignal.builder()
                        .phrase(trimmed)
                        .type(ExperienceSignalType.ENTRY_LEVEL_HINT)
                        .weight(ENTRY_LEVEL_TOKEN_WEIGHT)
                        .build());
            } else if (SENIOR_TOKENS.contains(trimmed) && seen.add("senior:" + trimmed)) {
                signals.add(ExperienceSignal.builder()
                        .phrase(trimmed)
                        .type(ExperienceSignalType.SENIOR_REQUIREMENT)
                        .weight(SENIOR_TOKEN_WEIGHT)
                        .build());
            }
        }
    }

    private String normalize(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return normalized;
    }
}
