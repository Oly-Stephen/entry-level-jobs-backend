package com.entry_level_jobs.language;

import java.util.Map;

/**
 * Hardcoded keyword packs per supported language. Provides instant access to
 * positive/negative indicator weights without additional storage or lookups.
 */
public final class LanguageKeywordRepository {
    private LanguageKeywordRepository() {
    }

    public static final Map<JobLanguage, JobLanguagePack> PACKS = Map.of(
            JobLanguage.EN, new JobLanguagePack(
                    Map.ofEntries(
                            Map.entry("junior", 4), Map.entry("entry level", 5),
                            Map.entry("intern", 6), Map.entry("trainee", 5),
                            Map.entry("graduate", 4), Map.entry("no experience", 7),
                            Map.entry("0-1 year", 8), Map.entry("recent graduate", 6)),
                    Map.ofEntries(
                            Map.entry("senior", -8), Map.entry("lead", -8),
                            Map.entry("manager", -7), Map.entry("3+ years", -8),
                            Map.entry("5+ years", -12), Map.entry("experienced", -6))),
            JobLanguage.PL, new JobLanguagePack(
                    Map.ofEntries(
                            Map.entry("junior", 5), Map.entry("młodszy", 5),
                            Map.entry("praktykant", 7), Map.entry("staż", 7),
                            Map.entry("bez doświadczenia", 8),
                            Map.entry("0-1 rok", 8), Map.entry("absolwent", 6)),
                    Map.ofEntries(
                            Map.entry("senior", -9), Map.entry("starszy", -9),
                            Map.entry("kierownik", -7), Map.entry("3+ lata", -8),
                            Map.entry("doświadczony", -7))),
            JobLanguage.DE, new JobLanguagePack(
                    Map.ofEntries(
                            Map.entry("junior", 5), Map.entry("einsteiger", 6),
                            Map.entry("praktikum", 7), Map.entry("praktikant", 7),
                            Map.entry("ausbildung", 7), Map.entry("0-1 jahr", 8)),
                    Map.ofEntries(
                            Map.entry("senior", -9), Map.entry("leiter", -8),
                            Map.entry("3+ jahre", -8), Map.entry("mehrjährige", -8))),
            JobLanguage.ES, new JobLanguagePack(
                    Map.ofEntries(
                            Map.entry("junior", 5), Map.entry("prácticas", 7),
                            Map.entry("becario", 7), Map.entry("sin experiencia", 8),
                            Map.entry("0-1 año", 8), Map.entry("recién graduado", 6)),
                    Map.ofEntries(
                            Map.entry("senior", -9), Map.entry("gerente", -8),
                            Map.entry("3+ años", -8), Map.entry("experiencia mínima", -7))),
            JobLanguage.FR, new JobLanguagePack(
                    Map.ofEntries(
                            Map.entry("junior", 5), Map.entry("stage", 7),
                            Map.entry("stagiaire", 7), Map.entry("débutant", 6),
                            Map.entry("sans expérience", 8), Map.entry("0-1 an", 8)),
                    Map.ofEntries(
                            Map.entry("senior", -9), Map.entry("manager", -8),
                            Map.entry("3+ ans", -8), Map.entry("expérimenté", -7))));
}
