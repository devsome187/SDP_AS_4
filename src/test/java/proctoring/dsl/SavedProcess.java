package proctoring.dsl;

import proctoring.access.ExamSession;
import proctoring.access.ExamSessionRepository;
import proctoring.facade.ExamProcess;
import proctoring.facade.ExamProcessRepository;
import proctoring.monitoring.MonitoringRepository;
import proctoring.monitoring.MonitoringSession;
import proctoring.results.ExamResult;
import proctoring.results.ResultRepository;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SavedProcess {

    private final Path dataDir;
    private final ExamProcess process;

    SavedProcess(Path dataDir, String processId) {
        this.dataDir = dataDir;
        this.process = new ExamProcessRepository(dataDir).findById(processId).orElseThrow();
    }

    public SavedProcess stageIs(ExamProcess.Stage stage) {
        assertEquals(stage, process.getStage());
        return this;
    }

    public SavedProcess sessionIs(ExamSession.Status status) {
        ExamSession session = new ExamSessionRepository(dataDir).findById(process.getExamSessionId()).orElseThrow();
        assertEquals(status, session.getStatus());
        return this;
    }

    public SavedProcess monitoringIs(MonitoringSession.Status status) {
        MonitoringSession monitoring = new MonitoringRepository(dataDir)
                .findByExamSessionId(process.getExamSessionId()).orElseThrow();
        assertEquals(status, monitoring.getStatus());
        return this;
    }

    public SavedProcess hasNoMonitoring() {
        assertTrue(new MonitoringRepository(dataDir).findByExamSessionId(process.getExamSessionId()).isEmpty());
        return this;
    }

    public SavedProcess resultIs(ExamResult.Status status) {
        List<ExamResult> results = new ResultRepository(dataDir).findByExamSessionId(process.getExamSessionId());
        assertEquals(status, results.get(results.size() - 1).getStatus());
        return this;
    }

    public SavedProcess rolledBack(String... steps) {
        List<String> undone = process.getRollbackLog().stream().map(line -> line.substring(0, 1)).toList();
        assertEquals(List.of(steps), undone);
        return this;
    }
}
