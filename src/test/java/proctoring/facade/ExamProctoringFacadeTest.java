package proctoring.facade;

import proctoring.dsl.ExamScenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static proctoring.access.ExamSession.Status.CANCELLED;
import static proctoring.access.ExamSession.Status.STARTED;
import static proctoring.access.ExamSession.Status.SUBMITTED;
import static proctoring.dsl.ExamScenario.ALICE;
import static proctoring.dsl.ExamScenario.BOB;
import static proctoring.dsl.ExamScenario.SDP_FINAL;
import static proctoring.facade.ExamProcess.Stage.COMPENSATED;
import static proctoring.facade.ExamProcess.Stage.COMPLETED;
import static proctoring.facade.ExamProcess.Stage.PENDING_B;
import static proctoring.monitoring.MonitoringSession.Status.CLEARED;
import static proctoring.monitoring.MonitoringSession.Status.VOIDED;
import static proctoring.results.ExamResult.Status.RECORDED;
import static proctoring.results.ExamResult.Status.REJECTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExamProctoringFacadeTest {

    @TempDir
    Path dataDir;

    private ExamScenario exam;

    @BeforeEach
    void setUp() {
        exam = new ExamScenario(dataDir);
    }

    @Test
    void successfulExamGoesThroughAllThreeSteps() {
        ExamOutcome outcome = exam.studentTakesExam(ALICE, SDP_FINAL);

        assertTrue(outcome.isCompleted());
        assertEquals(100, outcome.score());
        assertEquals("A", outcome.grade());
        exam.onDisk(outcome).stageIs(COMPLETED).sessionIs(SUBMITTED).monitoringIs(CLEARED).resultIs(RECORDED);
    }

    @Test
    void screenMonitorFailureCancelsSession() {
        ExamOutcome outcome = exam.screenMonitorIsOffline().studentTakesExam(ALICE, SDP_FINAL);

        assertFalse(outcome.isCompleted());
        exam.onDisk(outcome).stageIs(COMPENSATED).sessionIs(CANCELLED).hasNoMonitoring().rolledBack("A");
    }

    @Test
    void gradingFailureVoidsMonitoringThenCancelsSession() {
        ExamOutcome outcome = exam.gradingEngineIsDown().studentTakesExam(ALICE, SDP_FINAL);

        exam.onDisk(outcome).stageIs(COMPENSATED)
                .resultIs(REJECTED).monitoringIs(VOIDED).sessionIs(CANCELLED)
                .rolledBack("B", "A");
    }

    @Test
    void wrongNumberOfAnswersIsRejectedByGrading() {
        ExamOutcome outcome = exam.studentTakesExam(BOB, SDP_FINAL, List.of("facade"));

        exam.onDisk(outcome).stageIs(COMPENSATED).rolledBack("B", "A");
    }

    @Test
    void invalidStudentIdStopsBeforeAnythingIsSaved() {
        assertThrows(ExamRequestRejectedException.class, () -> exam.studentTakesExam("12ab", SDP_FINAL));
        assertThrows(ExamRequestRejectedException.class, () -> exam.studentTakesExam("999999", SDP_FINAL));
        assertThrows(ExamRequestRejectedException.class, () -> exam.studentTakesExam(ALICE, "", List.of("x")));
        assertThrows(ExamRequestRejectedException.class, () -> exam.studentTakesExam(ALICE, SDP_FINAL, List.of()));

        assertFalse(Files.exists(dataDir.resolve("exam_sessions.json")));
        assertFalse(Files.exists(dataDir.resolve("exam_process.json")));
    }

    @Test
    void examInterruptedAfterStepAContinuesAfterRestart() {
        String processId = exam.studentStartsExamAndAppCrashes(ALICE, SDP_FINAL);
        exam.onDisk(processId).stageIs(PENDING_B).sessionIs(STARTED);

        ExamOutcome outcome = exam.restartApp().examFacade().resume(processId);

        assertTrue(outcome.isCompleted());
        exam.onDisk(processId).stageIs(COMPLETED).sessionIs(SUBMITTED).monitoringIs(CLEARED).resultIs(RECORDED);
    }

    @Test
    void failureAfterRestartStillRollsBackStepA() {
        String processId = exam.studentStartsExamAndAppCrashes(BOB, SDP_FINAL);

        List<ExamOutcome> outcomes = exam.restartApp().screenMonitorIsOffline().examFacade().resumeUnfinished();

        assertEquals(1, outcomes.size());
        exam.onDisk(processId).stageIs(COMPENSATED).sessionIs(CANCELLED).rolledBack("A");
    }
}
