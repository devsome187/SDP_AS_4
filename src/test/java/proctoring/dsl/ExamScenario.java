package proctoring.dsl;

import proctoring.app.ProctoringSystem;
import proctoring.facade.ExamAdminFacade;
import proctoring.facade.ExamOutcome;
import proctoring.facade.ExamProctoringFacade;

import java.nio.file.Path;
import java.util.List;

public class ExamScenario {

    public static final String ALICE = "230001";
    public static final String BOB = "230002";
    public static final String SDP_FINAL = "SDP-FINAL";

    private final Path dataDir;
    private ProctoringSystem system;

    public ExamScenario(Path dataDir) {
        this.dataDir = dataDir;
        this.system = new ProctoringSystem(dataDir);
    }

    public ExamScenario screenMonitorIsOffline() {
        system.setScreenMonitorOnline(false);
        return this;
    }

    public ExamScenario gradingEngineIsDown() {
        system.setGradingEngineOnline(false);
        return this;
    }

    public ExamOutcome studentTakesExam(String studentId, String examId) {
        return studentTakesExam(studentId, examId, ProctoringSystem.ANSWER_KEYS.get(examId));
    }

    public ExamOutcome studentTakesExam(String studentId, String examId, List<String> answers) {
        return examFacade().conductExam(studentId, examId, answers);
    }

    public String studentStartsExamAndAppCrashes(String studentId, String examId) {
        return examFacade().beginExam(studentId, examId, ProctoringSystem.ANSWER_KEYS.get(examId));
    }

    public ExamScenario restartApp() {
        system = new ProctoringSystem(dataDir);
        return this;
    }

    public ExamProctoringFacade examFacade() {
        return system.examFacade();
    }

    public ExamAdminFacade adminFacade() {
        return system.adminFacade();
    }

    public SavedProcess onDisk(ExamOutcome outcome) {
        return onDisk(outcome.processId());
    }

    public SavedProcess onDisk(String processId) {
        return new SavedProcess(dataDir, processId);
    }
}
