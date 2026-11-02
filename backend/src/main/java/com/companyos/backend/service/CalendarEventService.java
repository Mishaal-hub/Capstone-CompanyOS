package com.companyos.backend.service;

import com.companyos.backend.dto.CalendarEventRequestDTO;
import com.companyos.backend.dto.CalendarEventResponseDTO;
import java.time.Instant;
import java.util.List;

public interface CalendarEventService {
    List<CalendarEventResponseDTO> list(Instant from, Instant to);
    CalendarEventResponseDTO get(Long id);
    CalendarEventResponseDTO create(CalendarEventRequestDTO request);
    CalendarEventResponseDTO update(Long id, CalendarEventRequestDTO request);
    void delete(Long id);
}
