package com.companyos.backend.service.impl;

import com.companyos.backend.dto.CalendarEventRequestDTO;
import com.companyos.backend.dto.CalendarEventResponseDTO;
import com.companyos.backend.entity.CalendarEvent;
import com.companyos.backend.entity.User;
import com.companyos.backend.exception.ResourceNotFoundException;
import com.companyos.backend.repository.CalendarEventRepository;
import com.companyos.backend.repository.UserRepository;
import com.companyos.backend.security.TenantContext;
import com.companyos.backend.service.AuditService;
import com.companyos.backend.service.CalendarEventService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class CalendarEventServiceImpl implements CalendarEventService {
    private final CalendarEventRepository events;
    private final UserRepository users;
    private final AuditService audit;

    public CalendarEventServiceImpl(CalendarEventRepository events, UserRepository users, AuditService audit) {
        this.events = events; this.users = users; this.audit = audit;
    }

    private User actor() {
        Long orgId = TenantContext.getRequiredTenantId();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new AccessDeniedException("Authentication required");
        User user = users.findByUsername(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Active workspace user required"));
        if (!Objects.equals(orgId, user.getOrganizationId()) || !"ACTIVE".equals(user.getStatus()))
            throw new AccessDeniedException("Active workspace membership required");
        return user;
    }

    private CalendarEvent requireEvent(Long id, Long orgId) {
        return events.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Calendar event not found"));
    }

    @Override
    public List<CalendarEventResponseDTO> list(Instant from, Instant to) {
        User user = actor();
        if (from == null || to == null || !to.isAfter(from) || Duration.between(from, to).compareTo(Duration.ofDays(366)) > 0)
            throw new IllegalArgumentException("Provide a valid date range of no more than 366 days");
        return events.findOverlapping(user.getOrganizationId(), from, to).stream()
                .map(CalendarEventResponseDTO::from).toList();
    }

    @Override
    public CalendarEventResponseDTO get(Long id) {
        return CalendarEventResponseDTO.from(requireEvent(id, actor().getOrganizationId()));
    }

    @Override @Transactional
    public CalendarEventResponseDTO create(CalendarEventRequestDTO request) {
        User user = actor();
        CalendarEvent event = new CalendarEvent();
        event.setOrganizationId(user.getOrganizationId());
        event.setCreatedBy(user.getUserId());
        event.setUsername(user.getUsername()); // compatibility with existing NOT NULL column
        apply(event, request);
        event = events.saveAndFlush(event);
        audit.log("CREATED", "CalendarEvent", event.getId(), "Created calendar event: " + event.getTitle());
        return CalendarEventResponseDTO.from(event);
    }

    @Override @Transactional
    public CalendarEventResponseDTO update(Long id, CalendarEventRequestDTO request) {
        CalendarEvent event = requireEvent(id, actor().getOrganizationId());
        apply(event, request);
        event = events.saveAndFlush(event);
        audit.log("UPDATED", "CalendarEvent", event.getId(), "Updated calendar event: " + event.getTitle());
        return CalendarEventResponseDTO.from(event);
    }

    @Override @Transactional
    public void delete(Long id) {
        CalendarEvent event = requireEvent(id, actor().getOrganizationId());
        events.delete(event);
        audit.log("DELETED", "CalendarEvent", id, "Deleted calendar event: " + event.getTitle());
    }

    private void apply(CalendarEvent e, CalendarEventRequestDTO r) {
        if (r.title() == null || r.title().isBlank() || r.title().length() > 150)
            throw new IllegalArgumentException("Event title is required (maximum 150 characters)");
        if (r.startTime() == null || r.endTime() == null || !r.endTime().isAfter(r.startTime()))
            throw new IllegalArgumentException("Event end must be after its start");
        if (r.timeZone() == null || !ZoneId.getAvailableZoneIds().contains(r.timeZone()))
            throw new IllegalArgumentException("Use a valid IANA time zone");
        if (!Set.of("MEETING", "DEADLINE", "FOCUS", "OTHER").contains(r.eventType() == null ? "" : r.eventType()))
            throw new IllegalArgumentException("Invalid event category");
        if (!Set.of("SCHEDULED", "COMPLETED", "CANCELLED").contains(r.status() == null ? "" : r.status()))
            throw new IllegalArgumentException("Invalid event status");
        e.setTitle(r.title().trim()); e.setDescription(r.description());
        e.setStartTime(r.startTime()); e.setEndTime(r.endTime()); e.setTimeZone(r.timeZone());
        e.setEventType(r.eventType()); e.setStatus(r.status()); e.setLocation(r.location());
    }
}
