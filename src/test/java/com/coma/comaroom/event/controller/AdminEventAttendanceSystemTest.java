package com.coma.comaroom.event.controller;

import com.coma.comaroom.auth.jwt.JwtTokenProvider;
import com.coma.comaroom.event.dto.response.AttendanceItemResponseDto;
import com.coma.comaroom.event.dto.response.EventAttendanceResponse;
import com.coma.comaroom.event.entity.Event;
import com.coma.comaroom.event.entity.EventCategory;
import com.coma.comaroom.event.entity.EventParticipant;
import com.coma.comaroom.event.repository.EventParticipateRepository;
import com.coma.comaroom.event.repository.EventRepository;
import com.coma.comaroom.event.service.AdminEventService;
import com.coma.comaroom.member.entity.Major;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "app.security.cors.allowed-origins=http://localhost",
        "spring.jpa.open-in-view=false",
        "storage.oci.region=test", "storage.oci.namespace=test", "storage.oci.bucket=test",
        "storage.oci.object-base-url=https://example.invalid"
})
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "ATTENDANCE_TEST_MYSQL_PORT", matches = "[0-9]{1,5}")
class AdminEventAttendanceSystemTest {

    private static final String ATTENDANCE_ENDPOINT = "/api/admin/event/attendances";
    private static final String ROSTER_ENDPOINT = ATTENDANCE_ENDPOINT + "/all";
    private static final LocalDateTime EVENT_DATE = LocalDateTime.of(2026, 1, 1, 12, 0);
    private static final String PASSWORD = "attendance-test-password";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    @LocalServerPort private int port;
    @Autowired private AdminEventService service;
    @Autowired private EventRepository events;
    @Autowired private EventParticipateRepository participants;
    @Autowired private MemberRepository members;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider tokens;
    @Autowired private ObjectMapper json;
    @Autowired private TransactionTemplate transactions;
    @MockitoBean private StringRedisTemplate redisTemplate;

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build();

    @DynamicPropertySource
    static void mysql(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:mysql://127.0.0.1:"
                + System.getenv("ATTENDANCE_TEST_MYSQL_PORT")
                + "/comaroom_attendance_test?createDatabaseIfNotExist=true");
        registry.add("spring.datasource.username", () -> System.getenv().getOrDefault("ATTENDANCE_TEST_MYSQL_USER", "root"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("ATTENDANCE_TEST_MYSQL_PASSWORD", ""));
    }

    @AfterEach
    void clearTestSchema() {
        participants.deleteAllInBatch();
        events.deleteAllInBatch();
        members.deleteAllInBatch();
    }

    @Test
    void groupsPersistedAttendancesByEventAndSortsByDate() {
        Member member = saveMember("attendee", Role.USER);
        Event newer = saveEvent(member, "같은 제목", EVENT_DATE.plusDays(1));
        Event older = saveEvent(member, "같은 제목", EVENT_DATE);
        saveAttendance(older, member);

        List<EventAttendanceResponse> result = service.getAllAttendanceLists();

        assertThat(result).extracting(EventAttendanceResponse::eventId)
                .containsExactly(newer.getEventId(), older.getEventId());
        assertThat(result.getFirst().attendances()).isEmpty();
        assertThat(result.getLast().attendances()).extracting(AttendanceItemResponseDto::getMemberId)
                .containsExactly(member.getMemberId());
    }

    @Test
    void adminCanLogInAndReadAnEmptyThenPopulatedRoster() throws Exception {
        Member admin = saveMember("admin", Role.ADMIN);
        HttpRequest login = HttpRequest.newBuilder(uri("/api/auth/login"))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("studentId=admin&password=" + PASSWORD))
                .build();
        HttpResponse<String> loginResponse = client.send(login, HttpResponse.BodyHandlers.ofString());
        assertThat(loginResponse.statusCode()).isEqualTo(200);
        String accessToken = json.readTree(loginResponse.body()).path("data").path("accessToken").asString();
        assertThat(accessToken).isNotBlank();
        assertThat(roster(accessToken)).isEmpty();

        Member attendee = saveMember("attendee", Role.USER);
        Event older = saveEvent(admin, "빈 명단", EVENT_DATE);
        Event newer = saveEvent(admin, "출석 명단", EVENT_DATE.plusDays(1));
        saveAttendance(newer, attendee);

