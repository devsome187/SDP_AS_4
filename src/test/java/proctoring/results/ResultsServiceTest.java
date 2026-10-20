package proctoring.results;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultsServiceTest {

    @TempDir
    Path dataDir;

    private GradingEngine engine;
    private ResultsService results;

    @BeforeEach
    void setUp() {
        engine = new GradingEngine(Map.of("QUIZ", List.of("a", "b", "c", "d")));
        results = new ResultsService(engine, new TranscriptRecorder(), new ResultRepository(dataDir));
    }

    @Test
    void resultIsGradedAndSaved() {
        ExamResult result = results.gradeAndRecord("SES-1", "230001", "QUIZ", List.of("a", " B ", "c", "x"));

        ExamResult saved = new ResultRepository(dataDir).findById(result.getId()).orElseThrow();
        assertEquals(ExamResult.Status.RECORDED, saved.getStatus());
        assertEquals(75, saved.getScore());
        assertEquals("B", saved.getGrade());
        assertEquals("230001", saved.getStudentId());
        assertEquals("QUIZ", saved.getExamId());
    }

    @Test
    void rejectedWorkIsSavedAsRejected() {
        assertThrows(GradingRejectedException.class,
                () -> results.gradeAndRecord("S1", "230001", "NO-KEY", List.of("a")));
        assertThrows(GradingRejectedException.class,
                () -> results.gradeAndRecord("S2", "230001", "QUIZ", List.of("a")));
        engine.setOnline(false);
        assertThrows(GradingRejectedException.class,
                () -> results.gradeAndRecord("S3", "230001", "QUIZ", List.of("a", "b", "c", "d")));

        ExamResult rejected = results.findLatest("S3").orElseThrow();
        assertEquals(ExamResult.Status.REJECTED, rejected.getStatus());
        assertTrue(rejected.getReason().contains("maintenance"));
        assertTrue(results.findRecorded("S3").isEmpty());
        assertTrue(results.findLatest("S4").isEmpty());
    }

    @Test
    void onlyRecordedResultCanBeVoided() {
        String id = results.gradeAndRecord("SES-1", "230001", "QUIZ", List.of("a", "b", "c", "d")).getId();

        results.cancel(id);

        assertEquals(ExamResult.Status.VOIDED, results.get(id).getStatus());
        assertThrows(IllegalStateException.class, () -> results.cancel(id));
        assertThrows(NoSuchElementException.class, () -> results.get("RES-unknown"));
    }

    @Test
    void letterGrades() {
        TranscriptRecorder recorder = new TranscriptRecorder();
        assertEquals("A", recorder.letterGrade(90));
        assertEquals("B", recorder.letterGrade(75));
        assertEquals("C", recorder.letterGrade(60));
        assertEquals("D", recorder.letterGrade(50));
        assertEquals("F", recorder.letterGrade(49));
    }
}
