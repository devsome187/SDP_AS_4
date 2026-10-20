package proctoring.access;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExamAccessServiceTest {

    private static final Instant NOW = Instant.parse("2026-12-15T09:00:00Z");

    @TempDir
    Path dataDir;

    private ExamAccessService access;

    @BeforeEach
    void setUp() {
        access = new ExamAccessService(
                new IdentityVerifier(Set.of("230001")),
                new ExamTimer(Clock.fixed(NOW, ZoneOffset.UTC), Map.of("SDP-FINAL", Duration.ofMinutes(120))),
                new ExamSessionRepository(dataDir));
    }

    @Test
    void startedSessionIsSavedWithDeadline() {
        ExamSession session = access.startExam("230001", "SDP-FINAL");

        ExamSession saved = new ExamSessionRepository(dataDir).findById(session.getId()).orElseThrow();
        assertEquals(ExamSession.Status.STARTED, saved.getStatus());
        assertEquals(NOW, saved.getStartedAt());
        assertEquals(NOW.plus(Duration.ofMinutes(120)), saved.getDeadline());
        assertEquals("230001", saved.getStudentId());
        assertEquals("SDP-FINAL", saved.getExamId());
    }

    @Test
    void defaultDurationIs90Minutes() {
        ExamSession session = access.startExam("230001", "OTHER");

        assertEquals(NOW.plus(Duration.ofMinutes(90)), session.getDeadline());
    }

    @Test
    void sessionCanBeSubmittedAndThenCancelled() {
        String id = access.startExam("230001", "SDP-FINAL").getId();

        access.submit(id);
        access.cancel(id);

        assertEquals(ExamSession.Status.CANCELLED, access.get(id).getStatus());
        assertEquals(1, new ExamSessionRepository(dataDir).findAll().size());
    }

    @Test
    void cancelledSessionCannotChange() {
        String id = access.startExam("230001", "SDP-FINAL").getId();
        access.cancel(id);

        assertThrows(IllegalStateException.class, () -> access.submit(id));
        assertThrows(IllegalStateException.class, () -> access.cancel(id));
    }

    @Test
    void unknownOrBadStudentIsRejected() {
        assertThrows(IdentityVerificationException.class, () -> access.startExam("abc", "SDP-FINAL"));
        assertThrows(IdentityVerificationException.class, () -> access.startExam(null, "SDP-FINAL"));
        assertThrows(IdentityVerificationException.class, () -> access.startExam("230002", "SDP-FINAL"));
        assertThrows(NoSuchElementException.class, () -> access.get("SES-unknown"));
    }
}
