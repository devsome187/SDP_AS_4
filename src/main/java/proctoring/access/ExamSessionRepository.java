package proctoring.access;

import proctoring.persistence.JsonFileStore;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ExamSessionRepository {

    private final JsonFileStore<ExamSession> store;

    public ExamSessionRepository(Path dataDir) {
        store = new JsonFileStore<>(dataDir.resolve("exam_sessions.json"), ExamSession.class, ExamSession::getId);
    }

    public void save(ExamSession session) {
        store.save(session);
    }

    public Optional<ExamSession> findById(String id) {
        return store.findById(id);
    }

    public List<ExamSession> findAll() {
        return store.findAll();
    }
}
