package model;

public record Event(
        int id,
        int sportId,
        String sportName,
        String title,
        String description,
        String eventDate,
        String venue,
        String status
) { }
