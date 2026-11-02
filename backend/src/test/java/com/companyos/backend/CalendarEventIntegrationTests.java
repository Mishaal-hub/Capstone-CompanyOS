package com.companyos.backend;

import com.companyos.backend.entity.CalendarEvent;
import com.companyos.backend.entity.User;
import com.companyos.backend.repository.CalendarEventRepository;
import com.companyos.backend.repository.UserRepository;
import com.companyos.backend.repository.AuditLogRepository;
import com.companyos.backend.util.JwtUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:calendar_test;DB_CLOSE_DELAY=-1;MODE=MySQL;NON_KEYWORDS=VALUE",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false",
    "spring.jpa.properties.hibernate.hbm2ddl.halt_on_error=true",
    "spring.mail.host=localhost", "spring.mail.username=test", "spring.mail.password=test",
    "jwt.secret=calendar-test-signing-key-not-for-production-at-least-32-characters"
})
class CalendarEventIntegrationTests {
    @LocalServerPort int port;
    @Autowired CalendarEventRepository events;
    @Autowired UserRepository users;
    @Autowired AuditLogRepository audits;
    @Autowired JwtUtil jwt;
    private String tokenA, tokenB, tokenPeer;
    private User userA;
    private final HttpClient http = HttpClient.newHttpClient();
    private static final String BASE = "/api/calendar/events";
    private static final String RANGE = "?from=2026-10-01T00:00:00Z&to=2026-11-01T00:00:00Z";
    private static final String BODY = """
        {"title":" Team planning ","description":"Calendar MVP","startTime":"2026-10-21T03:30:00Z",
        "endTime":"2026-10-21T04:30:00Z","timeZone":"Asia/Kolkata","eventType":"MEETING",
        "status":"SCHEDULED","location":"Room 1"}
        """;

