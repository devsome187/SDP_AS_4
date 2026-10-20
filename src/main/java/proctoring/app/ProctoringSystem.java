package proctoring.app;

import proctoring.access.ExamAccessService;
import proctoring.access.ExamSessionRepository;
import proctoring.access.ExamTimer;
import proctoring.access.IdentityVerifier;
import proctoring.facade.ExamAdminFacade;
import proctoring.facade.ExamProcessRepository;
import proctoring.facade.ExamProctoringFacade;
import proctoring.monitoring.MonitoringRepository;
import proctoring.monitoring.PlagiarismChecker;
import proctoring.monitoring.ProctoringService;
import proctoring.monitoring.ScreenMonitor;
import proctoring.results.GradingEngine;
import proctoring.results.ResultRepository;
import proctoring.results.ResultsService;
import proctoring.results.TranscriptRecorder;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProctoringSystem {

    public static final Set<String> STUDENTS = Set.of("230001", "230002", "230003");

    public static final Map<String, List<String>> ANSWER_KEYS = Map.of(
            "SDP-FINAL", List.of("facade", "adapter", "composition", "least knowledge", "compensation"),
            "OOP-MIDTERM", List.of("encapsulation", "inheritance", "polymorphism"));

    private static final List<String> LEAKED_ANSWERS =
            List.of("singleton", "observer", "strategy", "decorator", "factory");

    private final ScreenMonitor screenMonitor = new ScreenMonitor();
    private final GradingEngine gradingEngine = new GradingEngine(ANSWER_KEYS);
    private final ExamProctoringFacade examFacade;
    private final ExamAdminFacade adminFacade;

    public ProctoringSystem(Path dataDir) {
        ExamAccessService access = new ExamAccessService(
                new IdentityVerifier(STUDENTS),
                new ExamTimer(Clock.systemUTC(), Map.of("SDP-FINAL", Duration.ofMinutes(120))),
                new ExamSessionRepository(dataDir));
        ProctoringService proctoring = new ProctoringService(
                screenMonitor, new PlagiarismChecker(LEAKED_ANSWERS), new MonitoringRepository(dataDir));
        ResultsService results = new ResultsService(
                gradingEngine, new TranscriptRecorder(), new ResultRepository(dataDir));
        ExamProcessRepository processes = new ExamProcessRepository(dataDir);

        examFacade = new ExamProctoringFacade(access, proctoring, results, processes);
        adminFacade = new ExamAdminFacade(access, proctoring, results, processes, examFacade);
    }

    public ExamProctoringFacade examFacade() {
        return examFacade;
    }

    public ExamAdminFacade adminFacade() {
        return adminFacade;
    }

    public void setScreenMonitorOnline(boolean online) {
        screenMonitor.setOnline(online);
    }

    public void setGradingEngineOnline(boolean online) {
        gradingEngine.setOnline(online);
    }
}
