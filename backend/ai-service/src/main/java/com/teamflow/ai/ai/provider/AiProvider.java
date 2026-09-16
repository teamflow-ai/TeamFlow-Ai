package com.teamflow.ai.ai.provider;

import java.util.Map;

/**
 * The seam that keeps AI features swappable.
 *
 * <p>Every intelligent feature is expressed against this interface rather than
 * against any particular model vendor. The shipped implementation is a
 * deterministic rule engine, which matters for three practical reasons: it needs no
 * API key, it cannot fail because a network call timed out mid-demo, and its output
 * is explainable — a viva examiner can be walked through exactly why an employee
 * was recommended.
 *
 * <p>A Spring AI adapter implementing this same interface can be activated later by
 * configuration alone, with no change to any calling service.
 */
public interface AiProvider {

    /** Identifier reported in responses so callers know which engine produced a result. */
    String name();

    /**
     * Produces a natural-language rationale for a decision.
     *
     * @param template  prompt or template key
     * @param variables values substituted into the template
     */
    String explain(String template, Map<String, Object> variables);

    /** True when this provider can currently serve requests. */
    boolean isAvailable();
}
