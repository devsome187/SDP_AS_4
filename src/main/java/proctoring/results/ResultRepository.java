package proctoring.results;

import proctoring.persistence.JsonFileStore;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ResultRepository {

    private final JsonFileStore<ExamResult> store;

    public ResultRepository(Path dataDir) {
        store = new JsonFileStore<>(dataDir.resolve("exam_results.json"), ExamResult.class, ExamResult::getId);
    }

    public void save(ExamResult result) {
        store.save(result);
    }

    public Optional<ExamResult> findById(String id) {
        return store.findById(id);
    }

    public List<ExamResult> findByExamSessionId(String examSessionId) {
        return store.findAll().stream()
                .filter(r -> r.getExamSessionId().equals(examSessionId))
                .toList();
    }
}
