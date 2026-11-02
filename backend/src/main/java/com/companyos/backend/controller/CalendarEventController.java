package com.companyos.backend.controller;

import com.companyos.backend.dto.CalendarEventRequestDTO;
import com.companyos.backend.dto.CalendarEventResponseDTO;
import com.companyos.backend.service.CalendarEventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/calendar/events", "/api/calendar-events"})
public class CalendarEventController {
    private final CalendarEventService service;
    public CalendarEventController(CalendarEventService service) { this.service = service; }

    @GetMapping
    public List<CalendarEventResponseDTO> list(@RequestParam Instant from, @RequestParam Instant to) {
        return service.list(from, to);
    }
    @GetMapping("/{id}")
    public CalendarEventResponseDTO get(@PathVariable Long id) { return service.get(id); }
    @PostMapping
    public ResponseEntity<CalendarEventResponseDTO> create(@Valid @RequestBody CalendarEventRequestDTO request) {
        return ResponseEntity.status(201).body(service.create(request));
    }
    @PutMapping("/{id}")
    public CalendarEventResponseDTO update(@PathVariable Long id, @Valid @RequestBody CalendarEventRequestDTO request) {
        return service.update(id, request);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }

    /** Scope parse errors to Calendar rather than changing existing auth error behavior. */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<Map<String, Object>> invalidRequest(Exception ignored) {
        return ResponseEntity.badRequest().body(Map.of("status", 400, "error", "Invalid Calendar request: check required fields and ISO-8601 timestamps"));
    }
}
