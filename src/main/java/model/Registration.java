package model;

public record Registration(
        int id,
        int eventId,
        String eventTitle,
        String sportName,
        String eventDate,
        String venue,
        String status,
        String registeredAt
) { }
