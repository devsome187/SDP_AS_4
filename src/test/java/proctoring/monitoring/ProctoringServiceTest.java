package proctoring.monitoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProctoringServiceTest {

    @TempDir
    Path dataDir;

    private ScreenMonitor screenMonitor;
    private ProctoringService proctoring;

    @BeforeEach
    void setUp() {
        screenMonitor = new ScreenMonitor();
        proctoring = new ProctoringService(screenMonitor,
                new PlagiarismChecker(List.of("leaked answer sheet")), new MonitoringRepository(dataDir));
    }

    @Test
    void ownWorkIsCleared() {
        MonitoringSession session = proctoring.monitor("SES-1", "230001", List.of("my own answer"));

        MonitoringSession saved = new MonitoringRepository(dataDir).findById(session.getId()).orElseThrow();
        assertEquals(MonitoringSession.Status.CLEARED, saved.getStatus());
        assertEquals("SES-1", saved.getExamSessionId());
        assertEquals("230001", saved.getStudentId());
    }

    @Test
    void copiedWorkIsFlagged() {
        MonitoringSession session = proctoring.monitor("SES-2", "230001", List.of("leaked answer sheet"));

        assertEquals(MonitoringSession.Status.FLAGGED, session.getStatus());
        assertEquals(1.0, session.getPlagiarismScore());
    }

    @Test
    void offlineMonitorSavesNothing() {
        screenMonitor.setOnline(false);

        assertThrows(ScreenMonitorUnavailableException.class,
                () -> proctoring.monitor("SES-3", "230001", List.of("x")));
        assertTrue(proctoring.findByExamSession("SES-3").isEmpty());
    }

    @Test
    void voidedSessionCannotBeVoidedAgain() {
        String id = proctoring.monitor("SES-4", "230001", List.of("answer")).getId();

        proctoring.cancel(id);

        assertEquals(MonitoringSession.Status.VOIDED, proctoring.get(id).getStatus());
        assertThrows(IllegalStateException.class, () -> proctoring.cancel(id));
        assertThrows(NoSuchElementException.class, () -> proctoring.get("MON-unknown"));
    }

    @Test
    void similarityIsSharedWordsOverAllWords() {
        PlagiarismChecker checker = new PlagiarismChecker(List.of("a b c d"));

        assertEquals(0.75, checker.similarity(List.of("a b", "c")));
        assertEquals(0.0, new PlagiarismChecker(List.of()).similarity(List.of()));
    }
}
