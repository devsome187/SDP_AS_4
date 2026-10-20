package proctoring.monitoring;

import proctoring.persistence.JsonFileStore;

import java.nio.file.Path;
import java.util.Optional;

public class MonitoringRepository {

    private final JsonFileStore<MonitoringSession> store;

    public MonitoringRepository(Path dataDir) {
        store = new JsonFileStore<>(dataDir.resolve("monitoring_sessions.json"), MonitoringSession.class,
                MonitoringSession::getId);
    }

    public void save(MonitoringSession session) {
        store.save(session);
    }

    public Optional<MonitoringSession> findById(String id) {
        return store.findById(id);
    }

    public Optional<MonitoringSession> findByExamSessionId(String examSessionId) {
        return store.findAll().stream()
                .filter(s -> s.getExamSessionId().equals(examSessionId))
                .findFirst();
    }
}
