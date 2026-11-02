package com.companyos.backend.dto;

import com.companyos.backend.entity.CalendarEvent;
import java.time.Instant;

public record CalendarEventResponseDTO(Long id, Long organizationId, Long createdBy,
        String title, String description, Instant startTime, Instant endTime,
        String timeZone, String eventType, String status, String location,
        Instant createdAt, Instant updatedAt) {
    public static CalendarEventResponseDTO from(CalendarEvent e) {
        return new CalendarEventResponseDTO(e.getId(), e.getOrganizationId(), e.getCreatedBy(),
                e.getTitle(), e.getDescription(), e.getStartTime(), e.getEndTime(),
                e.getTimeZone(), e.getEventType(), e.getStatus(), e.getLocation(),
                e.getCreatedAt(), e.getUpdatedAt());
    }
}
