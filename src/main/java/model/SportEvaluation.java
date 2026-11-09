package model;

public record SportEvaluation(
        int id,
        String sportName,
        String result,
        String evaluatedBy,
        String evaluatedAt
) {
}