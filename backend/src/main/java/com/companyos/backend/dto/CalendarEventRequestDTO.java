package com.companyos.backend.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

/** Identity, organization, IDs and audit timestamps are never request fields. */
public record CalendarEventRequestDTO(
        @NotBlank @Size(max = 150) String title,
        @Size(max = 5000) String description,
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        @NotBlank @Size(max = 100) String timeZone,
        @NotNull @Pattern(regexp = "MEETING|DEADLINE|FOCUS|OTHER") String eventType,
        @NotNull @Pattern(regexp = "SCHEDULED|COMPLETED|CANCELLED") String status,
        @Size(max = 255) String location) {}
