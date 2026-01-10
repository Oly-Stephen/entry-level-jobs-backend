package com.entry_level_jobs.language;

import java.util.Map;

/**
 * Keyword weights for a specific language. Positive weights boost entry-level
 * confidence, negative weights subtract when senior-level cues are present.
 */
public class JobLanguagePack {
    public final Map<String, Integer> positive;
    public final Map<String, Integer> negative;

    public JobLanguagePack(Map<String, Integer> pos, Map<String, Integer> neg) {
        this.positive = pos;
        this.negative = neg;
    }
}
