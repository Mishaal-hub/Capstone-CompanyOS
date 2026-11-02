package com.companyos.backend.repository;

import com.companyos.backend.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    Optional<CalendarEvent> findByIdAndOrganizationId(Long id, Long organizationId);

    /** Half-open [from,to) overlap, including events spanning midnight/month boundaries. */
    @Query("select e from CalendarEvent e where e.organizationId = :organizationId "
            + "and e.startTime < :to and e.endTime > :from order by e.startTime, e.id")
    List<CalendarEvent> findOverlapping(@Param("organizationId") Long organizationId,
            @Param("from") Instant from, @Param("to") Instant to);
}
