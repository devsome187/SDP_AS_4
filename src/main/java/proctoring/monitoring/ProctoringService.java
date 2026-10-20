package proctoring.monitoring;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ProctoringService {

    private final ScreenMonitor screenMonitor;
    private final PlagiarismChecker plagiarismChecker;
    private final MonitoringRepository repository;

    public ProctoringService(ScreenMonitor screenMonitor, PlagiarismChecker plagiarismChecker,
                             MonitoringRepository repository) {
        this.screenMonitor = screenMonitor;
        this.plagiarismChecker = plagiarismChecker;
        this.repository = repository;
    }

    public MonitoringSession monitor(String examSessionId, String studentId, List<String> answers) {
        screenMonitor.startCapture(studentId);
        double score = plagiarismChecker.similarity(answers);
        MonitoringSession session = new MonitoringSession(examSessionId, studentId, score,
                plagiarismChecker.isSuspicious(score));
        repository.save(session);
        return session;
    }

    public void cancel(String monitoringId) {
        MonitoringSession session = get(monitoringId);
        session.cancel();
        repository.save(session);
    }

    public MonitoringSession get(String monitoringId) {
        return repository.findById(monitoringId)
                .orElseThrow(() -> new NoSuchElementException("Monitoring session not found: " + monitoringId));
    }

    public Optional<MonitoringSession> findByExamSession(String examSessionId) {
        return repository.findByExamSessionId(examSessionId);
    }
}