        JsonNode data = roster(accessToken);
        assertThat(data.size()).isEqualTo(2);
        assertThat(data.get(0).path("eventId").asLong()).isEqualTo(newer.getEventId());
        assertThat(data.get(0).path("title").asString()).isEqualTo("출석 명단");
        JsonNode row = data.get(0).path("attendances").get(0);
        assertThat(row.path("memberId").asLong()).isEqualTo(attendee.getMemberId());
        assertThat(row.path("name").asString()).isEqualTo(attendee.getName());
        assertThat(row.path("studentId").asString()).isEqualTo(attendee.getStudentId());
        assertThat(row.path("major").asString()).isEqualTo(Major.COMPUTER_INFO.getMajor());
        assertThat(row.has("password") || row.has("role") || row.has("xp")).isFalse();
        assertThat(data.get(1).path("eventId").asLong()).isEqualTo(older.getEventId());
        assertThat(data.get(1).path("attendances").isArray()).isTrue();
        assertThat(data.get(1).path("attendances").isEmpty()).isTrue();
    }

    enum Credential { NONE, MALFORMED, REFRESH, USER }

    @ParameterizedTest(name = "{0} 인증 정보로 전체 명단 접근 거부")
    @EnumSource(Credential.class)
    void deniesCredentialsWithoutAdminAccess(Credential credential) throws Exception {
        Member admin = saveMember("admin", Role.ADMIN);
        Member user = saveMember("private-student-id", Role.USER);
        Event event = saveEvent(admin, "private-event-title", EVENT_DATE);
        saveAttendance(event, user);

        String token = switch (credential) {
            case NONE -> null;
            case MALFORMED -> "invalid-token";
            case REFRESH -> tokens.createRefreshToken(admin.getMemberId());
            case USER -> accessToken(user);
        };
        HttpResponse<String> response = get(ROSTER_ENDPOINT, token);

        assertThat(response.statusCode()).isEqualTo(credential == Credential.USER ? 403 : 401);
        assertThat(json.readTree(response.body()).has("data")).isFalse();
        assertThat(response.body()).doesNotContain("private-student-id", "private-event-title");
    }

    @Test
    void individualEventEndpointRetainsItsResponseAndNotFoundStatus() throws Exception {
        Member admin = saveMember("admin", Role.ADMIN);
        Event event = saveEvent(admin, "기존 출석 조회", EVENT_DATE);
        saveAttendance(event, admin);
        String token = accessToken(admin);

        HttpResponse<String> individual = get(ATTENDANCE_ENDPOINT + "/" + event.getEventId(), token);
        assertThat(individual.statusCode()).isEqualTo(200);
        assertThat(json.readTree(individual.body()).path("data"))
                .isEqualTo(roster(token).get(0).path("attendances"));
        HttpResponse<String> missing = get(ATTENDANCE_ENDPOINT + "/" + Long.MAX_VALUE, token);
        assertThat(missing.statusCode()).isEqualTo(400);
        assertThat(json.readTree(missing.body()).path("code").asString()).isEqualTo("EVENT-001");
    }

    @Test
    void concurrentReadsSeeOnlyCommittedAttendance() throws Exception {
        Member admin = saveMember("admin", Role.ADMIN);
        Event event = saveEvent(admin, "동시 조회", EVENT_DATE);
        String token = accessToken(admin);
        CountDownLatch inserted = new CountDownLatch(1);
        CountDownLatch allowCommit = new CountDownLatch(1);
        int readers = 8;

        try (var executor = Executors.newFixedThreadPool(readers + 1)) {
            var writer = executor.submit(() -> transactions.executeWithoutResult(transaction -> {
                participants.saveAndFlush(EventParticipant.builder().event(event).participantMember(admin).build());
                inserted.countDown();
                await(allowCommit);
            }));
            try {
                await(inserted);
                CountDownLatch readersReady = new CountDownLatch(readers);
                CountDownLatch startReads = new CountDownLatch(1);
                var results = IntStream.range(0, readers).mapToObj(index -> executor.submit(() -> {
                    readersReady.countDown();
                    await(startReads);
                    return roster(token);
                })).toList();
                await(readersReady);
                startReads.countDown();
                for (var result : results) {
                    JsonNode data = result.get(REQUEST_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
                    assertThat(data.size()).isEqualTo(1);
                    assertThat(data.get(0).path("eventId").asLong()).isEqualTo(event.getEventId());
                    assertThat(data.get(0).path("attendances").isEmpty()).isTrue();
                }
            } finally {
                allowCommit.countDown();
                writer.get(REQUEST_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            }
        }
        assertThat(roster(token).get(0).path("attendances").size()).isEqualTo(1);
        assertThat(participants.count()).isEqualTo(1);
    }

    @Test
    void returnsLargeRosterWithinLocalSmokeTestBudget() throws Exception {
        Member admin = saveMember("admin", Role.ADMIN);
        int eventCount = 100;
        int attendeesPerEvent = 20;
        transactions.executeWithoutResult(transaction -> {
            List<Member> attendees = members.saveAll(IntStream.range(0, attendeesPerEvent)
                    .mapToObj(index -> member("student-" + index, Role.USER, admin.getPassword())).toList());
            List<Event> savedEvents = events.saveAll(IntStream.range(0, eventCount)
                    .mapToObj(index -> event(admin, "이벤트 " + index, EVENT_DATE.plusDays(index))).toList());
            participants.saveAll(savedEvents.stream().flatMap(event -> attendees.stream()
                    .map(member -> EventParticipant.builder().event(event).participantMember(member).build())).toList());
        });
        String token = accessToken(admin);
        for (int index = 0; index < 3; index++) {
            assertLargeRoster(roster(token), eventCount, attendeesPerEvent);
        }
        List<Duration> samples = new ArrayList<>();
        for (int index = 0; index < 20; index++) {
            long start = System.nanoTime();
            JsonNode data = roster(token);
            samples.add(Duration.ofNanos(System.nanoTime() - start));
            assertLargeRoster(data, eventCount, attendeesPerEvent);
        }
        samples.sort(Duration::compareTo);
        Duration p95 = samples.get((int) Math.ceil(samples.size() * 0.95) - 1);
        System.out.printf("출석 전체 조회: 이벤트 %d개, 출석 %d건, p95 %dms%n",
                eventCount, eventCount * attendeesPerEvent, p95.toMillis());
        assertThat(p95).isLessThanOrEqualTo(Duration.ofSeconds(2));
    }

    private void assertLargeRoster(JsonNode data, int eventCount, int attendeesPerEvent) {
        assertThat(data.size()).isEqualTo(eventCount);
        data.forEach(event -> assertThat(event.path("attendances").size()).isEqualTo(attendeesPerEvent));
    }

    private void await(CountDownLatch latch) {
        try {
            assertThat(latch.await(REQUEST_TIMEOUT.toSeconds(), TimeUnit.SECONDS)).isTrue();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError("동시성 테스트 대기가 중단되었습니다", exception);
        }
    }

    private JsonNode roster(String token) throws Exception {
        HttpResponse<String> response = get(ROSTER_ENDPOINT, token);
        assertThat(response.statusCode()).as("GET %s", ROSTER_ENDPOINT).isEqualTo(200);
        JsonNode body = json.readTree(response.body());
        assertThat(body.path("code").asString()).isEqualTo("GEN-000");
        assertThat(body.path("data").isArray()).isTrue();
        return body.path("data");
    }

    private HttpResponse<String> get(String path, String token) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri(path)).timeout(REQUEST_TIMEOUT);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return client.send(request.GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://127.0.0.1:" + port + path);
    }

    private String accessToken(Member member) {
        return tokens.createAccessToken(member.getMemberId(), member.getRole().name(), member.getStudentId());
    }

    private Member saveMember(String studentId, Role role) {
        return members.save(member(studentId, role, passwordEncoder.encode(PASSWORD)));
    }

    private Member member(String studentId, Role role, String password) {
        return Member.builder().studentId(studentId).name("회원 " + studentId).password(password)
                .role(role).major(Major.COMPUTER_INFO).xp(0L).build();
    }

    private Event saveEvent(Member host, String title, LocalDateTime eventDate) {
        return events.save(event(host, title, eventDate));
    }

    private Event event(Member host, String title, LocalDateTime eventDate) {
        return Event.builder().host(host).title(title).eventDate(eventDate).location("강의실")
                .rewardXp(3L).eventCategory(EventCategory.REGULAR_MEETING).build();
    }

    private void saveAttendance(Event event, Member member) {
        participants.save(EventParticipant.builder().event(event).participantMember(member).build());
    }
}
