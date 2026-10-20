package proctoring.facade;

import proctoring.access.ExamAccessService;
import proctoring.monitoring.ProctoringService;
import proctoring.results.ResultsService;

import java.util.List;
import java.util.NoSuchElementException;

public class ExamAdminFacade {

    private final ExamAccessService access;
    private final ProctoringService proctoring;
    private final ResultsService results;
    private final ExamProcessRepository processes;
    private final ExamProctoringFacade examFacade;

    public ExamAdminFacade(ExamAccessService access, ProctoringService proctoring, ResultsService results,
                           ExamProcessRepository processes, ExamProctoringFacade examFacade) {
        this.access = access;
        this.proctoring = proctoring;
        this.results = results;
        this.processes = processes;
        this.examFacade = examFacade;
    }

    public List<ProcessReport> listProcesses() {
        return processes.findAll().stream().map(this::reportOf).toList();
    }

    public List<ProcessReport> listUnfinished() {
        return processes.findUnfinished().stream().map(this::reportOf).toList();
    }

    public ProcessReport inspect(String processId) {
        return reportOf(load(processId));
    }

    public ProcessReport compensate(String processId, String reason) {
        ExamProcess process = load(processId);
        if (process.getStage() == ExamProcess.Stage.COMPENSATED) {
            throw new IllegalStateException("Process " + processId + " is already compensated");
        }
        examFacade.rollback(process, reason);
        return inspect(processId);
    }

    private ProcessReport reportOf(ExamProcess process) {
        String a = access.get(process.getExamSessionId()).getStatus().name();
        String b = process.getMonitoringId() == null ? "-"
                : proctoring.get(process.getMonitoringId()).getStatus().name();
        String c = results.findLatest(process.getExamSessionId())
                .map(r -> r.getStatus().name())
                .orElse("-");
        return new ProcessReport(process.getId(), process.getStudentId(), process.getExamId(), process.getStage(),
                a, b, c, process.getRollbackLog());
    }

    private ExamProcess load(String processId) {
        return processes.findById(processId)
                .orElseThrow(() -> new NoSuchElementException("Exam process not found: " + processId));
    }
}
