package proctoring.facade;

import proctoring.access.ExamAccessService;
import proctoring.access.ExamSession;
import proctoring.access.IdentityVerificationException;
import proctoring.monitoring.MonitoringSession;
import proctoring.monitoring.ProctoringService;
import proctoring.monitoring.ScreenMonitorUnavailableException;
import proctoring.results.ExamResult;
import proctoring.results.GradingRejectedException;
import proctoring.results.ResultsService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ExamProctoringFacade {

    private final ExamAccessService access;
    private final ProctoringService proctoring;
    private final ResultsService results;
    private final ExamProcessRepository processes;

    public ExamProctoringFacade(ExamAccessService access, ProctoringService proctoring, ResultsService results,
                                ExamProcessRepository processes) {
        this.access = access;
        this.proctoring = proctoring;
        this.results = results;
        this.processes = processes;
    }

    public ExamOutcome conductExam(String studentId, String examId, List<String> answers) {
        return resume(beginExam(studentId, examId, answers));
    }

    public String beginExam(String studentId, String examId, List<String> answers) {
        if (examId == null || examId.isBlank()) {
            throw new ExamRequestRejectedException("Exam ID is empty");
        }
        if (answers == null || answers.isEmpty()) {
            throw new ExamRequestRejectedException("No answers submitted");
        }
        ExamSession session;
        try {
            session = access.startExam(studentId, examId);
        } catch (IdentityVerificationException e) {
            throw new ExamRequestRejectedException(e.getMessage());
        }
        ExamProcess process = new ExamProcess(studentId, examId, answers, session.getId());
        processes.save(process);
        return process.getId();
    }

    public ExamOutcome resume(String processId) {
        ExamProcess process = load(processId);
        while (!process.isFinished()) {
            if (process.getStage() == ExamProcess.Stage.PENDING_B) {
                runMonitoring(process);
            } else {
                runGrading(process);
            }
            processes.save(process);
            process = load(processId);
        }
        return outcomeOf(process);
    }

    public List<ExamOutcome> resumeUnfinished() {
        List<ExamOutcome> outcomes = new ArrayList<>();
        for (ExamProcess process : processes.findUnfinished()) {
            outcomes.add(resume(process.getId()));
        }
        return outcomes;
    }

    private void runMonitoring(ExamProcess process) {
        try {
            MonitoringSession monitoring = proctoring.findByExamSession(process.getExamSessionId())
                    .orElseGet(() -> proctoring.monitor(process.getExamSessionId(), process.getStudentId(),
                            process.getAnswers()));
            process.monitoringDone(monitoring.getId());
        } catch (ScreenMonitorUnavailableException e) {
            rollback(process, e.getMessage());
        }
    }

    private void runGrading(ExamProcess process) {
        try {
            ExamResult result = results.findRecorded(process.getExamSessionId())
                    .orElseGet(() -> results.gradeAndRecord(process.getExamSessionId(), process.getStudentId(),
                            process.getExamId(), process.getAnswers()));
            if (access.get(process.getExamSessionId()).getStatus() == ExamSession.Status.STARTED) {
                access.submit(process.getExamSessionId());
            }
            process.gradingDone(result.getId());
        } catch (GradingRejectedException e) {
            rollback(process, e.getMessage());
        }
    }

    void rollback(ExamProcess process, String reason) {
        List<String> log = new ArrayList<>();
        if (process.getResultId() != null
                && results.get(process.getResultId()).getStatus() == ExamResult.Status.RECORDED) {
            results.cancel(process.getResultId());
            log.add("C: result " + process.getResultId() + " voided");
        }
        if (process.getMonitoringId() != null
                && proctoring.get(process.getMonitoringId()).getStatus() != MonitoringSession.Status.VOIDED) {
            proctoring.cancel(process.getMonitoringId());
            log.add("B: monitoring " + process.getMonitoringId() + " voided");
        }
        if (access.get(process.getExamSessionId()).getStatus() != ExamSession.Status.CANCELLED) {
            access.cancel(process.getExamSessionId());
            log.add("A: session " + process.getExamSessionId() + " cancelled");
        }
        process.compensated(reason, log);
        processes.save(process);
    }

    private ExamProcess load(String processId) {
        return processes.findById(processId)
                .orElseThrow(() -> new NoSuchElementException("Exam process not found: " + processId));
    }

    private ExamOutcome outcomeOf(ExamProcess process) {
        if (process.getStage() == ExamProcess.Stage.COMPLETED) {
            ExamResult result = results.get(process.getResultId());
            return new ExamOutcome(process.getId(), process.getStage(), result.getScore(), result.getGrade(), null);
        }
        return new ExamOutcome(process.getId(), process.getStage(), null, null, process.getFailureReason());
    }
}
