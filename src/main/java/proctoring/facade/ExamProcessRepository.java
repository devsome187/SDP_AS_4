package proctoring.facade;

import proctoring.persistence.JsonFileStore;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ExamProcessRepository {

    private final JsonFileStore<ExamProcess> store;

    public ExamProcessRepository(Path dataDir) {
        store = new JsonFileStore<>(dataDir.resolve("exam_process.json"), ExamProcess.class, ExamProcess::getId);
    }

    public void save(ExamProcess process) {
        store.save(process);
    }

    public Optional<ExamProcess> findById(String id) {
        return store.findById(id);
    }

    public List<ExamProcess> findAll() {
        return store.findAll();
    }

    public List<ExamProcess> findUnfinished() {
        return findAll().stream().filter(p -> !p.isFinished()).toList();
    }
}