    @BeforeEach void setup() {
        audits.deleteAll(); events.deleteAll(); users.deleteAll();
        userA = user("calendar-a@test.invalid", 101L);
        User b = user("calendar-b@test.invalid", 202L), peer = user("calendar-peer@test.invalid", 101L);
        tokenA = jwt.generateToken(userA.getUserId(), userA.getUsername(), "MEMBER", 101L);
        tokenB = jwt.generateToken(b.getUserId(), b.getUsername(), "MEMBER", 202L);
        tokenPeer = jwt.generateToken(peer.getUserId(), peer.getUsername(), "MEMBER", 101L);
    }
    private User user(String name, Long org) {
        User u = new User(); u.setUsername(name); u.setOrganizationId(org); u.setRole("MEMBER");
        u.setVerified(true); return users.saveAndFlush(u);
    }
    private HttpResponse<String> call(String method, String url, String token, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + url))
                .header("Content-Type", "application/json");
        if (token != null) request.header("Authorization", "Bearer " + token);
        request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
    private long create() throws Exception {
        var result = call("POST", BASE, tokenA, BODY);
        assertEquals(201, result.statusCode(), result.body());
        // Response is compact JSON; use persisted row rather than assuming serialization field order.
        assertEquals(1, events.count());
        return events.findAll().getFirst().getId();
    }

    @Test void anonymousAndInvalidTokensAreRejectedForAllOperations() throws Exception {
        for (String method : new String[]{"GET", "POST", "PUT", "DELETE"}) {
            String url = method.equals("GET") ? BASE + RANGE : method.equals("POST") ? BASE : BASE + "/1";
            assertEquals(401, call(method, url, null, method.equals("POST") || method.equals("PUT") ? BODY : null).statusCode());
        }
        assertEquals(401, call("GET", BASE + RANGE, "not-a-valid-token", null).statusCode());
    }
    @Test void createPersistsUtcAuditAndServerOwnedIdentityDespiteSpoofing() throws Exception {
        String spoof = """
            {"organizationId":202,"createdBy":999,"username":"attacker",
            """ + BODY.strip().substring(1);
        var result = call("POST", BASE, tokenA, spoof);
        assertEquals(201, result.statusCode(), result.body());
        CalendarEvent e = events.findAll().getFirst();
        assertEquals(101L, e.getOrganizationId()); assertEquals(userA.getUserId(), e.getCreatedBy());
        assertEquals(userA.getUsername(), e.getUsername()); assertEquals("Team planning", e.getTitle());
        assertEquals(Instant.parse("2026-10-21T03:30:00Z"), e.getStartTime());
        assertEquals("Asia/Kolkata", e.getTimeZone()); assertNotNull(e.getCreatedAt()); assertEquals(1, audits.count());
    }
    @Test void listIsTenantScopedAndSharedWithinWorkspace() throws Exception {
        long id = create();
        assertTrue(call("GET", BASE + RANGE, tokenPeer, null).body().contains("Team planning"));
        assertEquals("[]", call("GET", BASE + RANGE + "&username=calendar-a@test.invalid", tokenB, null).body());
        assertEquals(200, call("GET", BASE + "/" + id, tokenPeer, null).statusCode());
    }
    @Test void foreignGetUpdateAndDeleteDoNotRevealOrModifyRows() throws Exception {
        long id = create();
        for (String method : new String[]{"GET", "PUT", "DELETE"})
            assertEquals(404, call(method, BASE + "/" + id, tokenB, method.equals("PUT") ? BODY : null).statusCode());
        assertEquals(1, events.count()); assertEquals(1, audits.count());
    }
    @Test void updatePreservesIdentityAndDeleteIsAudited() throws Exception {
        long id = create(); var original = events.findById(id).orElseThrow();
        var update = call("PUT", BASE + "/" + id, tokenPeer, BODY.replace("Team planning", "Updated planning"));
        assertEquals(200, update.statusCode(), update.body());
        var after = events.findById(id).orElseThrow();
        assertEquals(original.getCreatedBy(), after.getCreatedBy()); assertEquals(original.getCreatedAt(), after.getCreatedAt());
        assertEquals(204, call("DELETE", BASE + "/" + id, tokenA, null).statusCode());
        assertEquals(404, call("GET", BASE + "/" + id, tokenA, null).statusCode());
        assertEquals(0, events.count()); assertEquals(3, audits.count());
    }
    @Test void invalidFieldsAndRangesReturn400() throws Exception {
        for (String body : new String[]{
            BODY.replace(" Team planning ", " "), BODY.replace(" Team planning ", "x".repeat(151)),
            BODY.replace("2026-10-21T04:30:00Z", "2026-10-21T03:30:00Z"),
            BODY.replace("2026-10-21T04:30:00Z", "2026-10-20T03:30:00Z"),
            BODY.replace("Asia/Kolkata", "Mars/Nowhere"), BODY.replace("MEETING", "INVALID"),
            BODY.replace("SCHEDULED", "INVALID"), BODY.replace("2026-10-21T03:30:00Z", "not-a-date"),
            "{}", "{broken-json"}) {
            var result = call("POST", BASE, tokenA, body);
            assertEquals(400, result.statusCode(), result.body());
        }
        assertEquals(0, events.count());
        for (String query : new String[]{"", "?from=bad&to=bad", "?from=2026-11-01T00:00:00Z&to=2026-10-01T00:00:00Z",
                "?from=2026-01-01T00:00:00Z&to=2028-01-01T00:00:00Z"})
            assertEquals(400, call("GET", BASE + query, tokenA, null).statusCode());
    }
    @Test void eventsOverlappingRangeAreIncludedAndBoundaryAdjacentEventsExcluded() throws Exception {
        create();
        assertTrue(call("GET", BASE + "?from=2026-10-21T04:00:00Z&to=2026-10-21T05:00:00Z", tokenA, null).body().contains("Team planning"));
        assertEquals("[]", call("GET", BASE + "?from=2026-10-21T04:30:00Z&to=2026-10-21T05:00:00Z", tokenA, null).body());
        assertEquals("[]", call("GET", BASE + "?from=2026-10-21T02:00:00Z&to=2026-10-21T03:30:00Z", tokenA, null).body());
    }
    @Test void missingTenantAndMismatchedMembershipFailClosed() throws Exception {
        String missing = jwt.generateToken(userA.getUserId(), userA.getUsername(), "MEMBER", null);
        String mismatch = jwt.generateToken(userA.getUserId(), userA.getUsername(), "MEMBER", 202L);
        assertEquals(403, call("GET", BASE + RANGE, missing, null).statusCode());
        assertEquals(403, call("POST", BASE, mismatch, BODY).statusCode());
        assertEquals(0, events.count());
    }
    @Test void legacyRowsWithoutTenantRemainQuarantined() throws Exception {
        CalendarEvent legacy = new CalendarEvent(); legacy.setUsername(userA.getUsername()); legacy.setTitle("Legacy private row");
        legacy.setStartTime(Instant.parse("2026-10-21T03:30:00Z")); legacy.setEndTime(Instant.parse("2026-10-21T04:30:00Z"));
        legacy = events.saveAndFlush(legacy);
        assertEquals("[]", call("GET", BASE + RANGE, tokenA, null).body());
        assertEquals(404, call("GET", BASE + "/" + legacy.getId(), tokenA, null).statusCode());
        assertEquals(404, call("DELETE", BASE + "/" + legacy.getId(), tokenA, null).statusCode());
        assertEquals(1, events.count());
    }
    @Test void canonicalAliasAndOffsetTimestampInputsWork() throws Exception {
        String offset = BODY.replace("2026-10-21T03:30:00Z", "2026-10-21T09:00:00+05:30")
                .replace("2026-10-21T04:30:00Z", "2026-10-21T10:00:00+05:30");
        var result = call("POST", "/api/calendar-events", tokenA, offset);
        assertEquals(201, result.statusCode(), result.body());
        assertEquals(Instant.parse("2026-10-21T03:30:00Z"), events.findAll().getFirst().getStartTime());
    }
    @Test void existingPasswordLoginStillIssuesAUsableTenantToken() throws Exception {
        userA.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("Preview-test-password-42!"));
        users.saveAndFlush(userA);
        var login = call("POST", "/api/auth/login", null, """
            {"username":"calendar-a@test.invalid","password":"Preview-test-password-42!"}
            """);
        assertEquals(200, login.statusCode(), login.body());
        var json = new tools.jackson.databind.json.JsonMapper().readTree(login.body());
        String issuedToken = json.get("token").asText();
        assertTrue(jwt.isTokenValid(issuedToken));
        assertEquals(101L, jwt.extractOrganizationId(issuedToken));
        assertEquals(200, call("GET", BASE + RANGE, issuedToken, null).statusCode());
        var wrongPassword = call("POST", "/api/auth/login", null, """
            {"username":"calendar-a@test.invalid","password":"wrong-password"}
            """);
        assertEquals(400, wrongPassword.statusCode());
    }

}
