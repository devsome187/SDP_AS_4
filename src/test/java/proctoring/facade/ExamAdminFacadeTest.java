package proctoring.facade;

import proctoring.dsl.ExamScenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.NoSuchElementException;

import static proctoring.access.ExamSession.Status.CANCELLED;
import static proctoring.dsl.ExamScenario.ALICE;
import static proctoring.dsl.ExamScenario.BOB;
import static proctoring.dsl.ExamScenario.SDP_FINAL;
import static proctoring.facade.ExamProcess.Stage.COMPENSATED;
import static proctoring.monitoring.MonitoringSession.Status.VOIDED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExamAdminFacadeTest {

    @TempDir
    Path dataDir;

    private ExamScenario exam;

    @BeforeEach
    void setUp() {
        exam = new ExamScenario(dataDir);
    }

    @Test
    void reportShowsSavedStatusOfEachSubsystem() {
        ExamOutcome outcome = exam.studentTakesExam(ALICE, SDP_FINAL);

        ProcessReport report = exam.restartApp().adminFacade().inspect(outcome.processId());

        assertEquals("SUBMITTED", report.sessionStatus());
        assertEquals("CLEARED", report.monitoringStatus());
        assertEquals("RECORDED", report.resultStatus());
    }

    @Test
    void listsUnfinishedExams() {
        exam.studentTakesExam(ALICE, SDP_FINAL);
        exam.studentStartsExamAndAppCrashes(BOB, SDP_FINAL);

        assertEquals(2, exam.adminFacade().listProcesses().size());
        assertEquals(1, exam.adminFacade().listUnfinished().size());
    }

    @Test
    void manualCompensationUndoesCThenBThenA() {
        ExamOutcome outcome = exam.studentTakesExam(ALICE, SDP_FINAL);

        exam.adminFacade().compensate(outcome.processId(), "Exam annulled");

        exam.onDisk(outcome).stageIs(COMPENSATED)
                .resultIs(proctoring.results.ExamResult.Status.VOIDED)
                .monitoringIs(VOIDED).sessionIs(CANCELLED)
                .rolledBack("C", "B", "A");
    }

    @Test
    void compensatedExamCannotBeCompensatedAgain() {
        ExamOutcome outcome = exam.screenMonitorIsOffline().studentTakesExam(ALICE, SDP_FINAL);

        assertThrows(IllegalStateException.class,
                () -> exam.adminFacade().compensate(outcome.processId(), "again"));
        assertThrows(NoSuchElementException.class, () -> exam.adminFacade().inspect("PRC-unknown"));
    }
}
