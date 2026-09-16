package com.teamflow.ai.ai.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Deterministic, dependency-free {@link AiProvider}.
 *
 * <p>Unconditionally registered — unlike {@code GeminiAiProvider}/{@code OpenAiProvider}/
 * {@code ClaudeAiProvider}, which only exist when {@code teamflow.ai.provider} selects
 * them. Every cloud provider takes this class as a constructor-injected fallback, so it
 * must always be available as a bean regardless of which provider is configured as
 * primary; when no cloud provider is configured, it is also the sole {@link AiProvider}
 * implementation and therefore the one Spring wires everywhere. Renders explanations by
 * substituting {@code {placeholder}} tokens, so the reasoning shown to a manager is
 * derived from the same numbers the scoring engine used rather than from a generative
 * model that might describe factors it did not actually weigh.
 */
@Slf4j
@Component
public class RuleBasedAiProvider implements AiProvider {

    @Override
    public String name() {
        return "rule-based";
    }

    @Override
    public String explain(String template, Map<String, Object> variables) {
        String rendered = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            rendered = rendered.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return rendered;
    }

    /** Always available: no network dependency, so a demo cannot be broken by one. */
    @Override
    public boolean isAvailable() {
        return true;
    }
}
